package dao;

import org.cine.dao.ConexionBD;

import java.sql.Connection;
import java.sql.SQLException;

final class ConexionDB {
    private static final ConexionBD CONFIGURACION = new ConexionBD();

    private ConexionDB() {
    }

    static Connection obtenerConexion() throws SQLException {
        return CONFIGURACION.obtenerConexion();
    }
}
