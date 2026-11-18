package org.cine;

import dao.ClienteDAO;
import dao.PeliculaDAO;
import dao.ReservaDAO;
import dao.SalaDAO;
import excepciones.DatoInvalidoException;
import excepciones.ElementoNoEncontradoException;
import excepciones.PersistenciaException;
import org.cine.modelo.Cliente;
import org.cine.modelo.Pelicula;
import org.cine.modelo.Sala;

final class MenuAdministracion {
    private final Consola consola;
    private final PeliculaDAO peliculas = new PeliculaDAO();
    private final SalaDAO salas = new SalaDAO();
    private final ClienteDAO clientes = new ClienteDAO();
    private final ReservaDAO reservas = new ReservaDAO();

    MenuAdministracion(Consola consola) {
        this.consola = consola;
    }

    void ejecutar() {
        while (true) {
            consola.limpiar();
            consola.encabezado("ADMINISTRACIÓN");
            System.out.println("""
                    1. Películas
                    2. Salas
                    3. Clientes
                    0. Volver
                    """);
            try {
                switch (consola.entero("Opción: ")) {
                    case 0 -> { return; }
                    case 1 -> menuPeliculas();
                    case 2 -> menuSalas();
                    case 3 -> menuClientes();
                    default -> System.out.println("Opción inválida.");
                }
            } catch (PersistenciaException | DatoInvalidoException | ElementoNoEncontradoException e) {
                System.out.println("Error: " + e.getMessage());
            }
            consola.pausar();
        }
    }

    // ---------------- PELICULAS ----------------

    private void menuPeliculas() throws PersistenciaException, DatoInvalidoException, ElementoNoEncontradoException {
        consola.limpiar();
        consola.encabezado("PELÍCULAS");
        System.out.println("""
                1. Agregar película
                2. Listar películas
                3. Modificar película
                4. Desactivar película
                """);
        switch (consola.entero("Opción: ")) {
            case 1 -> altaPelicula();
            case 2 -> listarPeliculas();
            case 3 -> modificarPelicula();
            case 4 -> desactivarPelicula();
            default -> System.out.println("Opción inválida.");
        }
    }

    // Pide el titulo hasta que no este vacio y no se repita (sin importar mayusculas)
    private String tituloNuevo(String pregunta, int idActual) throws PersistenciaException {
        while (true) {
            String titulo = consola.textoObligatorio(pregunta, "título");
            boolean repetido = false;
            for (Pelicula p : peliculas.listar()) {
                if (p.getTitulo().equalsIgnoreCase(titulo) && p.getIdPelicula() != idActual) {
                    repetido = true;
                }
            }
            if (!repetido) {
                return titulo;
            }
            System.out.println("Error: ya existe una película con ese título.");
        }
    }

    private Pelicula buscarPelicula(int id) throws PersistenciaException, ElementoNoEncontradoException {
        Pelicula pelicula = peliculas.buscarPorId(id);
        if (pelicula == null) {
            throw new ElementoNoEncontradoException("No existe ninguna película con el ID " + id + ".");
        }
        return pelicula;
    }

    private void altaPelicula() throws PersistenciaException {
        String titulo = tituloNuevo("Título: ", 0);
        String genero = consola.genero();
        int duracion = consola.numero("Duración en minutos: ", "duración", 1, 600);
        String clasificacion = consola.clasificacion();
        Pelicula pelicula = new Pelicula(null, titulo, genero, duracion, clasificacion, true);
        peliculas.guardarPelicula(pelicula);
        System.out.println("Película guardada con ID " + pelicula.getIdPelicula());
    }

    private void listarPeliculas() throws PersistenciaException {
        if (peliculas.listar().isEmpty()) {
            System.out.println("No hay películas cargadas.");
        }
        for (Pelicula pelicula : peliculas.listar()) {
            System.out.println(pelicula.getIdPelicula() + " - " + pelicula);
        }
    }

