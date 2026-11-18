package org.cine.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Abre una conexión por operación. Los DAO son responsables de cerrarla. */
public class ConexionBD {
    public Connection obtenerConexion() throws SQLException {
        String url = System.getenv().getOrDefault("CINE_DB_URL", "jdbc:mysql://localhost:3306/cine");
        String usuario = System.getenv().getOrDefault("CINE_DB_USER", "root");
        String clave = System.getenv().getOrDefault("CINE_DB_PASSWORD", "");
        return DriverManager.getConnection(url, usuario, clave);
    }
}
