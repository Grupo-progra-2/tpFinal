package dao;

import java.sql.*;
import org.cine.modelo.Sala;
import excepciones.PersistenciaException;

public class SalaDAO {

    private Connection conexion;

    public SalaDAO()throws PersistenciaException {
        this.conexion = ConexionDB.obtenerConexion();
    }

    public Sala buscarPorId(int idSala) throws PersistenciaException{
        String sql = "SELECT * FROM sala WHERE id_sala =?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)){
            ps.setInt(1, idSala);

            try(ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    Sala s = new Sala();
                    s.setIdSala(rs.getInt("id_sala"));
                    s.setHabilitada(rs.getBoolean("habilitada"));
                    s.setCapacidad(rs.getInt("capacidad"));
                    s.setNumero(rs.getInt("numero"));

                    return s;
                }

            }catch (SQLException e){
                throw new PersistenciaException("Error al buscar la sala: " + e.getMessage());
            }
        }
    }

}
