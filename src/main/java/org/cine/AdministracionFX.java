package org.cine;

import dao.ClienteDAO;
import dao.PeliculaDAO;
import dao.SalaDAO;
import excepciones.DatoInvalidoException;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;
import org.cine.modelo.Cliente;
import org.cine.modelo.Pelicula;
import org.cine.modelo.Sala;
import org.cine.util.Validador;

import java.util.List;
import java.util.Objects;

import static org.cine.ComponentesFX.*;

final class AdministracionFX {
    private final Window ventana;
    private final GestorCine cine;
    private final PeliculaDAO peliculas = new PeliculaDAO();
    private final SalaDAO salas = new SalaDAO();
    private final ClienteDAO clientes = new ClienteDAO();
    private final VBox listado = new VBox(8);
    private final ComboBox<String> seccion = opciones(List.of("Películas", "Salas", "Clientes"), s -> s);

    AdministracionFX(Window ventana, GestorCine cine) {
        this.ventana = ventana;
        this.cine = cine;
    }

    VBox vista() throws Exception {
        Button agregar = boton("+  Agregar", "principal", () -> {
            switch (seccion.getValue()) {
                case "Películas" -> pelicula(null);
                case "Salas" -> sala(null);
                case "Clientes" -> cliente(null);
            }
        });
        seccion.setOnAction(e -> recargar());
        recargar();
        return new VBox(18, fila(seccion, espacio(), agregar), listado);
    }

    private void recargar() {
        listado.getChildren().clear();
        try {
            switch (seccion.getValue()) {
                case "Películas" -> {
                    for (var p : cine.listarPeliculas()) {
                        renglon(p.getTitulo(), p.getGenero() + " · " + p.getDuracionMinutos() + " min · " + p.getClasificacion(),
                                p.estaActiva() ? "Activa" : "Inactiva", () -> pelicula(p),
                                p.estaActiva() ? "Desactivar" : "", () -> {
                                    if (confirmar(ventana, "¿Desactivar " + p.getTitulo() + "?")) {
                                        Pelicula actual = peliculas.buscarPorId(p.getIdPelicula());
                                        if (actual == null) throw new DatoInvalidoException("La película ya no existe.");
                                        actual.setActiva(false);
                                        peliculas.actualizar(actual);
                                        recargar();
                                    }
                                });
                    }
                }
                case "Salas" -> {
                    for (var s : cine.listarSalas()) {
                        renglon("Sala " + s.getNumero(), s.getCapacidad() + " asientos",
                                s.estaHabilitada() ? "Habilitada" : "Deshabilitada", () -> sala(s),
                                s.estaHabilitada() ? "Deshabilitar" : "Habilitar", () -> {
                                    if (confirmar(ventana, "¿Cambiar el estado de la sala " + s.getNumero() + "?")) {
                                        Sala actual = salas.buscarPorId(s.getIdSala());
                                        if (actual == null) throw new DatoInvalidoException("La sala ya no existe.");
                                        actual.setHabilitada(!s.estaHabilitada());
                                        salas.actualizar(actual);
                                        recargar();
                                    }
                                });
                    }
                }
                case "Clientes" -> {
                    TextField buscar = new TextField();
                    buscar.setPromptText("Buscar por DNI");
                    List<Cliente> registros = cine.listarClientes();
                    listado.getChildren().add(buscar);
                    Runnable filtrar = () -> {
                        listado.getChildren().remove(1, listado.getChildren().size());
                        for (var c : registros) {
                            if (c.getDni().contains(buscar.getText().trim())) {
                                renglon(c.getNombre() + " " + c.getApellido(), c.getEmail(), "DNI " + c.getDni(),
                                        () -> cliente(c), "Dar de baja", () -> baja(c));
                            }
                        }
                        if (listado.getChildren().size() == 1) listado.getChildren().add(texto("No se encontraron clientes.", "tarjeta"));
                    };
                    buscar.textProperty().addListener((o, anterior, nuevo) -> filtrar.run());
                    filtrar.run();
                }
            }
            if (listado.getChildren().isEmpty()) listado.getChildren().add(texto("Todavía no hay registros.", "tarjeta"));
        } catch (Exception e) {
            listado.getChildren().setAll(texto(e.getMessage(), "error"), boton("Reintentar", "secundario", this::recargar));
        }
    }

    private void renglon(String nombre, String detalle, String estado, Accion editar, String accion, Accion ejecutar) {
        HBox fila = fila(new VBox(4, texto(nombre, "nombre"), texto(detalle, "detalle")),
                espacio(), texto(estado, "detalle"), boton("Editar", "secundario", editar));
        if (!accion.isEmpty()) fila.getChildren().add(boton(accion, "secundario", ejecutar));
        fila.getStyleClass().add("tarjeta");
        listado.getChildren().add(fila);
    }

