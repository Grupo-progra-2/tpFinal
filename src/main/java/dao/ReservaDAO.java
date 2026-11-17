package dao;

import excepciones.AsientoOcupadoException;
import excepciones.DatoInvalidoException;
import excepciones.ElementoNoEncontradoException;
import excepciones.PersistenciaException;
import org.cine.enums.EstadoReserva;
import org.cine.enums.TipoEntrada;
import org.cine.modelo.Cliente;
import org.cine.modelo.Entrada;
import org.cine.modelo.EntradaGeneral;
import org.cine.modelo.EntradaReducida;
import org.cine.modelo.Funcion;
import org.cine.modelo.Reserva;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReservaDAO {
    private static final String CONSULTA = """
            SELECT r.id_reserva, r.numero_asiento, r.tipo_entrada, r.precio_final, r.fecha_reserva,
                   r.estado AS estado_reserva, c.id_cliente, c.nombre, c.apellido, c.dni, c.email,
            """ + FuncionDAO.COLUMNAS + """
            FROM reserva r
            JOIN cliente c ON c.id_cliente = r.id_cliente
            JOIN funcion f ON f.id_funcion = r.id_funcion
            """ + FuncionDAO.RELACIONES;

    public Reserva crearReserva(int idCliente, int idFuncion, int asiento, TipoEntrada tipo, BigDecimal descuento)
            throws PersistenciaException, ElementoNoEncontradoException, DatoInvalidoException, AsientoOcupadoException {
        if (tipo == null) {
            throw new DatoInvalidoException("Elegí un tipo de entrada.");
        }
        try (Connection conexion = ConexionDB.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                Funcion funcion = verificarAsiento(conexion, idFuncion, asiento);
                Cliente cliente = buscarCliente(conexion, idCliente);
                Entrada entrada;
                try {
                    entrada = switch (tipo) {
                        case GENERAL -> new EntradaGeneral(funcion.getPrecioBase());
                        case REDUCIDA -> new EntradaReducida(funcion.getPrecioBase(), descuento);
                    };
                } catch (IllegalArgumentException e) {
                    throw new DatoInvalidoException(e.getMessage());
                }
                Reserva reserva = new Reserva(0, cliente, funcion, asiento, entrada,
                        entrada.calcularPrecio(), LocalDateTime.now().withNano(0), EstadoReserva.CONFIRMADA);
                int id = insertar(conexion, reserva);
                conexion.commit();
                reserva.setIdReserva(id);
                return reserva;
            } catch (SQLException | ElementoNoEncontradoException | DatoInvalidoException | AsientoOcupadoException e) {
                ConexionDB.revertir(conexion, e);
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo guardar la reserva.", e);
        }
    }

    public void guardarReserva(Reserva reserva) throws PersistenciaException {
        if (reserva == null || reserva.getIdReserva() != 0 || !reserva.estaConfirmada()) {
            throw new PersistenciaException("El alta requiere una reserva nueva en estado CONFIRMADA.");
        }
        BigDecimal descuento = reserva.getEntrada() instanceof EntradaReducida reducida
                ? reducida.getPorcentajeDescuento() : null;
        if (reserva.getTipoEntrada() == TipoEntrada.REDUCIDA && descuento == null) {
            throw new PersistenciaException("Para guardar una entrada reducida indicá su porcentaje de descuento.");
        }
        try {
            Reserva guardada = crearReserva(reserva.getCliente().getId(), reserva.getFuncion().getIdFuncion(),
                    reserva.getNumeroAsiento(), reserva.getTipoEntrada(), descuento);
            reserva.setIdReserva(guardada.getIdReserva());
            reserva.setPrecioFinal(guardada.getPrecioFinal());
            reserva.setFechaReserva(guardada.getFechaReserva());
            reserva.setEstado(guardada.getEstado());
        } catch (ElementoNoEncontradoException | DatoInvalidoException | AsientoOcupadoException e) {
            throw new PersistenciaException(e.getMessage(), e);
        }
    }

    private Funcion verificarAsiento(Connection conexion, int idFuncion, int asiento)
            throws SQLException, ElementoNoEncontradoException, DatoInvalidoException {
        Funcion funcion = FuncionDAO.buscarPorId(conexion, idFuncion, true);
        if (funcion == null) {
            throw new ElementoNoEncontradoException("No existe la función " + idFuncion + ".");
        }
        if (!funcion.estaDisponible()) {
            throw new DatoInvalidoException("La función ya no está disponible.");
        }
        if (asiento < 1 || asiento > funcion.getSala().getCapacidad()) {
            throw new DatoInvalidoException("El asiento debe estar entre 1 y " + funcion.getSala().getCapacidad() + ".");
        }
        return funcion;
    }

    private Cliente buscarCliente(Connection conexion, int idCliente)
            throws SQLException, ElementoNoEncontradoException {
        String sql = "SELECT id_cliente, nombre, apellido, dni, email FROM cliente WHERE id_cliente = ? FOR SHARE";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, idCliente);
            try (ResultSet fila = stmt.executeQuery()) {
                if (!fila.next()) {
                    throw new ElementoNoEncontradoException("No existe el cliente " + idCliente + ".");
                }
                return mapearCliente(fila);
            }
        }
    }

    private int insertar(Connection conexion, Reserva reserva) throws SQLException, AsientoOcupadoException {
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
        } catch (SQLException e) {
            // El índice único es la comprobación definitiva, incluso con dos altas simultáneas.
            if (e.getErrorCode() == 1062) {
                AsientoOcupadoException ocupado = new AsientoOcupadoException("Ese asiento ya está reservado.");
                ocupado.initCause(e);
                throw ocupado;
            }
            throw e;
        }
    }

    public Reserva buscarReserva(int idReserva) throws PersistenciaException {
        List<Reserva> reservas = buscar(CONSULTA + " WHERE r.id_reserva = ?", idReserva);
        return reservas.isEmpty() ? null : reservas.getFirst();
    }

    public List<Reserva> listarPorCliente(int idCliente) throws PersistenciaException {
        return buscar(CONSULTA + " WHERE r.id_cliente = ? ORDER BY r.fecha_reserva DESC, r.id_reserva DESC", idCliente);
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

    public Set<Integer> asientosOcupados(int idFuncion) throws PersistenciaException {
        String sql = "SELECT asiento_confirmado FROM reserva WHERE id_funcion = ? AND asiento_confirmado IS NOT NULL";
        Set<Integer> asientos = new HashSet<>();
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
                            EstadoReserva.valueOf(filas.getString("estado_reserva"))));
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