    private void modificarPelicula() throws PersistenciaException, ElementoNoEncontradoException {
        Pelicula pelicula = buscarPelicula(consola.entero("ID de película: "));
        System.out.println("Actual: " + pelicula);
        String titulo = tituloNuevo("Nuevo título: ", pelicula.getIdPelicula());
        String genero = consola.genero();
        int duracion = consola.numero("Nueva duración: ", "duración", 1, 600);
        String clasificacion = consola.clasificacion();
        pelicula.setTitulo(titulo);
        pelicula.setGenero(genero);
        pelicula.setDuracionMinutos(duracion);
        pelicula.setClasificacion(clasificacion);
        peliculas.actualizar(pelicula);
        System.out.println("Película modificada.");
    }

    private void desactivarPelicula() throws PersistenciaException, DatoInvalidoException, ElementoNoEncontradoException {
        Pelicula pelicula = buscarPelicula(consola.entero("ID de película: "));
        if (!pelicula.estaActiva()) {
            throw new DatoInvalidoException("La película ya está desactivada.");
        }
        pelicula.setActiva(false);
        peliculas.actualizar(pelicula);
        System.out.println("Película desactivada.");
    }

    // ---------------- SALAS ----------------

    private void menuSalas() throws PersistenciaException, ElementoNoEncontradoException {
        consola.limpiar();
        consola.encabezado("SALAS");
        System.out.println("""
                1. Agregar sala
                2. Listar salas
                3. Modificar sala
                4. Habilitar / deshabilitar sala
                """);
        switch (consola.entero("Opción: ")) {
            case 1 -> altaSala();
            case 2 -> listarSalas();
            case 3 -> modificarSala();
            case 4 -> cambiarEstadoSala();
            default -> System.out.println("Opción inválida.");
        }
    }

    private Sala buscarSala(int id) throws PersistenciaException, ElementoNoEncontradoException {
        Sala sala = salas.buscarPorId(id);
        if (sala == null) {
            throw new ElementoNoEncontradoException("No existe ninguna sala con el ID " + id + ".");
        }
        return sala;
    }

    // Pide el numero de sala (positivo) hasta que no este repetido
    private int numeroSalaNuevo(String pregunta, int idActual) throws PersistenciaException {
        while (true) {
            int numero = consola.numero(pregunta, "número de sala", 1, 99);
            boolean repetido = false;
            for (Sala s : salas.listar()) {
                if (s.getNumero() == numero && s.getIdSala() != idActual) {
                    repetido = true;
                }
            }
            if (!repetido) {
                return numero;
            }
            System.out.println("Error: ya existe una sala con ese número.");
        }
    }

    private void altaSala() throws PersistenciaException {
        int numero = numeroSalaNuevo("Número de sala: ", 0);
        int capacidad = consola.numero("Capacidad: ", "capacidad", 1, 500);
        Sala sala = new Sala(0, numero, capacidad, true);
        salas.guardar(sala);
        System.out.println("Sala guardada con ID " + sala.getIdSala());
    }

    private void listarSalas() throws PersistenciaException {
        if (salas.listar().isEmpty()) {
            System.out.println("No hay salas cargadas.");
        }
        for (Sala sala : salas.listar()) {
            System.out.println(sala.getIdSala() + " - " + sala);
        }
    }

    private void modificarSala() throws PersistenciaException, ElementoNoEncontradoException {
        Sala sala = buscarSala(consola.entero("ID de sala: "));
        System.out.println("Actual: " + sala);
        int numero = numeroSalaNuevo("Nuevo número: ", sala.getIdSala());
        int capacidad = consola.numero("Nueva capacidad: ", "capacidad", 1, 500);
        sala.setNumero(numero);
        sala.setCapacidad(capacidad);
        salas.actualizar(sala);
        System.out.println("Sala modificada.");
    }

    private void cambiarEstadoSala() throws PersistenciaException, ElementoNoEncontradoException {
        Sala sala = buscarSala(consola.entero("ID de sala: "));
        sala.setHabilitada(!sala.estaHabilitada());
        salas.actualizar(sala);
        System.out.println("Nuevo estado: " + (sala.estaHabilitada() ? "habilitada" : "deshabilitada"));
    }

    // ---------------- CLIENTES ----------------

