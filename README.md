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

Recargá Maven con JDK 21 y ejecutá `org.cine.EscenariosCine` desde IntelliJ. Contiene las pruebas de descuentos y disponibilidad. Para incluir las pruebas JDBC, cargá el esquema en una instancia de prueba, configurá las variables de conexión a esa instancia y agregá `CINE_TEST_MYSQL=1`. Las pruebas JDBC crean sus propios registros de ejemplo.

El porcentaje de descuento se escribe de 0 a 100. Por ejemplo, 20 significa 20 %. Los asientos se numeran desde 1 hasta la capacidad actual de la sala. La base garantiza que solo una reserva confirmada puede ocupar cada asiento por función. Una cancelación conserva el historial y libera el asiento.

La opción 7 del menú informa las entradas confirmadas y la recaudación de una función. Las reservas canceladas quedan en el historial del cliente.

## Diseño

El [UML de CineGestor](docs/UML.md) muestra los modelos, la clase gestora y los DAO. El menú consulta a `GestorCine`. El gestor valida los datos, calcula precios y llama a los DAO para guardar y consultar MySQL.
