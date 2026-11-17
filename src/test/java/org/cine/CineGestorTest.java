package org.cine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

class CineGestorTest {
    @Test
    void reglasDelModelo() throws Exception {
        EscenariosCine.probarModelos();
    }

    @Test
    void guardaYCancelaReservasConcurrentesEnMySql() throws Exception {
        assumeTrue("1".equals(System.getenv("CINE_TEST_MYSQL")), "Usar el esquema de pruebas MySQL");
        EscenariosCine.probarMySql();
    }
}
