package org.cine;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.cine.enums.TipoEntrada;
import org.cine.modelo.*;
import org.cine.util.Validador;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.cine.ComponentesFX.*;

public class CineGestorFX extends Application {
    private final GestorCine cine = new GestorCine();
    private final VBox contenido = new VBox(18);
    private final ToggleGroup navegacion = new ToggleGroup();
    private Stage ventana;
    private AdministracionFX administracion;

    @Override
    public void start(Stage stage) {
        ventana = stage;
        administracion = new AdministracionFX(stage, cine);
        Label marca = texto("CineGestor", "marca");
        Label simbolo = texto("CG", "monograma");
        ToggleButton cartelera = pestana("Cartelera", this::cartelera);
        HBox barra = fila(simbolo, marca, espacio(), cartelera,
                pestana("Reservas", this::reservas), pestana("Administración", this::administrar));
        barra.getStyleClass().add("barra");
        cartelera.setSelected(true);
        contenido.getStyleClass().add("pagina");
        contenido.setMaxWidth(1300);
        StackPane margen = new StackPane(contenido);
        margen.setAlignment(Pos.TOP_CENTER);
        ScrollPane scroll = new ScrollPane(margen);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        BorderPane base = new BorderPane(scroll);
        base.setTop(barra);
        Scene escena = new Scene(base, 1040, 680);
        escena.getStylesheets().add(ESTILOS);
        stage.setScene(escena);
        stage.setTitle("CineGestor");
        stage.setMinWidth(760);
        stage.setMinHeight(510);
        stage.setMaximized(true);
        stage.show();
        Platform.runLater(() -> mostrar(this::cartelera));
    }

    private ToggleButton pestana(String titulo, Accion accion) {
        ToggleButton boton = new ToggleButton(titulo);
        boton.getStyleClass().add("pestana");
        boton.setToggleGroup(navegacion);
        boton.setOnAction(e -> {
            boton.setSelected(true);
            mostrar(accion);
        });
        return boton;
    }

    private void mostrar(Accion accion) {
        contenido.getChildren().clear();
        try {
            accion.ejecutar();
        } catch (Exception e) {
            contenido.getChildren().setAll(texto("No se pudo cargar la vista", "titulo"),
                    texto(e.getMessage(), "detalle"),
                    boton("Reintentar", "secundario", () -> mostrar(accion)));
        }
    }

    private void cartelera() throws Exception {
        List<Funcion> funciones = cine.listarFuncionesDisponibles();
        List<Pelicula> peliculas = cine.listarPeliculas().stream().filter(Pelicula::estaActiva).toList();
        HBox cabecera = fila(texto("Cartelera", "titulo"), espacio(),
                boton("+  Programar función", "secundario", this::programar));
        contenido.getChildren().addAll(texto(DIA.format(LocalDate.now()).toUpperCase(ARGENTINA), "ceja"), cabecera);
        if (funciones.isEmpty()) {
            contenido.getChildren().add(texto("Todavía no hay funciones disponibles. Programá la primera para abrir la venta.", "tarjeta"));
        } else {
            contenido.getChildren().add(ticket(funciones.get(0)));
        }
        if (funciones.size() > 1) {
            contenido.getChildren().add(texto("Más funciones", "subtitulo"));
            for (Funcion f : funciones.subList(1, funciones.size())) {
                HBox fila = fila(texto(f.getPelicula().getTitulo(), "nombre"), espacio(),
                        texto(FECHA.format(f.getFechaHora()) + " · Sala " + f.getSala().getNumero(), "detalle"),
                        boton("Informe", "secundario", () -> informe(f)),
                        boton("Reservar", "principal", () -> reservar(f)));
                fila.getStyleClass().add("tarjeta");
                contenido.getChildren().add(fila);
            }
        }
        contenido.getChildren().add(fila(texto("Películas en catálogo", "subtitulo"), espacio(),
                texto(peliculas.size() + (peliculas.size() == 1 ? " película" : " películas"), "detalle")));
        VBox catalogo = new VBox();
        catalogo.getStyleClass().add("lista");
        for (Pelicula p : peliculas) {
            HBox renglon = fila(texto(p.getTitulo(), "nombre"), espacio(), texto(p.getGenero(), "detalle"),
                    texto(p.getDuracionMinutos() + " min", "duracion"), texto(p.getClasificacion(), "clasificacion"));
            renglon.getStyleClass().add("renglon");
            catalogo.getChildren().add(renglon);
        }
        if (peliculas.isEmpty()) catalogo.getChildren().add(texto("Agregá una película desde Administración.", "tarjeta"));
        contenido.getChildren().add(catalogo);
    }

