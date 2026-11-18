package org.cine;

import dao.PeliculaDAO;
import dao.SalaDAO;
import dao.ClienteDAO;
import excepciones.PersistenciaException;
import org.cine.modelo.Pelicula;
import org.cine.modelo.Sala;
import org.cine.modelo.Cliente;
import excepciones.DatoInvalidoException;

final class MenuAdministracion {
    private final Consola consola;
    private final PeliculaDAO peliculas = new PeliculaDAO();
    private final SalaDAO salas = new SalaDAO();
    private final ClienteDAO clientes = new ClienteDAO();

    MenuAdministracion(Consola consola) {
        this.consola = consola;
    }

    void ejecutar() {
        while (true) {
            System.out.println("""
                    ADMINISTRACIÓN

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
            } catch (PersistenciaException | IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private String obligatorio(String pregunta) {
        String valor = consola.texto(pregunta);
        if (valor.isEmpty()) throw new IllegalArgumentException("El campo no puede quedar vacío.");
        return valor;
    }


    private String emailValido(String pregunta) {
        while (true) {
            try {
                String email = consola.texto(pregunta);

                if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                    throw new DatoInvalidoException(
                            "Email inválido. Ejemplo: usuario@gmail.com"
                    );
                }

                return email;

            } catch (DatoInvalidoException e) {
                System.out.println(e.getMessage());
            }
        }
    }



    private String dniValido(String pregunta) {
        while (true) {
            try {
                String dni = consola.texto(pregunta);

                if (!dni.matches("\\d{7,8}")) {
                    throw new DatoInvalidoException(
                            "DNI inválido. Ingresá 7 u 8 números sin puntos ni letras."
                    );
                }

                return dni;

            } catch (DatoInvalidoException e) {
                System.out.println(e.getMessage());
            }
        }
    }



    private String nombreValido(String pregunta) {
        while (true) {
            try {
                String valor = consola.texto(pregunta);

                if (valor.isEmpty() ||
                        !valor.matches("[\\p{L}]+(?:[ '-][\\p{L}]+)*")) {

                    throw new DatoInvalidoException(
                            "Nombre o apellido inválido. Ingresá solamente letras."
                    );
                }

                return valor;

            } catch (DatoInvalidoException e) {
                System.out.println(e.getMessage());
            }
        }
    }





    private void menuPeliculas() throws PersistenciaException {
        System.out.println("""
                PELÍCULAS
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

    private void altaPelicula() throws PersistenciaException {
        String titulo = obligatorio("Título: ");
        String genero = obligatorio("Género: ");
        int duracion = consola.entero("Duración en minutos: ");
        String clasificacion = obligatorio("Clasificación: ");
        if (duracion <= 0) throw new IllegalArgumentException("La duración debe ser positiva.");
        Pelicula pelicula = new Pelicula(null, titulo, genero, duracion, clasificacion, true);
        peliculas.guardarPelicula(pelicula);
        System.out.println("Película guardada con ID " + pelicula.getIdPelicula());
    }

    private void listarPeliculas() throws PersistenciaException {
        for (Pelicula pelicula : peliculas.listar()) {
            System.out.println(pelicula.getIdPelicula() + " - " + pelicula);
        }
    }

    private void modificarPelicula() throws PersistenciaException {
        int id = consola.entero("ID de película: ");
        Pelicula pelicula = peliculas.buscarPorId(id);
        if (pelicula == null) {
            System.out.println("No existe esa película.");
            return;
        }
        System.out.println("Actual: " + pelicula);
        String titulo = obligatorio("Nuevo título: ");
        String genero = obligatorio("Nuevo género: ");
        int duracion = consola.entero("Nueva duración: ");
        String clasificacion = obligatorio("Nueva clasificación: ");
        if (duracion <= 0) throw new IllegalArgumentException("La duración debe ser positiva.");
        pelicula.setTitulo(titulo);
        pelicula.setGenero(genero);
        pelicula.setDuracionMinutos(duracion);
        pelicula.setClasificacion(clasificacion);
        peliculas.actualizar(pelicula);
        System.out.println("Película modificada.");
    }

    private void desactivarPelicula() throws PersistenciaException {
        int id = consola.entero("ID de película: ");
        Pelicula pelicula = peliculas.buscarPorId(id);
        if (pelicula == null) {
            System.out.println("No existe esa película.");
            return;
        }
        pelicula.setActiva(false);
        peliculas.actualizar(pelicula);
        System.out.println("Película desactivada.");
    }

    private void menuSalas() throws PersistenciaException {
        System.out.println("""
                SALAS
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

    private void altaSala() throws PersistenciaException {
        int numero = consola.entero("Número de sala: ");
        int capacidad = consola.entero("Capacidad: ");
        if (numero <= 0 || capacidad <= 0) throw new IllegalArgumentException("Número y capacidad deben ser positivos.");
        Sala sala = new Sala(0, numero, capacidad, true);
        salas.guardar(sala);
        System.out.println("Sala guardada con ID " + sala.getIdSala());
    }

    private void listarSalas() throws PersistenciaException {
        for (Sala sala : salas.listar()) {
            System.out.println(sala.getIdSala() + " - " + sala);
        }
    }

    private void modificarSala() throws PersistenciaException {
        Sala sala = salas.buscarPorId(consola.entero("ID de sala: "));
        if (sala == null) {
            System.out.println("No existe esa sala.");
            return;
        }
        System.out.println("Actual: " + sala);
        int numero = consola.entero("Nuevo número: ");
        int capacidad = consola.entero("Nueva capacidad: ");
        if (numero <= 0 || capacidad <= 0) throw new IllegalArgumentException("Número y capacidad deben ser positivos.");
        sala.setNumero(numero);
        sala.setCapacidad(capacidad);
        salas.actualizar(sala);
        System.out.println("Sala modificada.");
    }

    private void cambiarEstadoSala() throws PersistenciaException {
        Sala sala = salas.buscarPorId(consola.entero("ID de sala: "));
        if (sala == null) {
            System.out.println("No existe esa sala.");
            return;
        }
        sala.setHabilitada(!sala.estaHabilitada());
        salas.actualizar(sala);
        System.out.println("Nuevo estado: " + (sala.estaHabilitada() ? "habilitada" : "deshabilitada"));
    }

    private void menuClientes() throws PersistenciaException {
        System.out.println("""
                CLIENTES
                1. Registrar cliente
                2. Listar clientes
                3. Buscar cliente por DNI
                4. Modificar cliente
                """);
        switch (consola.entero("Opción: ")) {
            case 1 -> altaCliente();
            case 2 -> listarClientes();
            case 3 -> buscarCliente();
            case 4 -> modificarCliente();
            default -> System.out.println("Opción inválida.");
        }
    }

    private void altaCliente() throws PersistenciaException {
        String nombre = nombreValido("Nombre: ");
        String apellido = nombreValido("Apellido: ");
        String dni = dniValido("DNI: ");
        String email = emailValido("Email: ");
        String clave = consola.texto("Clave de acceso: ");
        Cliente cliente = new Cliente(null, nombre, apellido, dni, email, clave);
        clientes.guardarCliente(cliente);
        System.out.println("Cliente registrado con ID " + cliente.getId());
    }

    private void listarClientes() throws PersistenciaException {
        for (Cliente cliente : clientes.listar()) {
            System.out.println(cliente.getId() + " - " + cliente);
        }
    }

    private void buscarCliente() throws PersistenciaException {
        Cliente cliente = clientes.buscarPorDni(obligatorio("DNI: "));
        System.out.println(cliente == null ? "No existe ese cliente." : cliente);
    }

    private void modificarCliente() throws PersistenciaException {
        Cliente cliente = clientes.buscarPorId(consola.entero("ID de cliente: "));
        if (cliente == null) {
            System.out.println("No existe ese cliente.");
            return;
        }
        System.out.println("Actual: " + cliente);
        String nombre = nombreValido("Nuevo nombre: ");
        String apellido = nombreValido("Nuevo apellido: ");
        String dni = dniValido("Nuevo DNI: ");
        String email = emailValido("Nuevo email: ");
        cliente.setNombre(nombre);
        cliente.setApellido(apellido);
        cliente.setDni(dni);
        cliente.setEmail(email);
        clientes.actualizar(cliente);
        System.out.println("Cliente modificado.");
    }
}
