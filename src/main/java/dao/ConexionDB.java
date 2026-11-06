package dao;

import excepciones.PersistenciaException;

import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Connection;


 class ConexionDB {  //le saco el public para que no se pueda instanciar desde otra clase, solo se puede acceder a traves del metodo obtenerConexion() que es static y devuelve la conexion a la base de datos

    private static final String URL = "jdbc:mysql://localhost:3306/tienda";
    private static final String USER = "";
    private static final String PASS = "";

    private static Connection conexion;

    private ConexionDB() {
        // Constructor privado para evitar instanciación
    }

     static Connection obtenerConexion() { //aca tambien le saco el public para que no se pueda acceder desde otra clase externa al paquete DAO
        try {
            if (conexion == null || conexion.isClosed()) {      //se reutiliza si esta abierta, si no se crea una nueva conexion
                conexion = DriverManager.getConnection(URL, USER, PASS);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error de conexión: " + e.getMessage());
        }
        return conexion;
    }


}
