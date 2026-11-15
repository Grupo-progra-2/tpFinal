package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.cine.modelo.Cliente;
import excepciones.PersistenciaException;

public class ClienteDAO {

    private Connection conexion;

    public ClienteDAO() throws PersistenciaException {
        this.conexion = ConexionDB.obtenerConexion();
    }


    public void guardarCliente (Cliente cliente) throws PersistenciaException {
        String sql = "INSERT INTO cliente (nombre, apellido, dni, email, clave_acceso) VALUES (?,?,?,?,?,?)";

        try(PreparedStatement ps = conexion.prepareStatement (sql, Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getApellido());
            ps.setString(3, cliente.getDni());
            ps.setString(4, cliente.getEmail());
            ps.setString(5, cliente.getClaveAcceso());
            ps.executeUpdate();


            try (ResultSet key = ps.getGeneratedKeys()){
                if (key.next()){
                    cliente.setId(key.getInt(1));
                }
            }

            }catch (SQLException e){
                throw new PersistenciaException("Error al guardar el cliente: " + e.getMessage());

        }

    }



    public void actualizar (Cliente cliente) throws PersistenciaException {
        String sql = "UPDATE cliente SET nombre = ?, apellido = ?, dni = ?, email = ? , clave_acceso = ? WHERE idCliente = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getApellido());
            ps.setString(3, cliente.getDni());
            ps.setString(4, cliente.getEmail());
            ps.setString(5, cliente.getClaveAcceso());
            ps.setInt(6, cliente.getId());

            int filasActualizadas = ps.executeUpdate();
            if (filasActualizadas == 0) {
                throw new PersistenciaException("No se encontró el cliente con ID: " + cliente.getId());
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al actualizar el cliente: " + e.getMessage(), e);
        }
    }

    public List<Cliente> listar() throws PersistenciaException {
        String sql = "SELECT * FROM cliente";
        return extraerClientesLista(sql);
    }

    public Cliente buscarPorId(int id) throws PersistenciaException {
        String sql = "SELECT * FROM cliente WHERE idCliente = ?";
        return extraerClienteUnico(sql, id);
    }

    public Cliente buscarPorDni(String dni) throws PersistenciaException {
        String sql = "SELECT * FROM cliente WHERE dni = ?";
        return extraerClienteUnico(sql, dni);
    }

    public Cliente buscarPorDniYClave(String dni, String clave) throws PersistenciaException {
        String sql = "SELECT * FROM cliente WHERE dni = ? AND claveAcceso = ?";
        return extraerClienteUnico(sql, dni, clave);
    }

    private Cliente extraerClienteUnico(String sql, Object... parametros) throws PersistenciaException { //object... parametros permite pasar un número variable de argumentos
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapearCliente(rs);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error en base de datos: " + e.getMessage(), e);
        }
        return null;
    }

    private List<Cliente> extraerClientesLista(String sql, Object... parametros) throws PersistenciaException {
        List<Cliente> lista = new ArrayList<>();
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCliente(rs));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar clientes: " + e.getMessage(), e);
        }
        return lista;
    }

    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("idCliente"));
        c.setNombre(rs.getString("nombre"));
        c.setApellido(rs.getString("apellido"));
        c.setDni(rs.getString("dni"));
        c.setEmail(rs.getString("email"));
        c.setClaveAcceso(rs.getString("claveAcceso"));
        return c;
    }


}





