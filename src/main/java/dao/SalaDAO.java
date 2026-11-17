package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.cine.modelo.Sala;
import excepciones.PersistenciaException;

public class SalaDAO {

    private Connection conexion;

    public SalaDAO()throws PersistenciaException {
        this.conexion = ConexionDB.obtenerConexion();
    }

    public Sala buscarPorId(int idSala) throws PersistenciaException {
        String sql = "SELECT * FROM sala WHERE idSala = ?";
        return extraerUnico(sql, idSala);
    }

    public List<Sala> listar() throws PersistenciaException {
        String sql = "SELECT * FROM sala";
        return extraerLista(sql);
    }

    private Sala extraerUnico(String sql, Object... parametros) throws PersistenciaException {
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapearSala(rs);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error en base de datos de salas: " + e.getMessage(), e);
        }
        return null;
    }

    private List<Sala> extraerLista(String sql, Object... parametros) throws PersistenciaException {
        List<Sala> lista = new ArrayList<>();
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearSala(rs));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar salas: " + e.getMessage(), e);
        }
        return lista;
    }

    private Sala mapearSala(ResultSet rs) throws SQLException {
        Sala s = new Sala();
        s.setIdSala(rs.getInt("idSala"));
        s.setNumero(rs.getInt("numero"));
        s.setCapacidad(rs.getInt("capacidad"));
        s.setHabilitada(rs.getBoolean("habilitada"));
        return s;
    }

}