    private HBox ticket(Funcion f) {
        Pelicula p = f.getPelicula();
        Label titulo = texto(p.getTitulo(), "pelicula");
        VBox ficha = new VBox(10, texto("PRÓXIMA FUNCIÓN", "ceja-clara"), titulo,
                texto(p.getGenero() + "   ·   " + p.getDuracionMinutos() + " min   ·   " + p.getClasificacion(), "datos-ticket"),
                fila(boton("Elegir asiento", "acento", () -> reservar(f)),
                        boton("Ver informe", "sobre-oscuro", () -> informe(f))));
        ficha.getStyleClass().add("ficha");
        ficha.setAlignment(Pos.CENTER_LEFT);
        StackPane frente = new StackPane();
        Canvas fotogramas = tiraDeCine();
        fotogramas.setMouseTransparent(true);
        StackPane.setAlignment(fotogramas, Pos.CENTER_RIGHT);
        frente.getChildren().addAll(fotogramas, ficha);
        frente.getStyleClass().add("frente");
        HBox.setHgrow(frente, Priority.ALWAYS);
        Label hora = texto(HORA.format(f.getFechaHora()), "hora");
        VBox talon = new VBox(7, texto("SALA " + f.getSala().getNumero(), "ceja"), hora,
                texto(DIA.format(f.getFechaHora()), "dia-ticket"), new Separator(), texto(dinero(f.getPrecioBase()), "importe"));
        talon.setAlignment(Pos.CENTER_LEFT);
        talon.getStyleClass().add("talon");
        return new HBox(frente, talon);
    }

    // Una cinta de película dibujada en la ventana, disponible también sin conexión.
    private Canvas tiraDeCine() {
        Canvas lienzo = new Canvas(216, 204);
        GraphicsContext g = lienzo.getGraphicsContext2D();
        g.translate(143, 90);
        g.rotate(-30);
        g.setFill(Color.web("#49386e"));
        g.fillRect(-41, -200, 82, 410);
        for (int y = -190; y < 210; y += 44) {
            g.setFill(Color.web("#33264f"));
            g.fillRoundRect(-27, y, 54, 39, 5, 5);
            for (int i = 0; i < 4; i++) {
                g.fillRoundRect(-37, y + i * 11, 5, 6, 2, 2);
                g.fillRoundRect(32, y + i * 11, 5, 6, 2, 2);
            }
        }
        return lienzo;
    }

    private void programar() throws Exception {
        Formulario form = new Formulario(ventana, "Programar función", "Programar");
        var peliculas = opciones(cine.listarPeliculas().stream().filter(Pelicula::estaActiva).toList(), Pelicula::getTitulo);
        var salas = opciones(cine.listarSalas().stream().filter(Sala::estaHabilitada).toList(), s -> "Sala " + s.getNumero());
        DatePicker fecha = new DatePicker(LocalDate.now());
        fecha.setEditable(false);
        form.agregar("Película", peliculas);
        form.agregar("Sala", salas);
        form.agregar("Fecha", fecha);
        var hora = form.campo("Hora (HH:mm)", "20:00");
        var precio = form.campo("Precio base ($)", "");
        if (form.abrir(() -> cine.programarFuncion(
                elegido(peliculas, "Agregá una película activa.").getIdPelicula(),
                elegido(salas, "Habilitá una sala.").getIdSala(),
                Validador.fechaFutura(fecha.getValue() + " " + hora.getText()), Validador.precio(precio.getText())))) {
            mostrar(this::cartelera);
        }
    }

