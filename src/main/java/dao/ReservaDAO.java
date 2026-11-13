package dao;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import excepciones.PersistenciaException;
import org.cine.enums.EstadoReserva;
import org.cine.modelo.Reserva;



public class ReservaDAO {

    //se declara funcion y cliente por que se necesita para buscar la reserva, ya que la reserva tiene una funcion y un cliente
    private Connection conexion;
    private ClienteDAO clienteDAO;
    private FuncionDAO funcionDAO;

    public ReservaDAO() throws PersistenciaException {
        this.conexion = ConexionDB.obtenerConexion();
        this.clienteDAO = new ClienteDAO();
        this.funcionDAO = new FuncionDAO();

    }

    public void guardarReserva(Reserva reserva) throws PersistenciaException {
        String sql = "INSERT INTO reserva (id_cliente, id_funcion, numero_asiento, precio_final, fecha_reserva, estado ) VALUES (?,?,?,?,?,?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) //se agrega Statement.RETURN_GENERATED_KEYS para que devuelva el id de la reserva que se acaba de insertar
        {
            ps.setInt(1, reserva.getCliente().getIdCliente());
            ps.setInt(2, reserva.getFuncion().getIdFuncion());
            ps.setInt(3, reserva.getNumeroAsiento());
            ps.setBigDecimal(4, reserva.getPrecioFinal()); //bigdecimal asegura el precio correcto
            ps.setTimestamp(5, Timestamp.valueOf(reserva.getFechaReserva())); // convierte LocalDateTime a Timestamp para guardarlo en la base de datos
            ps.setString(6, reserva.getEstado().name());

            ps.executeUpdate();

            try (ResultSet KEYS = ps.getGeneratedKeys()) {
                if (KEYS.next()) {
                    reserva.setIdReserva(KEYS.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al guardar la reserva" + e.getMessage(), e);
        }

    }

    public Reserva buscarReserva(int id_Reserva) throws PersistenciaException {
        String sql = "SELECT * FROM reserva WHERE id_reserva = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id_Reserva);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Reserva r = new Reserva();
                    r.setIdReserva(rs.getInt("id_reserva"));
                    r.setCliente(clienteDAO.buscarId(rs.getInt("id_cliente")));
                    r.setFuncion(funcionDAO.buscarId(rs.getInt("id_funcion")));
                    r.setNumeroAsiento(rs.getInt("numero_asiento"));
                    r.setPrecioFinal(rs.getBigDecimal("precio_final"));
                    r.setFechaReserva(rs.getTimestamp("fecha_reserva").toLocalDateTime());
                    r.setEstado(EstadoReserva.valueOf(rs.getString("estado")));
                    return r;
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar la reserva: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Reserva> listarReservas() throws PersistenciaException {
        List<Reserva> listadoReservas = new ArrayList<>();
        String sql = "SELECT * FROM reserva ORDER BY fecha_reserva DESC";

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Reserva r = new Reserva();
                r.setIdReserva(rs.getInt("id_reserva"));
                r.setCliente(clienteDAO.buscarId(rs.getInt("id_cliente")));
                r.setFuncion(funcionDAO.buscarId(rs.getInt("id_funcion")));
                r.setNumeroAsiento(rs.getInt("numero_asiento"));
                r.setPrecioFinal(rs.getBigDecimal("precio_final"));
                r.setFechaReserva(rs.getTimestamp("fecha_reserva").toLocalDateTime());
                r.setEstado(EstadoReserva.valueOf(rs.getString("estado")));
                listadoReservas.add(r);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar las reservas: " + e.getMessage(), e);
        }
        return listadoReservas;
    }

    public List<Reserva> listarPorCliente(int id_cliente) throws PersistenciaException {
        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT * FROM reserva WHERE id_cliente = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id_cliente);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reserva r = new Reserva();
                    r.setIdReserva(rs.getInt("id_reserva"));
                    r.setCliente(clienteDAO.buscarId(rs.getInt("id_cliente")));
                    r.setFuncion(funcionDAO.buscarId(rs.getInt("id_funcion")));
                    r.setNumeroAsiento(rs.getInt("numero_asiento"));
                    r.setPrecioFinal(rs.getBigDecimal("precioFinal"));
                    r.setFechaReserva(rs.getTimestamp("fecha_reserva").toLocalDateTime());
                    r.setEstado(EstadoReserva.valueOf(rs.getString("estado")));
                    lista.add(r);
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar las reservas del cliente: " + e.getMessage(), e);
        }
        return lista;
    }

    public void cancelarReserva(int id_reserva) throws PersistenciaException {
        String sql = "UPDATE reserva SET estado = CANCELADA WHERE id_reserva = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id_reserva);
            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                throw new PersistenciaException("No se encontró la reserva con ID: " + id_reserva);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al cancelar la reserva: " + e.getMessage(), e);
        }
    }

    //la idea es luego agregar metodos de pago, lo hare en la siguiente actualizacion, por ahora solo se puede cancelar y confirmar la reserva.
    public void confirmarReserva(int id_reserva) throws PersistenciaException {
        String sql = "UPDATE reserva SET estado = CONFIRMADA WHERE id_reserva = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id_reserva);
            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                throw new PersistenciaException("No se encontró la reserva con ID: " + id_reserva);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al confirmar la reserva: " + e.getMessage(), e);
        }
    }

    public boolean asientoOcupado(int id_funcion, int numero_asiento) throws PersistenciaException {
        String sql = "SELECT COUNT(*) FROM reserva WHERE id_funcion = ? AND numero_asiento = ? AND estado = 'CONFIRMADA'";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id_funcion);
            ps.setInt(2, numero_asiento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Si el conteo es mayor a 0, el asiento está ocupado
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al verificar si el asiento está ocupado: " + e.getMessage(), e);
        }
        return false; // Si no se encuentra ninguna reserva, el asiento no está ocupado
    }
}