    private void menuClientes() throws PersistenciaException, DatoInvalidoException, ElementoNoEncontradoException {
        consola.limpiar();
        consola.encabezado("CLIENTES");
        System.out.println("""
                1. Registrar cliente
                2. Listar clientes
                3. Buscar cliente por DNI
                4. Modificar cliente
                5. Dar de baja cliente
                """);
        switch (consola.entero("Opción: ")) {
            case 1 -> altaCliente();
            case 2 -> listarClientes();
            case 3 -> buscarCliente();
            case 4 -> modificarCliente();
            case 5 -> eliminarCliente();
            default -> System.out.println("Opción inválida.");
        }
    }

    private Cliente buscarCliente(int id) throws PersistenciaException, ElementoNoEncontradoException {
        Cliente cliente = clientes.buscarPorId(id);
        if (cliente == null) {
            throw new ElementoNoEncontradoException("No existe ningún cliente con el ID " + id + ".");
        }
        return cliente;
    }

    // Pide el DNI hasta que sea valido y no lo tenga otro cliente
    private String dniNuevo(String pregunta, int idActual) throws PersistenciaException {
        while (true) {
            String dni = consola.dni(pregunta);
            Cliente existente = clientes.buscarPorDni(dni);
            if (existente == null || existente.getId() == idActual) {
                return dni;
            }
            System.out.println("Error: ya existe un cliente con ese DNI.");
        }
    }

    private void altaCliente() throws PersistenciaException {
        String nombre = consola.nombre("Nombre: ", "nombre");
        String apellido = consola.nombre("Apellido: ", "apellido");
        String dni = dniNuevo("DNI: ", 0);
        String email = consola.email("Email: ");
        String clave = consola.clave("Clave de acceso: ");
        Cliente cliente = new Cliente(null, nombre, apellido, dni, email, clave);
        clientes.guardarCliente(cliente);
        System.out.println("Cliente registrado con ID " + cliente.getId());
    }

    private void listarClientes() throws PersistenciaException {
        if (clientes.listar().isEmpty()) {
            System.out.println("No hay clientes cargados.");
        }
        for (Cliente cliente : clientes.listar()) {
            System.out.println(cliente.getId() + " - " + cliente);
        }
    }

    private void buscarCliente() throws PersistenciaException, ElementoNoEncontradoException {
        Cliente cliente = clientes.buscarPorDni(consola.dni("DNI a buscar: "));
        if (cliente == null) {
            throw new ElementoNoEncontradoException("No existe un cliente con ese DNI.");
        }
        System.out.println(cliente);
    }

    private void modificarCliente() throws PersistenciaException, ElementoNoEncontradoException {
        Cliente cliente = buscarCliente(consola.entero("ID de cliente: "));
        System.out.println("Actual: " + cliente);
        String nombre = consola.nombre("Nuevo nombre: ", "nombre");
        String apellido = consola.nombre("Nuevo apellido: ", "apellido");
        String dni = dniNuevo("Nuevo DNI: ", cliente.getId());
        String email = consola.email("Nuevo email: ");
        cliente.setNombre(nombre);
        cliente.setApellido(apellido);
        cliente.setDni(dni);
        cliente.setEmail(email);
        clientes.actualizar(cliente);
        System.out.println("Cliente modificado.");
    }

    // Da de baja a un cliente. Si no existe o tiene reservas se muestra el error
    private void eliminarCliente() throws PersistenciaException, DatoInvalidoException, ElementoNoEncontradoException {
        Cliente cliente = buscarCliente(consola.entero("ID de cliente a dar de baja: "));
        if (!reservas.listarPorCliente(cliente.getId()).isEmpty()) {
            throw new DatoInvalidoException("El cliente tiene reservas, no se puede dar de baja.");
        }
        System.out.println(cliente);
        String respuesta = consola.texto("¿Seguro que querés darlo de baja? (s/n): ");
        if (respuesta.equalsIgnoreCase("s")) {
            clientes.eliminar(cliente.getId());
            System.out.println("Cliente dado de baja.");
        } else {
            System.out.println("No se dio de baja al cliente.");
        }
    }
}
