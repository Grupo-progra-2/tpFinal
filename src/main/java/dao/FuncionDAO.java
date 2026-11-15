package dao;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.cine.modelo.Funcion;
import org.cine.enums.EstadoFuncion;
import excepciones.PersistenciaException;


public class FuncionDAO {


        private Connection conexion;
        private PeliculaDAO peliculaDAO;
        private SalaDAO salaDAO;

        public FuncionDAO() throws PersistenciaException {
            this.conexion = ConexionDB.obtenerConexion();
            this.peliculaDAO = new PeliculaDAO();
            this.salaDAO = new SalaDAO();
        }

        public void guardar(Funcion funcion) throws PersistenciaException {
            String sql = "INSERT INTO funcion (idPelicula, idSala, fechaHora, precioBase, estado) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, funcion.getPelicula().getIdPelicula());
                stmt.setInt(2, funcion.getSala().getIdSala());
                stmt.setTimestamp(3, Timestamp.valueOf(funcion.getFechaHora()));
                stmt.setBigDecimal(4, funcion.getPrecioBase());
                stmt.setString(5, funcion.getEstado().name());
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        // Si agregaste un setIdFuncion en tu clase Funcion, descomentá la línea de abajo:
                        // funcion.setIdFuncion(keys.getInt(1));
                    }
                }
            } catch (SQLException e) {
                throw new PersistenciaException("Error al guardar función: " + e.getMessage(), e);
            }
        }

        public Funcion buscarPorId(int idFuncion) throws PersistenciaException {
            String sql = "SELECT * FROM funcion WHERE idFuncion = ?";
            return extraerUnico(sql, idFuncion);
        }

        public List<Funcion> listarPorPelicula(int idPelicula) throws PersistenciaException {
            String sql = "SELECT * FROM funcion WHERE idPelicula = ? AND estado != 'CANCELADA' ORDER BY fechaHora ASC";
            return extraerLista(sql, idPelicula);
        }

        private Funcion extraerUnico(String sql, Object... parametros) throws PersistenciaException {
            try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
                for (int i = 0; i < parametros.length; i++) {
                    stmt.setObject(i + 1, parametros[i]);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) return mapearFuncion(rs);
                }
            } catch (SQLException e) {
                throw new PersistenciaException("Error en base de datos de funciones: " + e.getMessage(), e);
            }
            return null;
        }

        private List<Funcion> extraerLista(String sql, Object... parametros) throws PersistenciaException {
            List<Funcion> lista = new ArrayList<>();
            try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
                for (int i = 0; i < parametros.length; i++) {
                    stmt.setObject(i + 1, parametros[i]);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapearFuncion(rs));
                    }
                }
            } catch (SQLException e) {
                throw new PersistenciaException("Error al listar funciones: " + e.getMessage(), e);
            }
            return lista;

        }

        private Funcion mapearFuncion(ResultSet rs) throws SQLException, PersistenciaException {
            int id = rs.getInt("idFuncion");
            org.cine.modelo.Pelicula p = peliculaDAO.buscarPorId(rs.getInt("idPelicula"));
            org.cine.modelo.Sala s = salaDAO.buscarPorId(rs.getInt("idSala"));
            LocalDateTime fechaHora = rs.getTimestamp("fechaHora").toLocalDateTime();
            BigDecimal precio = rs.getBigDecimal("precioBase");
            EstadoFuncion estado = EstadoFuncion.valueOf(rs.getString("estado"));

            return new Funcion(id, p, s, fechaHora, precio, estado);
        }

}

