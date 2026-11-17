package dao;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import excepciones.PersistenciaException;
import org.cine.enums.EstadoReserva;
import org.cine.enums.TipoEntrada;
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
            ps.setInt(1, reserva.getCliente().getId());
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

    public Reserva buscarReserva(int idReserva) throws PersistenciaException {
        String sql = "SELECT * FROM reserva WHERE idReserva = ?";
        return extraerUnico(sql, idReserva);
    }

    public List<Reserva> listarPorCliente(int idCliente) throws PersistenciaException {
        String sql = "SELECT * FROM reserva WHERE idCliente = ?";
        return extraerLista(sql, idCliente);
    }


    public void cancelarReserva(int idReserva) throws PersistenciaException {
        String sql = "UPDATE reserva SET estado = 'CANCELADA' WHERE idReserva = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, idReserva);
            if (stmt.executeUpdate() == 0) {
                throw new PersistenciaException("No se encontró la reserva ID: " + idReserva + " o ya estaba cancelada.");
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al cancelar reserva: " + e.getMessage(), e);
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


    private Reserva extraerUnico(String sql, Object... parametros) throws PersistenciaException {

        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapearReserva(rs);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error en base de datos de reservas: " + e.getMessage(), e);
        }
        return null;
    }

    private List<Reserva> extraerLista(String sql, Object... parametros) throws PersistenciaException {

        List<Reserva> lista = new ArrayList<>();
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearReserva(rs));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar reservas: " + e.getMessage(), e);
        }
        return lista;
    }

    private Reserva mapearReserva(ResultSet rs) throws SQLException, PersistenciaException {

        int idReserva = rs.getInt("idReserva");
        org.cine.modelo.Cliente c = clienteDAO.buscarPorId(rs.getInt("idCliente"));
        org.cine.modelo.Funcion f = funcionDAO.buscarPorId(rs.getInt("idFuncion"));
        int asiento = rs.getInt("numeroAsiento");

        TipoEntrada tipoEntrada = TipoEntrada.valueOf(rs.getString("tipoEntrada"));

        BigDecimal precio = rs.getBigDecimal("precioFinal");
        LocalDateTime fecha = rs.getTimestamp("fechaReserva").toLocalDateTime();
        EstadoReserva estado = EstadoReserva.valueOf(rs.getString("estado"));

        return new Reserva(idReserva, c, f, asiento, tipoEntrada, precio, fecha, estado);
    }
}




