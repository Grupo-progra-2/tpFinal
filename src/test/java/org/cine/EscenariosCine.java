package org.cine;

import excepciones.AsientoOcupadoException;
import excepciones.DatoInvalidoException;
import excepciones.ElementoNoEncontradoException;
import excepciones.HorarioSuperpuestoException;
import org.cine.dao.ConexionBD;
import org.cine.enums.EstadoFuncion;
import org.cine.enums.EstadoReserva;
import org.cine.enums.TipoEntrada;
import org.cine.modelo.EntradaReducida;
import org.cine.modelo.Funcion;
import org.cine.modelo.Pelicula;
import org.cine.modelo.Reserva;
import org.cine.modelo.Sala;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

final class EscenariosCine {
    private EscenariosCine() {
    }

    public static void main(String[] args) throws Exception {
        probarModelos();
        System.out.println("OK: descuentos, redondeo y disponibilidad del modelo");
        if ("1".equals(System.getenv("CINE_TEST_MYSQL"))) {
            probarMySql();
            System.out.println("OK: programación, bloqueo de duplicados, cancelación y persistencia en MySQL");
        }
    }

    static void probarModelos() throws Exception {
        comprobar(entrada("1000", "20").calcularPrecio().equals(new BigDecimal("800.00")), "20 % de descuento");
        comprobar(entrada("1000", "0").calcularPrecio().equals(new BigDecimal("1000.00")), "0 % de descuento");
        comprobar(entrada("1000", "100").calcularPrecio().signum() == 0, "100 % de descuento");
        comprobar(entrada("1000", "0.2").calcularPrecio().equals(new BigDecimal("998.00")), "0,2 % de descuento");
        comprobar(entrada("19.99", "12.5").calcularPrecio().equals(new BigDecimal("17.49")), "redondeo a centavos");
        debeFallar(IllegalArgumentException.class, () -> entrada("1000", "-1"));
        debeFallar(IllegalArgumentException.class, () -> entrada("1000", "101"));
        debeFallar(IllegalArgumentException.class, () -> entrada("-1", "20"));

        LocalDateTime ahora = LocalDateTime.of(2026, 1, 1, 12, 0);
        Pelicula pelicula = new Pelicula(1, "Prueba", null, 90, null, true);
        Sala sala = new Sala(1, 1, 8, true);
        Funcion futura = new Funcion(1, pelicula, sala, ahora.plusHours(1), BigDecimal.TEN, EstadoFuncion.PROGRAMADA);
        Funcion pasada = new Funcion(2, pelicula, sala, ahora.minusMinutes(1), BigDecimal.TEN, EstadoFuncion.PROGRAMADA);
        comprobar(futura.estaDisponible(ahora), "función futura disponible");
        comprobar(!pasada.estaDisponible(ahora), "función cerrada");
        comprobar(!futura.estaDisponible(futura.getFechaHora()), "no reserva en hora de inicio");
        sala.setHabilitada(false);
        comprobar(!futura.estaDisponible(ahora), "sala inhabilitada");
        sala.setHabilitada(true);
        pelicula.setActiva(false);
        comprobar(!futura.estaDisponible(ahora), "película inactiva");
        pelicula.setActiva(true);
        futura.cancelar();
        comprobar(!futura.estaDisponible(ahora), "función cancelada");
    }

