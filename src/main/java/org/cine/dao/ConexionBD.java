package org.cine.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Se encarga de abrir la conexion con la base de datos MySQL.
 * Los datos de conexion se pueden cambiar aca.
 */
public class ConexionBD {
    private static final String URL = "jdbc:mysql://localhost:3306/cine";
    private static final String USUARIO = "root";
    private static final String CLAVE = "";

    public Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}
