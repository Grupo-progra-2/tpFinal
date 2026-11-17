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


        private PeliculaDAO peliculaDAO;
        private SalaDAO salaDAO;

        public FuncionDAO() throws PersistenciaException {
            this.peliculaDAO = new PeliculaDAO();
            this.salaDAO = new SalaDAO();
        }

        public void guardar(Funcion funcion) throws PersistenciaException {
            String sql = "INSERT INTO funcion (id_pelicula, id_sala, fecha_hora, precio_base, estado) VALUES (?, ?, ?, ?, ?)";
            try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, funcion.getPelicula().getIdPelicula());
                stmt.setInt(2, funcion.getSala().getIdSala());
                stmt.setTimestamp(3, Timestamp.valueOf(funcion.getFechaHora()));
                stmt.setBigDecimal(4, funcion.getPrecioBase());
                stmt.setString(5, funcion.getEstado().name());
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {

                        funcion.setIdFuncion(keys.getInt(1));
                    }
                }
            } catch (SQLException e) {
                throw new PersistenciaException("Error al guardar función: " + e.getMessage(), e);
            }
        }

        public Funcion buscarPorId(int idFuncion) throws PersistenciaException {
            String sql = "SELECT * FROM funcion WHERE id_funcion = ?";
            return extraerUnico(sql, idFuncion);
        }

        public List<Funcion> listarPorPelicula(int idPelicula) throws PersistenciaException {
            String sql = "SELECT * FROM funcion WHERE id_pelicula = ? AND estado != 'CANCELADA' ORDER BY fecha_hora ASC";
            return extraerLista(sql, idPelicula);
        }

        private Funcion extraerUnico(String sql, Object... parametros) throws PersistenciaException {
            try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
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
            try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
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
            int id = rs.getInt("id_funcion");
            org.cine.modelo.Pelicula p = peliculaDAO.buscarPorId(rs.getInt("id_pelicula"));
            org.cine.modelo.Sala s = salaDAO.buscarPorId(rs.getInt("id_sala"));
            LocalDateTime fechaHora = rs.getTimestamp("fecha_hora").toLocalDateTime();
            BigDecimal precio = rs.getBigDecimal("precio_base");
            EstadoFuncion estado = EstadoFuncion.valueOf(rs.getString("estado"));

            return new Funcion(id, p, s, fechaHora, precio, estado);
        }

}