    static void probarMySql() throws Exception {
        GestorCine cine = new GestorCine();
        String sufijo = UUID.randomUUID().toString().substring(0, 8);
        LocalDateTime futura = LocalDateTime.now().plusDays(1).withNano(0);
        try (Connection conexion = new ConexionBD().obtenerConexion()) {
            int pelicula = insertar(conexion, "INSERT INTO pelicula (titulo, duracion_minutos) VALUES (?, ?)", "Test " + sufijo, 90);
            int numeroSala = (sufijo.hashCode() & 0x3fffffff) + 100;
            int sala = insertar(conexion, "INSERT INTO sala (numero, capacidad) VALUES (?, ?)", numeroSala, 3);
            int cliente = insertar(conexion, "INSERT INTO cliente (nombre, dni) VALUES (?, ?)", "Prueba", "test-" + sufijo);
            int vieja = insertar(conexion, """
                    INSERT INTO funcion (id_pelicula, id_sala, fecha_hora, precio_base, estado)
                    VALUES (?, ?, ?, 1000, 'PROGRAMADA')
                    """, pelicula, sala, futura.minusDays(2));

            Funcion funcion = cine.programarFuncion(pelicula, sala, futura, new BigDecimal("1000.00"));
            int id = funcion.getIdFuncion();
            comprobar(id > 0, "ID generado para la función");
            comprobar(cine.listarFuncionesDisponibles().stream().anyMatch(f -> f.getIdFuncion() == id), "listar futura");
            comprobar(cine.listarFuncionesDisponibles().stream().noneMatch(f -> f.getIdFuncion() == vieja), "ocultar pasada");
            debeFallar(HorarioSuperpuestoException.class, () -> cine.programarFuncion(pelicula, sala, futura.plusMinutes(1), BigDecimal.TEN));
            int otra = cine.programarFuncion(pelicula, sala, futura.plusMinutes(90), BigDecimal.TEN).getIdFuncion();
            debeFallar(DatoInvalidoException.class, () -> cine.crearReserva(cliente, vieja, 1, TipoEntrada.GENERAL, null));
            debeFallar(DatoInvalidoException.class, () -> cine.crearReserva(cliente, id, 0, TipoEntrada.GENERAL, null));
            debeFallar(DatoInvalidoException.class, () -> cine.crearReserva(cliente, id, 4, TipoEntrada.GENERAL, null));
            debeFallar(DatoInvalidoException.class, () -> cine.crearReserva(cliente, id, 1, TipoEntrada.REDUCIDA, BigDecimal.valueOf(101)));
            debeFallar(ElementoNoEncontradoException.class, () -> cine.crearReserva(0, id, 1, TipoEntrada.GENERAL, null));
            comprobar(cine.listarAsientosDisponibles(id).equals(List.of(1, 2, 3)), "asientos iniciales");

            Reserva primera = competir(cliente, id);
            comprobar(primera.getPrecioFinal().equals(new BigDecimal("800.00")), "precio persistente con descuento");
            comprobar(cine.listarAsientosDisponibles(id).equals(List.of(1, 3)), "asiento ocupado");
            comprobar(cine.listarAsientosDisponibles(otra).equals(List.of(1, 2, 3)), "otra función independiente");
            debeFallar(AsientoOcupadoException.class, () -> cine.crearReserva(cliente, id, 2, TipoEntrada.GENERAL, null));

            new GestorCine().cancelarReserva(primera.getIdReserva());
            cine.cancelarReserva(primera.getIdReserva());
            debeFallar(ElementoNoEncontradoException.class, () -> cine.cancelarReserva(0));
            Reserva segunda = new GestorCine().crearReserva(cliente, id, 2, TipoEntrada.GENERAL, null);
            comprobar(segunda.getIdReserva() != primera.getIdReserva(), "volver a reservar con nuevo ID");
            comprobar(cine.informeFuncion(id).equals("Entradas confirmadas: 1/3 | Recaudación: $1000.00"),
                    "el informe cuenta solo reservas confirmadas");
            List<Reserva> historial = new GestorCine().listarReservasPorCliente(cliente);
            comprobar(historial.size() == 2 && historial.getLast().getEstado() == EstadoReserva.CANCELADA
                    && historial.getFirst().estaConfirmada(), "historial de estados en reconexión");
            comprobar(historial.getLast().getPrecioFinal().equals(new BigDecimal("800.00"))
                    && historial.getLast().getTipoEntrada() == TipoEntrada.REDUCIDA, "precio y tipo históricos");
        }
    }

    private static Reserva competir(int cliente, int id) throws Exception {
        CountDownLatch listos = new CountDownLatch(2);
        CountDownLatch empezar = new CountDownLatch(1);
        Callable<Reserva> reservar = () -> {
            listos.countDown();
            if (!empezar.await(5, TimeUnit.SECONDS)) {
                throw new AssertionError("Los dos intentos tienen que arrancar juntos.");
            }
            try {
                return new GestorCine().crearReserva(cliente, id, 2, TipoEntrada.REDUCIDA, new BigDecimal("20"));
            } catch (AsientoOcupadoException e) {
                return null;
            }
        };
        try (var ejecutor = Executors.newFixedThreadPool(2)) {
            var primerIntento = ejecutor.submit(reservar);
            var segundoIntento = ejecutor.submit(reservar);
            comprobar(listos.await(5, TimeUnit.SECONDS), "intentos listos");
            empezar.countDown();
            Reserva primera = primerIntento.get(10, TimeUnit.SECONDS);
            Reserva segunda = segundoIntento.get(10, TimeUnit.SECONDS);
            comprobar((primera == null) != (segunda == null), "exactamente una reserva simultánea confirmada");
            return primera == null ? segunda : primera;
        }
    }

    private static int insertar(Connection conexion, String sql, Object... parametros) throws Exception {
        try (PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            stmt.executeUpdate();
            try (ResultSet claves = stmt.getGeneratedKeys()) {
                comprobar(claves.next(), "ID de prueba generado");
                return claves.getInt(1);
            }
        }
    }

    private static EntradaReducida entrada(String precio, String descuento) {
        return new EntradaReducida(new BigDecimal(precio), new BigDecimal(descuento));
    }

    private static void comprobar(boolean condicion, String caso) {
        if (!condicion) {
            throw new AssertionError(caso);
        }
    }

    private static void debeFallar(Class<? extends Exception> tipo, Accion accion) throws Exception {
        try {
            accion.ejecutar();
        } catch (Exception e) {
            if (tipo.isInstance(e)) {
                return;
            }
            throw e;
        }
        throw new AssertionError("Se esperaba " + tipo.getSimpleName());
    }

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws Exception;
    }
}
