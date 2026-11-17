# CineGestor

Trabajo práctico de Programación II de la UTN. Aplicación de consola en Java 21 con Maven, JDBC y MySQL 8.0.

## Configurar la base

Para una instalación nueva, ejecutá `sql/cine.sql` en MySQL Workbench. Crea el esquema `cine` sin borrar tablas. Si ya usabas el esquema original de `feature/modelos`, hacé una copia de seguridad, ejecutá por separado el SELECT de diagnóstico de `sql/actualizar-cine.sql` y revisá que no devuelva reservas confirmadas duplicadas. Aplicá sus ALTER una sola vez. Ejecutá `sql/cine.sql` también para crear las tablas que falten. Una instalación con un esquema distinto requiere revisar primero su estructura.

Configurá estas variables en la configuración de ejecución de IntelliJ, en **Run → Edit Configurations → Environment variables**, o en tu terminal.

| Variable | Predeterminado |
| --- | --- |
| `CINE_DB_URL` | `jdbc:mysql://localhost:3306/cine` |
| `CINE_DB_USER` | `root` |
| `CINE_DB_PASSWORD` | Vacía |

Usá el usuario y la contraseña de MySQL que correspondan a tu entorno. Cargá al menos una película activa, una sala habilitada y un cliente para recorrer el menú de reservas. Ejecutá `org.cine.Main` desde IntelliJ.

## Pruebas

Ejecutá `mvn test` con JDK 21. Para incluir las pruebas JDBC, cargá el esquema en una instancia descartable, configurá las variables de conexión a esa instancia y agregá `CINE_TEST_MYSQL=1`. La prueba de integración crea sus propios registros de ejemplo. Ejecutala únicamente contra una base de pruebas.

El porcentaje de descuento se escribe de 0 a 100. Por ejemplo, 20 significa 20 %. Los asientos se numeran desde 1 hasta la capacidad actual de la sala. La base garantiza que solo una reserva confirmada puede ocupar cada asiento por función. Una cancelación conserva el historial y libera el asiento.
