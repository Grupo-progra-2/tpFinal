# UML de CineGestor

## Modelos

```mermaid
classDiagram
    direction TB
    class Persona {
        <<abstract>>
        -Integer id
        -String nombre
        -String dni
    }
    class Cliente
    class Empleado
    class Pelicula {
        -int duracionMinutos
        -boolean activa
    }
    class Sala {
        -int capacidad
        -boolean habilitada
    }
    class Funcion {
        -LocalDateTime fechaHora
        -EstadoFuncion estado
        +estaDisponible() boolean
        +calcularFin() LocalDateTime
    }
    class Reserva {
        -int numeroAsiento
        -BigDecimal precioFinal
        -EstadoReserva estado
        +cancelar()
    }
    class Entrada {
        <<abstract>>
        #BigDecimal precioBase
        +calcularPrecio() BigDecimal
    }
    class EntradaGeneral
    class EntradaReducida {
        -BigDecimal porcentajeDescuento
    }
    class Descontable {
        <<interface>>
        +calcularDescuento() BigDecimal
    }
    Persona <|-- Cliente
    Persona <|-- Empleado
    Entrada <|-- EntradaGeneral
    Entrada <|-- EntradaReducida
    Descontable <|.. EntradaReducida
    Funcion --> "1" Pelicula
    Funcion --> "1" Sala
    Reserva --> "1" Funcion
    Reserva --> "1" Cliente
    Reserva --> "0..1" Entrada
```

## Menú, gestor y persistencia

```mermaid
classDiagram
    direction TB
    class Main
    class MenuReservas
    class GestorCine {
        +programarFuncion() Funcion
        +crearReserva() Reserva
        +cancelarReserva()
        +informeFuncion() String
    }
    class Repositorio~T~ {
        -List~T~ elementos
        +agregar(T elemento)
        +listar() List~T~
    }
    class FuncionDAO
    class ReservaDAO
    class PeliculaDAO
    class SalaDAO
    class ClienteDAO
    class ConexionDB
    Main --> MenuReservas
    MenuReservas --> GestorCine
    GestorCine ..> Repositorio : asientos libres
    GestorCine --> FuncionDAO
    GestorCine --> ReservaDAO
    GestorCine --> PeliculaDAO
    GestorCine --> SalaDAO
    GestorCine --> ClienteDAO
    FuncionDAO ..> ConexionDB
    ReservaDAO ..> ConexionDB
    PeliculaDAO ..> ConexionDB
    SalaDAO ..> ConexionDB
    ClienteDAO ..> ConexionDB
```

Al reservar, el gestor guarda una referencia de tipo `Entrada`. Java elige si usa el cálculo general o el reducido. La base conserva el tipo y el precio final de la reserva histórica.

`Repositorio<Integer>` reúne los números de asientos libres. Entre las excepciones que atiende el menú están `DatoInvalidoException`, `HorarioSuperpuestoException`, `AsientoOcupadoException` y `PersistenciaException`.

Al aplicar el parche de Lauti, `Main` también abre `MenuAdministracion`, que usa los DAO de películas, salas y clientes.
