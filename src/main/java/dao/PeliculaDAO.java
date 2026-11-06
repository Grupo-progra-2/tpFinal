package dao;

import java.sql.*;

import org.cine.modelo.Pelicula;

public class PeliculaDAO {
    private Connection conexion;

    public PeliculaDAO() {
        this.conexion = ConexionDB.obtenerConexion();
    }

    public void guardarPelicula(Pelicula pelicula) {
        String sql = "INSERT INTO peliculas (titulo, genero, duracion_minutos, clasificacion, activa) VALUES (?, ?, ?, ?, ?)";
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
            //
        }
    }


}
