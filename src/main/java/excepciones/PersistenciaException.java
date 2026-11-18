package excepciones;

//este error se lanza cuando hay un error en la base de datos, por ejemplo si la tabla no existe, o si hay un error de sintaxis en la consulta
//SQL, o si hay un problema de conexión a la base de datos.
public class PersistenciaException extends Exception {
    public PersistenciaException(String message) {
        super(message);
    }

    public PersistenciaException(String message, Throwable cause) {
        super(message, cause);
    }
}
