package dao;

import excepciones.AsientoOcupadoException;
import excepciones.DatoInvalidoException;
import excepciones.ElementoNoEncontradoException;
import excepciones.PersistenciaException;
import org.cine.enums.EstadoReserva;
import org.cine.enums.TipoEntrada;
import org.cine.modelo.Cliente;
import org.cine.modelo.Funcion;
import org.cine.modelo.Reserva;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservaDAO {
    private static final String CONSULTA = """
            SELECT r.*, c.nombre, c.apellido, c.dni, c.email,
                   f.fecha_hora, f.precio_base, f.estado AS estado_funcion,
                   p.id_pelicula, p.titulo, p.genero, p.duracion_minutos, p.clasificacion, p.activa,
                   s.id_sala, s.numero, s.capacidad, s.habilitada
            FROM reserva r
            JOIN cliente c ON c.id_cliente = r.id_cliente
            JOIN funcion f ON f.id_funcion = r.id_funcion
            JOIN pelicula p ON p.id_pelicula = f.id_pelicula
            JOIN sala s ON s.id_sala = f.id_sala
            """;

    public void guardarReserva(Reserva reserva)
            throws PersistenciaException, DatoInvalidoException, AsientoOcupadoException {
        if (reserva == null || reserva.getIdReserva() != 0 || !reserva.estaConfirmada()) {
            throw new DatoInvalidoException("Solo se puede dar de alta una reserva nueva y confirmada.");
        }
        try (Connection conexion = ConexionDB.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                // Se vuelve a consultar con bloqueo para que nadie cambie la sala durante el alta.
                Funcion funcion = FuncionDAO.buscarPorId(conexion, reserva.getFuncion().getIdFuncion(), true);
                if (funcion == null || !funcion.estaDisponible()) {
                    throw new DatoInvalidoException("La función ya no está disponible.");
                }
                if (reserva.getNumeroAsiento() < 1 || reserva.getNumeroAsiento() > funcion.getSala().getCapacidad()) {
                    throw new DatoInvalidoException("El número de asiento está fuera de la capacidad de la sala.");
                }
                int id = insertar(conexion, reserva);
                conexion.commit();
                reserva.setIdReserva(id);
            } catch (SQLException | DatoInvalidoException e) {
                ConexionDB.revertir(conexion, e);
                throw e;
            }
        } catch (SQLException e) {
            // MySQL rechaza la segunda reserva confirmada del mismo asiento.
            if (e.getErrorCode() == 1062) {
                throw new AsientoOcupadoException("Ese asiento ya está reservado.");
            }
            throw new PersistenciaException("No se pudo guardar la reserva.", e);
        }
    }

    private int insertar(Connection conexion, Reserva reserva) throws SQLException {
        String sql = """
                INSERT INTO reserva
                    (id_cliente, id_funcion, numero_asiento, tipo_entrada, precio_final, fecha_reserva, estado)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, reserva.getCliente().getId());
            stmt.setInt(2, reserva.getFuncion().getIdFuncion());
            stmt.setInt(3, reserva.getNumeroAsiento());
            stmt.setString(4, reserva.getTipoEntrada().name());
            stmt.setBigDecimal(5, reserva.getPrecioFinal());
            stmt.setObject(6, reserva.getFechaReserva());
            stmt.setString(7, reserva.getEstado().name());
            stmt.executeUpdate();
            try (ResultSet claves = stmt.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("MySQL no devolvió el identificador de la reserva.");
                }
                return claves.getInt(1);
            }
        }
    }

    public Reserva buscarReserva(int idReserva) throws PersistenciaException {
        List<Reserva> reservas = buscar(CONSULTA + " WHERE r.id_reserva = ?", idReserva);
        return reservas.isEmpty() ? null : reservas.get(0);
    }

    public List<Reserva> listarPorCliente(int idCliente) throws PersistenciaException {
        return buscar(CONSULTA + " WHERE r.id_cliente = ? ORDER BY r.fecha_reserva DESC, r.id_reserva DESC", idCliente);
    }

    public List<Reserva> listarPorFuncion(int idFuncion) throws PersistenciaException {
        return buscar(CONSULTA + " WHERE r.id_funcion = ? ORDER BY r.numero_asiento", idFuncion);
    }

    public void cancelarReserva(int idReserva) throws PersistenciaException, ElementoNoEncontradoException {
        String sql = "UPDATE reserva SET estado = 'CANCELADA' WHERE id_reserva = ? AND estado = 'CONFIRMADA'";
        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, idReserva);
            if (stmt.executeUpdate() == 0 && estadoActual(conexion, idReserva) != EstadoReserva.CANCELADA) {
                throw new PersistenciaException("La reserva no se puede cancelar en su estado actual.");
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo cancelar la reserva.", e);
        }
    }

    public void confirmarReserva(int idReserva) throws PersistenciaException {
        // Crear una reserva la confirma; una cancelada es histórica y no se reabre.
        try (Connection conexion = ConexionDB.obtenerConexion()) {
            if (estadoActual(conexion, idReserva) != EstadoReserva.CONFIRMADA) {
                throw new PersistenciaException("La reserva está cancelada. Creá otra para volver a ocupar el asiento.");
            }
        } catch (SQLException | ElementoNoEncontradoException e) {
            throw new PersistenciaException("No se pudo confirmar la reserva: " + e.getMessage(), e);
        }
    }

    private EstadoReserva estadoActual(Connection conexion, int idReserva)
            throws SQLException, ElementoNoEncontradoException {
        try (PreparedStatement stmt = conexion.prepareStatement("SELECT estado FROM reserva WHERE id_reserva = ?")) {
            stmt.setInt(1, idReserva);
            try (ResultSet fila = stmt.executeQuery()) {
                if (!fila.next()) {
                    throw new ElementoNoEncontradoException("No existe la reserva " + idReserva + ".");
                }
                return EstadoReserva.valueOf(fila.getString(1));
            }
        }
    }

    public boolean asientoOcupado(int idFuncion, int asiento) throws PersistenciaException {
        String sql = "SELECT 1 FROM reserva WHERE id_funcion = ? AND asiento_confirmado = ? LIMIT 1";
        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, idFuncion);
            stmt.setInt(2, asiento);
            try (ResultSet fila = stmt.executeQuery()) {
                return fila.next();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo consultar el asiento.", e);
        }
    }

    public List<Integer> asientosOcupados(int idFuncion) throws PersistenciaException {
        String sql = "SELECT asiento_confirmado FROM reserva WHERE id_funcion = ? AND asiento_confirmado IS NOT NULL";
        List<Integer> asientos = new ArrayList<>();
        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, idFuncion);
            try (ResultSet filas = stmt.executeQuery()) {
                while (filas.next()) {
                    asientos.add(filas.getInt(1));
                }
            }
            return asientos;
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudieron consultar los asientos.", e);
        }
    }

    private List<Reserva> buscar(String sql, int id) throws PersistenciaException {
        List<Reserva> reservas = new ArrayList<>();
        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet filas = stmt.executeQuery()) {
                while (filas.next()) {
                    reservas.add(new Reserva(filas.getInt("id_reserva"), mapearCliente(filas),
                            FuncionDAO.mapearFuncion(filas), filas.getInt("numero_asiento"),
                            TipoEntrada.valueOf(filas.getString("tipo_entrada")), filas.getBigDecimal("precio_final"),
                            filas.getObject("fecha_reserva", LocalDateTime.class),
                            EstadoReserva.valueOf(filas.getString("estado"))));
                }
            }
            return reservas;
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudieron recuperar las reservas.", e);
        }
    }

    private static Cliente mapearCliente(ResultSet fila) throws SQLException {
        return new Cliente(fila.getInt("id_cliente"), fila.getString("nombre"), fila.getString("apellido"),
                fila.getString("dni"), fila.getString("email"), null);
    }
}
