# CineGestor

Trabajo práctico de Programación II de la UTN. Aplicación de consola en Java 21 con Maven, JDBC y MySQL 8.0.

## Configurar la base

Ejecutá `sql/cine.sql` en MySQL Workbench. Crea el esquema `cine` con sus tablas y carga datos de ejemplo: 2 salas y 20 películas. Se puede ejecutar más de una vez sin duplicar los datos.

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

## Validaciones y menú

Los datos se validan en el momento en que se escriben y se vuelven a pedir si son inválidos (clase `Validador`, que lanza `DatoInvalidoException`):

- Nombre y apellido: solo letras, sin campos vacíos ni espacios en blanco.
- DNI: 7 u 8 números, sin letras. No se puede repetir.
- Email: con `@` y un punto después del arroba.
- Clave: mínimo 8 caracteres, con mayúscula, minúscula y número, sin espacios.
- Películas: el género y la clasificación se eligen de una lista (por número o por nombre, sin importar mayúsculas) y el título no se puede repetir.
- Salas: número y capacidad positivos, y el número de sala no se repite.
- Funciones: no se pueden programar en una fecha pasada ni superpuestas en la misma sala.
- Baja o modificación de algo que no existe: se muestra `ElementoNoEncontradoException`.

Cada paso del menú limpia la pantalla y al terminar una acción espera un Enter para poder leer el resultado.
