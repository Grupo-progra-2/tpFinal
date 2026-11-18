package org.cine;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

final class ComponentesFX {
    static final Locale ARGENTINA = Locale.forLanguageTag("es-AR");
    static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", ARGENTINA);
    static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm");
    static final String ESTILOS = ComponentesFX.class.getResource("cinegestor.css").toExternalForm();

    private ComponentesFX() {}

    @FunctionalInterface
    interface Accion { void ejecutar() throws Exception; }

    static Label texto(String valor, String estilo) {
        Label label = new Label(valor);
        label.getStyleClass().add(estilo);
        label.setWrapText(true);
        return label;
    }

    static Region espacio() {
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        return espacio;
    }

    static HBox fila(Node... elementos) {
        HBox fila = new HBox(12, elementos);
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    static Button boton(String titulo, String estilo, Accion accion) {
        Button boton = new Button(titulo);
        boton.getStyleClass().add(estilo);
        boton.setOnAction(e -> {
            try {
                accion.ejecutar();
            } catch (Exception error) {
                aviso(boton.getScene().getWindow(), "Revisá los datos", error.getMessage());
            }
        });
        return boton;
    }

    static void aviso(Window ventana, String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        alerta.initOwner(ventana);
        alerta.setTitle("CineGestor");
        alerta.setHeaderText(titulo);
        alerta.showAndWait();
    }

    static boolean confirmar(Window ventana, String pregunta) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, pregunta, ButtonType.CANCEL, ButtonType.OK);
        alerta.initOwner(ventana);
        alerta.setTitle("CineGestor");
        alerta.setHeaderText(null);
        return alerta.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    static <T> ComboBox<T> opciones(List<T> items, Function<T, String> nombre) {
        ComboBox<T> combo = new ComboBox<>();
        combo.getItems().setAll(items);
        combo.setConverter(new StringConverter<>() {
            public String toString(T valor) { return valor == null ? "" : nombre.apply(valor); }
            public T fromString(String texto) { throw new UnsupportedOperationException(); }
        });
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.getSelectionModel().selectFirst();
        return combo;
    }

    static <T> T elegido(ComboBox<T> combo, String mensaje) {
        if (combo.getValue() == null) throw new IllegalArgumentException(mensaje);
        return combo.getValue();
    }

    static String dinero(java.math.BigDecimal valor) {
        return java.text.NumberFormat.getCurrencyInstance(ARGENTINA).format(valor);
    }

    static final class Formulario {
        private final Dialog<ButtonType> dialogo = new Dialog<>();
        private final VBox campos = new VBox(12);
        private final Label error = texto("", "error");
        private final ButtonType aceptar;

        Formulario(Window ventana, String titulo, String accion) {
            dialogo.initOwner(ventana);
            dialogo.setTitle(titulo);
            dialogo.getDialogPane().getStylesheets().add(ESTILOS);
            dialogo.getDialogPane().getStyleClass().add("formulario");
            aceptar = new ButtonType(accion, ButtonBar.ButtonData.OK_DONE);
            dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, aceptar);
            ((Button) dialogo.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Volver");
            dialogo.getDialogPane().lookupButton(aceptar).getStyleClass().add("principal");
            campos.setPadding(new Insets(8, 14, 6, 14));
            campos.setPrefWidth(450);
            campos.getChildren().add(texto(titulo, "subtitulo"));
            error.setManaged(false);
            error.setVisible(false);
            error.setMaxWidth(415);
            ScrollPane scroll = new ScrollPane(campos);
            scroll.setFitToWidth(true);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.setMaxHeight(470);
            dialogo.getDialogPane().setContent(scroll);
        }

        void agregar(String etiqueta, Node control) {
            Label nombre = texto(etiqueta, "campo-label");
            nombre.setLabelFor(control);
            campos.getChildren().add(new VBox(4, nombre, control));
        }

        TextField campo(String nombre, String valor) {
            TextField control = new TextField(valor);
            agregar(nombre, control);
            return control;
        }

        boolean abrir(Accion guardar) {
            campos.getChildren().add(error);
            dialogo.getDialogPane().lookupButton(aceptar).addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
                try {
                    guardar.ejecutar();
                } catch (Exception ex) {
                    error.setText(ex.getMessage());
                    error.setManaged(true);
                    error.setVisible(true);
                    e.consume();
                    ((ScrollPane) dialogo.getDialogPane().getContent()).setVvalue(1);
                    dialogo.getDialogPane().getScene().getWindow().sizeToScene();
                }
            });
            return dialogo.showAndWait().orElse(ButtonType.CANCEL) == aceptar;
        }
    }
}
