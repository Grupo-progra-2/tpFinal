package dao;

import java.sql.*;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import org.cine.modelo.Empleado;
import excepciones.PersistenciaException;


public class EmpleadoDAO {







    public void guardarEmpleado(Empleado empleado) throws PersistenciaException {
        // CORREGIDO: INSERT INTO (bien escrito) y todas las columnas alineadas con la BD
        String sql = "INSERT INTO empleado (nombre, apellido, dni, email, legajo, clave_acceso, es_administrador, dia_libre) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, empleado.getNombre());
            ps.setString(2, empleado.getApellido());
            ps.setString(3, empleado.getDni());
            ps.setString(4, empleado.getEmail());
            ps.setString(5, empleado.getLegajo());
            ps.setString(6, empleado.getClaveAcceso());
            ps.setBoolean(7, empleado.esAdmin());
            ps.setString(8, empleado.getDiaLibre() != null ? empleado.getDiaLibre().name() : null);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    empleado.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al guardar el empleado: " + e.getMessage());
        }
    }

    public Empleado buscarPorId(int id) throws PersistenciaException { ///Se utiliza internamente por el sistema para relacionar tablas usando la clave primaria numérica. El usuario nunca ingresa su ID a mano.
        String sql = "SELECT * FROM empleado WHERE id_empleado = ?";
        return extraerEmpleadoUnico(sql, id);
    }

    public Empleado buscarPorDni(String dni) throws PersistenciaException { ///Se utiliza para búsquedas directas cuando ya conocemos la identidad del usuario
        String sql = "SELECT * FROM empleado WHERE dni = ?";
        return extraerEmpleadoUnico(sql, dni);
    }

    public Empleado buscarPorDniYClave(String dni, String clave) throws PersistenciaException { ///exclusivo de Autenticación (Login). Valida de manera estricta que ambos datos coincidan en la base de datos para garantizar la seguridad del acceso.
        String sql = "SELECT * FROM empleado WHERE dni = ? AND clave_acceso = ?";
        return extraerEmpleadoUnico(sql, dni, clave);
    }


    private Empleado extraerEmpleadoUnico(String sql, Object... parametros) throws PersistenciaException { ///Para evitar repetir código de conexión y manejo de excepciones en cada consulta,
        ///El método recorre ese arreglo de forma dinámica con un bucle for y va rellenando los signos de interrogación ? (stmt.setObject) del PreparedStatement de manera segura, previniendo ataques de Inyección SQL.
        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearEmpleado(rs);
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error en base de datos del empleado: " + e.getMessage(), e);
        }
        return null;
    }
    private Empleado mapearEmpleado(ResultSet rs) throws SQLException { ///Recibe un ResultSet (la fila que devuelve MySQL) y extraer cada columna por su nombre (rs.getString(...)),
        ///instanciando un objeto de la clase modelo y cargándole todos sus atributos. (En cliente es lo mismo)
        Empleado e = new Empleado();

        e.setId(rs.getInt("id_empleado"));
        e.setNombre(rs.getString("nombre"));
        e.setApellido(rs.getString("apellido"));
        e.setDni(rs.getString("dni"));
        e.setEmail(rs.getString("email"));
        e.setClaveAcceso(rs.getString("clave_acceso"));

        // 2. Atributos exclusivos de Empleado
        e.setLegajo(rs.getString("legajo"));
        e.setEsAdmin(rs.getBoolean("es_administrador"));

        // Manejo del Día Libre (guardado como String o número en la BD, ej: "MONDAY" o 1-7)
        String diaStr = rs.getString("dia_libre");
        if (diaStr != null) {
            e.setDiaLibre(DayOfWeek.valueOf(diaStr.toUpperCase()));
        }

        return e;
    }




































































}