    private void reservar(Funcion f) throws Exception {
        Formulario form = new Formulario(ventana, "Reservar · " + f.getPelicula().getTitulo(), "Confirmar reserva");
        List<Integer> libres = cine.listarAsientosDisponibles(f.getIdFuncion());
        var clientes = opciones(cine.listarClientes(), CineGestorFX::nombreCliente);
        if (libres.isEmpty()) { aviso(ventana, "Sala completa", "Todos los asientos de esta función están reservados."); return; }
        if (clientes.getItems().isEmpty()) { aviso(ventana, "Primero registrá un cliente", "Abrí Administración y elegí Clientes."); return; }
        var asiento = opciones(libres, n -> "Asiento " + n);
        var tipo = opciones(List.of(TipoEntrada.values()), t -> t == TipoEntrada.GENERAL ? "General" : "Reducida");
        form.agregar("Cliente", clientes);
        form.agregar("Asientos disponibles · " + libres.size(), asiento);
        form.agregar("Tipo de entrada", tipo);
        var descuento = form.campo("Descuento (%)", "0");
        descuento.disableProperty().bind(tipo.valueProperty().isNotEqualTo(TipoEntrada.REDUCIDA));
        Label total = texto("", "importe");
        Runnable calcular = () -> {
            try {
                BigDecimal precio = tipo.getValue() == TipoEntrada.REDUCIDA
                        ? new EntradaReducida(f.getPrecioBase(), Validador.porcentaje(descuento.getText())).calcularPrecio()
                        : f.getPrecioBase();
                total.setText(dinero(precio));
            } catch (Exception ex) { total.setText("Revisá el porcentaje"); }
        };
        tipo.valueProperty().addListener((o, anterior, nuevo) -> calcular.run());
        descuento.textProperty().addListener((o, anterior, nuevo) -> calcular.run());
        calcular.run();
        form.agregar("Total", total);
        Reserva[] resultado = new Reserva[1];
        if (form.abrir(() -> {
            int numero = elegido(asiento, "Elegí un asiento disponible.").intValue();
            try {
                resultado[0] = cine.crearReserva(clientes.getValue().getId(), f.getIdFuncion(), numero,
                        tipo.getValue(), tipo.getValue() == TipoEntrada.REDUCIDA ? Validador.porcentaje(descuento.getText()) : BigDecimal.ZERO);
            } catch (excepciones.AsientoOcupadoException ocupado) {
                asiento.getItems().setAll(cine.listarAsientosDisponibles(f.getIdFuncion()));
                asiento.getSelectionModel().clearSelection();
                throw ocupado;
            }
        })) {
            aviso(ventana, "Reserva confirmada", f.getPelicula().getTitulo() + " · Asiento " + resultado[0].getNumeroAsiento()
                    + "\nReserva N.º " + resultado[0].getIdReserva() + " · " + dinero(resultado[0].getPrecioFinal()));
            mostrar(this::cartelera);
        }
    }

    private static String nombreCliente(Cliente c) { return c.getApellido() + ", " + c.getNombre() + " · " + c.getDni(); }

    private void reservas() throws Exception {
        contenido.getChildren().add(texto("Reservas", "titulo"));
        var clientes = opciones(cine.listarClientes(), CineGestorFX::nombreCliente);
        VBox lista = new VBox(8);
        contenido.getChildren().addAll(texto("Consultar por cliente", "campo-label"), clientes, lista);
        clientes.setOnAction(e -> cargarReservas(clientes.getValue(), lista));
        cargarReservas(clientes.getValue(), lista);
    }

    private void cargarReservas(Cliente cliente, VBox lista) {
        lista.getChildren().clear();
        if (cliente == null) { lista.getChildren().add(texto("Todavía no hay clientes registrados.", "tarjeta")); return; }
        try {
            List<Reserva> reservas = cine.listarReservasPorCliente(cliente.getId());
            for (Reserva r : reservas) {
                VBox datos = new VBox(5, texto(r.getFuncion().getPelicula().getTitulo(), "nombre"),
                        texto(FECHA.format(r.getFuncion().getFechaHora()) + " · Asiento " + r.getNumeroAsiento()
                            + " · Reserva N.º " + r.getIdReserva(), "detalle"));
                HBox fila = fila(datos, espacio(), texto(dinero(r.getPrecioFinal()), "importe"),
                        texto(r.estaConfirmada() ? "Confirmada" : "Cancelada", "detalle"),
                        boton("Informe", "secundario", () -> informe(r.getFuncion())));
                if (r.estaConfirmada()) fila.getChildren().add(boton("Cancelar", "secundario", () -> {
                    if (confirmar(ventana, "¿Cancelar la reserva N.º " + r.getIdReserva() + " y liberar el asiento?")) {
                        cine.cancelarReserva(r.getIdReserva());
                        cargarReservas(cliente, lista);
                    }
                }));
                fila.getStyleClass().add("tarjeta");
                lista.getChildren().add(fila);
            }
            if (reservas.isEmpty()) lista.getChildren().add(texto("Este cliente todavía no tiene reservas.", "tarjeta"));
        } catch (Exception e) { lista.getChildren().setAll(texto(e.getMessage(), "error")); }
    }

    private void informe(Funcion f) throws Exception {
        aviso(ventana, "Informe · " + f.getPelicula().getTitulo(),
                FECHA.format(f.getFechaHora()) + " · Sala " + f.getSala().getNumero() + "\n\n"
                        + cine.informeFuncion(f.getIdFuncion()).replace(" | ", "\n"));
    }

    private void administrar() throws Exception {
        contenido.getChildren().addAll(texto("Administración", "titulo"), administracion.vista());
    }
}
