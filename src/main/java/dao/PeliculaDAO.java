package dao;

import java.sql.*;
import java.util.List;

import excepciones.PersistenciaException;
import org.cine.modelo.Pelicula;

public class PeliculaDAO {
    private Connection conexion;

    public PeliculaDAO() {
        this.conexion = ConexionDB.obtenerConexion();
    }

    public void guardarPelicula(Pelicula pelicula) throws PersistenciaException {
        String sql = "INSERT INTO pelicula (titulo, genero, duracion_minutos, clasificacion, activa) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, pelicula.getTitulo());
            ps.setString(2, pelicula.getGenero());
            ps.setInt(3, pelicula.getDuracionMinutos());
            ps.setString(4, pelicula.getClasificacion());
            ps.setBoolean(5, pelicula.estaActiva());
            ps.executeUpdate();


            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    pelicula.setIdPelicula(keys.getInt(1));
                }
            }
        } catch (SQLException e) {

            throw new PersistenciaException("Error al guardar la película: " + e.getMessage());
        }
    }

    public void actualizar(Pelicula pelicula) throws PersistenciaException {
        String sql = "UPDATE pelicula SET titulo = ?, genero = ?, duracionMinutos = ?, clasificacion = ?, activa = ? WHERE idPelicula = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, pelicula.getTitulo());
            stmt.setString(2, pelicula.getGenero());
            stmt.setInt(3, pelicula.getDuracionMinutos());
            stmt.setString(4, pelicula.getClasificacion());
            stmt.setBoolean(5, pelicula.estaActiva());
            stmt.setInt(6, pelicula.getIdPelicula());

            if (stmt.executeUpdate() == 0) {
                throw new PersistenciaException("No se encontró la película ID: " + pelicula.getIdPelicula());
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al actualizar película: " + e.getMessage(), e);
        }
    }


    public Pelicula buscarPorId(int idPelicula) throws PersistenciaException {
        String sql = "SELECT * FROM pelicula WHERE idPelicula = ?";
        return extraerUnico(sql, idPelicula);
    }

    public List<Pelicula> listar() throws PersistenciaException {
        String sql = "SELECT * FROM pelicula";
        return extraerLista(sql);
    }

    private Pelicula extraerUnico(String sql, Object... parametros) throws PersistenciaException {
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapearPelicula(rs);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error en base de datos de películas: " + e.getMessage(), e);
        }
        return null;
    }

    private List<Pelicula> extraerLista(String sql, Object... parametros) throws PersistenciaException {
        List<Pelicula> lista = new ArrayList<>();
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearPelicula(rs));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar películas: " + e.getMessage(), e);
        }
        return lista;
    }

    private Pelicula mapearPelicula(ResultSet rs) throws SQLException {
        Pelicula p = new Pelicula();
        p.setIdPelicula(rs.getInt("idPelicula"));
        p.setTitulo(rs.getString("titulo"));
        p.setGenero(rs.getString("genero"));
        p.setDuracionMinutos(rs.getInt("duracionMinutos"));
        p.setClasificacion(rs.getString("clasificacion"));
        p.setActiva(rs.getBoolean("activa"));
        return p;
    }


}