    private void pelicula(Pelicula actual) {
        boolean nueva = actual == null;
        Formulario form = new Formulario(ventana, nueva ? "Agregar película" : "Editar película", "Guardar");
        var titulo = form.campo("Título", nueva ? "" : actual.getTitulo());
        var genero = opciones(List.of(Validador.GENEROS), s -> s);
        var clasificacion = opciones(List.of(Validador.CLASIFICACIONES), s -> s);
        if (!nueva) {
            genero.setValue(actual.getGenero());
            clasificacion.setValue(actual.getClasificacion());
        }
        form.agregar("Género", genero);
        var duracion = form.campo("Duración (minutos)", nueva ? "" : Integer.toString(actual.getDuracionMinutos()));
        form.agregar("Clasificación", clasificacion);
        if (form.abrir(() -> {
            String nombre = Validador.textoObligatorio(titulo.getText(), "título");
            Integer id = nueva ? null : actual.getIdPelicula();
            for (var p : peliculas.listar()) {
                if (p.getTitulo().equalsIgnoreCase(nombre) && !Objects.equals(p.getIdPelicula(), id))
                    throw new DatoInvalidoException("Ya existe una película con ese título.");
            }
            String generoElegido = genero.getValue();
            String clasificacionElegida = clasificacion.getValue();
            // Los registros anteriores pueden tener otra clasificación o género.
            if (nueva || !Objects.equals(generoElegido, actual.getGenero())) generoElegido = Validador.genero(generoElegido);
            if (nueva || !Objects.equals(clasificacionElegida, actual.getClasificacion())) clasificacionElegida = Validador.clasificacion(clasificacionElegida);
            Pelicula p = new Pelicula(id, nombre, generoElegido,
                    Validador.entero(duracion.getText(), "duración", 1, 600), clasificacionElegida, nueva || actual.estaActiva());
            if (nueva) peliculas.guardarPelicula(p); else peliculas.actualizar(p);
        })) recargar();
    }

    private void sala(Sala actual) {
        boolean nueva = actual == null;
        Formulario form = new Formulario(ventana, nueva ? "Agregar sala" : "Editar sala", "Guardar");
        var numero = form.campo("Número de sala", nueva ? "" : Integer.toString(actual.getNumero()));
        var capacidad = form.campo("Cantidad de asientos", nueva ? "" : Integer.toString(actual.getCapacidad()));
        if (form.abrir(() -> {
            int n = Validador.entero(numero.getText(), "número de sala", 1, 99);
            int id = nueva ? 0 : actual.getIdSala();
            for (var s : salas.listar()) {
                if (s.getNumero() == n && s.getIdSala() != id) throw new DatoInvalidoException("Ya existe una sala con ese número.");
            }
            Sala s = new Sala(id, n, Validador.entero(capacidad.getText(), "capacidad", 1, 500), nueva || actual.estaHabilitada());
            if (nueva) salas.guardar(s); else salas.actualizar(s);
        })) recargar();
    }

    private void cliente(Cliente actual) {
        boolean nuevo = actual == null;
        Formulario form = new Formulario(ventana, nuevo ? "Registrar cliente" : "Editar cliente", "Guardar");
        var nombre = form.campo("Nombre", nuevo ? "" : actual.getNombre());
        var apellido = form.campo("Apellido", nuevo ? "" : actual.getApellido());
        var dni = form.campo("DNI", nuevo ? "" : actual.getDni());
        var email = form.campo("Email", nuevo ? "" : actual.getEmail());
        var clave = new PasswordField();
        if (nuevo) {
            clave.setPromptText("8 caracteres, mayúscula, minúscula y número");
            form.agregar("Clave de acceso", clave);
        }
        if (form.abrir(() -> {
            String nombreValido = Validador.nombre(nombre.getText(), "nombre");
            String apellidoValido = Validador.nombre(apellido.getText(), "apellido");
            String dniValido = Validador.dni(dni.getText());
            Cliente existe = clientes.buscarPorDni(dniValido);
            Integer id = nuevo ? null : actual.getId();
            if (existe != null && !Objects.equals(existe.getId(), id)) throw new DatoInvalidoException("Ya existe un cliente con ese DNI.");
            Cliente c = new Cliente(id, nombreValido, apellidoValido, dniValido, Validador.email(email.getText()),
                    nuevo ? Validador.clave(clave.getText()) : actual.getClaveAcceso());
            if (nuevo) clientes.guardarCliente(c); else clientes.actualizar(c);
        })) recargar();
    }

    private void baja(Cliente cliente) throws Exception {
        if (!cine.listarReservasPorCliente(cliente.getId()).isEmpty()) {
            throw new DatoInvalidoException("El cliente tiene reservas y no se puede dar de baja.");
        }
        if (confirmar(ventana, "¿Dar de baja a " + cliente.getNombre() + " " + cliente.getApellido() + "?")) {
            clientes.eliminar(cliente.getId());
            recargar();
        }
    }
}
