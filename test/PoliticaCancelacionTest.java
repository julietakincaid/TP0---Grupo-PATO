package prueba;

import modelo.PoliticaCancelacion;
import modelo.enums.OrigenCancelacion;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Cuánto se devuelve al cancelar según los días que faltan para la salida (RN19 y RN20).
class PoliticaCancelacionTest {
    private final PoliticaCancelacion politica = new PoliticaCancelacion();
    private final LocalDate fechaSalida = LocalDate.of(2027, 3, 31);

    private double porcentaje(int diasAntes, OrigenCancelacion origen) {
        return politica.calcularPorcentaje(fechaSalida.minusDays(diasAntes), fechaSalida, origen);
    }

    @Test
    void conTreintaDiasOMasSeDevuelveElNoventaPorCiento() {
        assertEquals(0.90, porcentaje(40, OrigenCancelacion.CLIENTE), 0.001);
        assertEquals(0.90, porcentaje(30, OrigenCancelacion.CLIENTE), 0.001);
    }

    @Test
    void entreQuinceYVeintinueveDiasSeDevuelveLaMitad() {
        assertEquals(0.50, porcentaje(29, OrigenCancelacion.CLIENTE), 0.001);
        assertEquals(0.50, porcentaje(15, OrigenCancelacion.CLIENTE), 0.001);
    }

    @Test
    void entreSieteYCatorceDiasSeDevuelveElVeintePorCiento() {
        assertEquals(0.20, porcentaje(14, OrigenCancelacion.CLIENTE), 0.001);
        assertEquals(0.20, porcentaje(7, OrigenCancelacion.CLIENTE), 0.001);
    }

    @Test
    void conMenosDeSieteDiasNoSeDevuelveNada() {
        assertEquals(0.0, porcentaje(6, OrigenCancelacion.CLIENTE), 0.001);
        assertEquals(0.0, porcentaje(0, OrigenCancelacion.CLIENTE), 0.001);
    }

    @Test
    void siCancelaLaAgenciaSeDevuelveTodoSinImportarLosDias() {
        assertEquals(1.0, porcentaje(1, OrigenCancelacion.AGENCIA), 0.001);
    }

    @Test
    void laAnulacionPorFaltaDePagoNoDevuelveLaSenia() {
        assertEquals(0.0, porcentaje(100, OrigenCancelacion.FALTA_PAGO), 0.001);
    }
}