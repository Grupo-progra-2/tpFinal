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

    public Pelicula buscarPorId(int idPelicula) throws PersistenciaException {
        String sql = "SELECT * FROM pelicula WHERE id_pelicula =?" ;

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idPelicula);

            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    Pelicula p = new Pelicula();
                    p.setIdPelicula(rs.getInt("id_pelicula"));
                    p.setTitulo(rs.getString("titulo"));
                    p.setGenero(rs.getString("genero"));
                    p.setDuracionMinutos(rs.getInt("duracion_minutos"));
                    p.setClasificacion(rs.getString("clasificacion"));
                    p.setActiva(rs.getBoolean("activa"));
                    return p;
                }
            }
        }catch (SQLException e){
            throw new PersistenciaException("Error al buscar la película: " + e.getMessage());
        }
        return null;
    }

    public List<Pelicula> listar()throws PersistenciaException{
        List <Pelicula> listadoPeliculas = new java.util.ArrayList<>();
        String sql = "SELECT * FROM pelicula";

        try(PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){
            while(rs.next()){
                Pelicula p = new Pelicula();
                p.setIdPelicula(rs.getInt("id_pelicula"));
                p.setTitulo(rs.getString("titulo"));
                p.setGenero(rs.getString("genero"));
                p.setDuracionMinutos(rs.getInt("duracion_minutos"));
                p.setClasificacion(rs.getString("clasificacion"));
                p.setActiva(rs.getBoolean("activa"));
                listadoPeliculas.add(p);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar las películas: " + e.getMessage());
        }
        return listadoPeliculas;
    }


}
