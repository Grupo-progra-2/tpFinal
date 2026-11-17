package dao;

import excepciones.DatoInvalidoException;
import excepciones.HorarioSuperpuestoException;
import excepciones.PersistenciaException;
import org.cine.enums.EstadoFuncion;
import org.cine.modelo.Funcion;
import org.cine.modelo.Pelicula;
import org.cine.modelo.Sala;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FuncionDAO {
    private static final String CONSULTA = """
            SELECT f.id_funcion, f.fecha_hora, f.precio_base, f.estado AS estado_funcion,
                   p.id_pelicula, p.titulo, p.genero, p.duracion_minutos, p.clasificacion, p.activa,
                   s.id_sala, s.numero, s.capacidad, s.habilitada
            FROM funcion f
            JOIN pelicula p ON p.id_pelicula = f.id_pelicula
            JOIN sala s ON s.id_sala = f.id_sala
            """;
    private static final String DISPONIBLES =
            "f.estado = 'PROGRAMADA' AND f.fecha_hora > ? AND s.habilitada AND p.activa";
    private static final BigDecimal PRECIO_MAXIMO = new BigDecimal("99999999.99");

    public void guardar(Funcion funcion)
            throws PersistenciaException, DatoInvalidoException, HorarioSuperpuestoException {
        if (funcion == null || !funcion.estaDisponible()
                || funcion.getPelicula().getIdPelicula() == null || funcion.getSala().getIdSala() <= 0
                || funcion.getPrecioBase() == null || funcion.getPrecioBase().signum() <= 0
                || funcion.getPrecioBase().compareTo(PRECIO_MAXIMO) > 0
                || funcion.getPrecioBase().stripTrailingZeros().scale() > 2) {
            throw new DatoInvalidoException("Elegí una sala habilitada, una película activa, una fecha futura y un precio positivo de hasta dos decimales.");
        }
        String sql = """
                INSERT INTO funcion (id_pelicula, id_sala, fecha_hora, precio_base, estado)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection conexion = ConexionDB.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                int duracion = verificarSalaYPelicula(conexion, funcion);
                if (!funcion.getFechaHora().isAfter(LocalDateTime.now())) {
                    throw new DatoInvalidoException("La función tiene que empezar en el futuro.");
                }
                verificarHorario(conexion, funcion, duracion);
                int id;
                try (PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, funcion.getPelicula().getIdPelicula());
                    stmt.setInt(2, funcion.getSala().getIdSala());
                    stmt.setObject(3, funcion.getFechaHora());
                    stmt.setBigDecimal(4, funcion.getPrecioBase());
                    stmt.setString(5, funcion.getEstado().name());
                    stmt.executeUpdate();
                    try (ResultSet claves = stmt.getGeneratedKeys()) {
                        if (!claves.next()) {
                            throw new SQLException("MySQL no devolvió el identificador de la función.");
                        }
                        id = claves.getInt(1);
                    }
                }
                conexion.commit();
                funcion.setIdFuncion(id);
            } catch (SQLException | DatoInvalidoException | HorarioSuperpuestoException e) {
                ConexionDB.revertir(conexion, e);
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo guardar la función.", e);
        }
    }

    private int verificarSalaYPelicula(Connection conexion, Funcion funcion)
            throws SQLException, DatoInvalidoException {
        // Todas las altas en una sala bloquean primero la misma fila, incluso cuando está vacía.
        try (PreparedStatement sala = conexion.prepareStatement(
                "SELECT habilitada FROM sala WHERE id_sala = ? FOR UPDATE")) {
            sala.setInt(1, funcion.getSala().getIdSala());
            try (ResultSet fila = sala.executeQuery()) {
                if (!fila.next() || !fila.getBoolean("habilitada")) {
                    throw new DatoInvalidoException("La sala no está habilitada.");
                }
            }
        }
        try (PreparedStatement pelicula = conexion.prepareStatement(
                "SELECT activa, duracion_minutos FROM pelicula WHERE id_pelicula = ? FOR SHARE")) {
            pelicula.setInt(1, funcion.getPelicula().getIdPelicula());
            try (ResultSet fila = pelicula.executeQuery()) {
                if (!fila.next() || !fila.getBoolean("activa") || fila.getInt("duracion_minutos") <= 0) {
                    throw new DatoInvalidoException("La película debe estar activa y tener una duración válida.");
                }
                return fila.getInt("duracion_minutos");
            }
        }
    }

    private void verificarHorario(Connection conexion, Funcion funcion, int duracion)
            throws SQLException, HorarioSuperpuestoException {
        String sql = """
                SELECT f.fecha_hora, p.duracion_minutos
                FROM funcion f JOIN pelicula p ON p.id_pelicula = f.id_pelicula
                WHERE f.id_sala = ? AND f.estado = 'PROGRAMADA' AND f.fecha_hora < ?
                """;
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, funcion.getSala().getIdSala());
            stmt.setObject(2, funcion.getFechaHora().plusMinutes(duracion));
            try (ResultSet fila = stmt.executeQuery()) {
                while (fila.next()) {
                    LocalDateTime inicio = fila.getObject("fecha_hora", LocalDateTime.class);
                    LocalDateTime fin = inicio.plusMinutes(fila.getInt("duracion_minutos"));
                    if (fin.isAfter(funcion.getFechaHora())) {
                        throw new HorarioSuperpuestoException("La sala ya tiene una función en ese horario.");
                    }
                }
            }
        }
    }

    public Funcion buscarPorId(int idFuncion) throws PersistenciaException {
        try (Connection conexion = ConexionDB.obtenerConexion()) {
            return buscarPorId(conexion, idFuncion, false);
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo buscar la función.", e);
        }
    }

    static Funcion buscarPorId(Connection conexion, int idFuncion, boolean bloquear) throws SQLException {
        String sql = CONSULTA + " WHERE f.id_funcion = ?" + (bloquear ? " FOR SHARE" : "");
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, idFuncion);
            try (ResultSet fila = stmt.executeQuery()) {
                return fila.next() ? mapearFuncion(fila) : null;
            }
        }
    }

    public List<Funcion> listarPorPelicula(int idPelicula) throws PersistenciaException {
        return listarDisponibles(idPelicula);
    }

    public List<Funcion> listarDisponibles() throws PersistenciaException {
        return listarDisponibles(null);
    }

    private List<Funcion> listarDisponibles(Integer idPelicula) throws PersistenciaException {
        String sql = CONSULTA + " WHERE " + DISPONIBLES;
        if (idPelicula != null) {
            sql += " AND f.id_pelicula = ?";
        }
        sql += " ORDER BY f.fecha_hora, f.id_funcion";
        List<Funcion> funciones = new ArrayList<>();
        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setObject(1, LocalDateTime.now());
            if (idPelicula != null) {
                stmt.setInt(2, idPelicula);
            }
            try (ResultSet filas = stmt.executeQuery()) {
                while (filas.next()) {
                    funciones.add(mapearFuncion(filas));
                }
            }
            return funciones;
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudieron listar las funciones.", e);
        }
    }

    static Funcion mapearFuncion(ResultSet fila) throws SQLException {
        Pelicula pelicula = new Pelicula(fila.getInt("id_pelicula"), fila.getString("titulo"),
                fila.getString("genero"), fila.getInt("duracion_minutos"),
                fila.getString("clasificacion"), fila.getBoolean("activa"));
        Sala sala = new Sala(fila.getInt("id_sala"), fila.getInt("numero"),
                fila.getInt("capacidad"), fila.getBoolean("habilitada"));
        return new Funcion(fila.getInt("id_funcion"), pelicula, sala,
                fila.getObject("fecha_hora", LocalDateTime.class), fila.getBigDecimal("precio_base"),
                EstadoFuncion.valueOf(fila.getString("estado_funcion")));
    }
}
