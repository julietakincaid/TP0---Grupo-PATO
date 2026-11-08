package prueba;

import excepcion.ReglaNegocioException;
import modelo.Cancelacion;
import modelo.Pago;
import modelo.Pasajero;
import modelo.Reserva;
import modelo.Salida;
import modelo.TourAventura;
import modelo.enums.EstadoReserva;
import modelo.enums.MedioPago;
import modelo.enums.OrigenCancelacion;
import modelo.enums.TipoPasajero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Reglas de la reserva: precios por tipo de pasajero, promociones, seña, pagos, cancelación y ajustes.
class ReservaTest {
    private Escenario e;

    @BeforeEach
    void preparar() throws Exception {
        e = new Escenario();
    }

    // ---------- precio (RF12 y RF13) ----------

    @Test
    void unAdultoPagaLaTarifaCompleta() throws Exception {
        Reserva reserva = e.reservaConAdultos(e.salida, 1);
        assertEquals(100.0, reserva.getTotal(), 0.001);
    }

    @Test
    void unMenorDeDoceAniosPagaElSesentaPorCiento() throws Exception {
        Reserva reserva = e.reservaConAdultos(e.salida, 1);
        e.agregarPasajero(reserva, e.salida.getFechaInicio().minusYears(8), TipoPasajero.MENOR);
        e.calculadora.recalcularTotal(reserva);
        assertEquals(160.0, reserva.getTotal(), 0.001);   // 100 del adulto + 60 del menor
    }

    @Test
    void unMenorDeTresAniosNoPaga() throws Exception {
        Reserva reserva = e.reservaConAdultos(e.salida, 1);
        e.agregarPasajero(reserva, e.salida.getFechaInicio().minusYears(2), TipoPasajero.MENOR);
        e.calculadora.recalcularTotal(reserva);
        assertEquals(100.0, reserva.getTotal(), 0.001);
    }

    @Test
    void unJubiladoRecibeDiezPorCientoDeDescuento() throws Exception {
        Reserva reserva = e.reservaConAdultos(e.salida, 1);
        e.agregarPasajero(reserva, LocalDate.of(1950, 1, 1), TipoPasajero.JUBILADO);
        e.calculadora.recalcularTotal(reserva);
        assertEquals(190.0, reserva.getTotal(), 0.001);   // 100 + 90
    }

    @Test
    void losServiciosAdicionalesSeSumanAlTotal() throws Exception {
        Reserva reserva = e.reservaConAdultos(e.salida, 1);
        reserva.contratarServicio(reserva.getPasajeros().get(0), e.excursion);
        e.calculadora.recalcularTotal(reserva);
        assertEquals(130.0, reserva.getTotal(), 0.001);
    }

    @Test
    void conMasDeSesentaDiasDeAnticipacionSeAplicaElQuincePorCiento() throws Exception {
        Salida lejana = e.nuevaSalida(65, 1, 10);
        Reserva reserva = e.reservaConAdultos(lejana, 2);
        assertEquals(170.0, reserva.getTotal(), 0.001);   // 200 - 15%
    }

    @Test
    void unGrupoDeSeisOMasRecibeElDiezPorCiento() throws Exception {
        Reserva reserva = e.reservaConAdultos(e.salida, 6);
        assertEquals(540.0, reserva.getTotal(), 0.001);   // 600 - 10%
    }

    @Test
    void seAplicaSoloLaPromocionMasFavorable() throws Exception {
        Salida lejana = e.nuevaSalida(65, 1, 10);
        Reserva reserva = e.reservaConAdultos(lejana, 6);
        assertEquals(510.0, reserva.getTotal(), 0.001);   // gana el 15% de anticipación y no se suma el 10% del grupo
    }

    @Test
    void unMenorQueLaEdadMinimaNoPuedeViajarEnUnTourDeAventura() throws Exception {
        TourAventura aventura = e.crearTourAventura("T-AV");
        Salida salidaAventura = e.agencia.programarSalida(e.hoy.plusDays(30), e.hoy.plusDays(31), 1, 8, aventura);
        Reserva reserva = e.agencia.crearReserva(salidaAventura, e.cliente, e.vendedor, e.hoy);
        assertThrows(ReglaNegocioException.class,
                () -> e.agregarPasajero(reserva, salidaAventura.getFechaInicio().minusYears(12), TipoPasajero.MENOR));
    }

    // ---------- seña y pagos (RN16 a RN18) ----------

    @Test
    void laSeniaDelTreintaPorCientoConfirmaLaReserva() throws Exception {
        Reserva reserva = e.reservaConAdultos(e.salida, 1);
        reserva.crearPago(e.hoy, 29.99, MedioPago.EFECTIVO, 1);
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        reserva.crearPago(e.hoy, 0.01, MedioPago.EFECTIVO, 1);
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void noSePuedePagarMasQueElSaldo() throws Exception {
        Reserva reserva = e.reservaConAdultos(e.salida, 1);
        assertThrows(ReglaNegocioException.class, () -> reserva.crearPago(e.hoy, 101, MedioPago.EFECTIVO, 1));
    }

    @Test
    void elPagoConTarjetaEnCuotasTieneUnRecargoDelSietePorCiento() throws Exception {
        Reserva reserva = e.reservaConAdultos(e.salida, 1);
        Pago conTarjeta = reserva.crearPago(e.hoy, 50, MedioPago.TARJETA, 3);
        Pago enEfectivo = reserva.crearPago(e.hoy, 50, MedioPago.EFECTIVO, 1);
        assertEquals(3.5, conTarjeta.calcularRecargo(), 0.001);
        assertEquals(0.0, enEfectivo.calcularRecargo(), 0.001);
        assertEquals(0.0, reserva.calcularSaldo(), 0.001);   // el recargo no cuenta como parte del saldo
    }

    @Test
    void noSeAceptanPagosDespuesDelPlazoDelSaldo() throws Exception {
        Reserva reserva = e.reservaConAdultos(e.salida, 1);
        LocalDate fueraDePlazo = e.salida.getFechaInicio().minusDays(9);   // el plazo vence 10 días antes
        assertThrows(ReglaNegocioException.class, () -> reserva.crearPago(fueraDePlazo, 50, MedioPago.EFECTIVO, 1));
    }

    @Test
    void unPagoNoPuedeSerAnteriorALaFechaDeLaReserva() throws Exception {
        Reserva reserva = e.reservaConAdultos(e.salida, 1);
        assertThrows(ReglaNegocioException.class,
                () -> reserva.crearPago(e.hoy.minusDays(1), 50, MedioPago.EFECTIVO, 1));
    }

    @Test
    void unaReservaAdmitePagosMientrasTengaSaldo() throws Exception {
        Reserva reserva = e.reservaConAdultos(e.salida, 1);
        assertTrue(reserva.admitePagos());
        reserva.crearPago(e.hoy, 100, MedioPago.EFECTIVO, 1);
        assertFalse(reserva.admitePagos());
    }

    // ---------- modificar, ajustar y cancelar (RN19 a RN21) ----------

    @Test
    void noSeQuitaUnPasajeroDeUnaReservaConPagos() throws Exception {
        Reserva reserva = e.reservaConAdultos(e.salida, 2);
        reserva.crearPago(e.hoy, 50, MedioPago.EFECTIVO, 1);
        Pasajero primero = reserva.getPasajeros().get(0);
        assertThrows(ReglaNegocioException.class, () -> reserva.eliminarPasajero(primero));
    }

    @Test
    void unaNotaDeAjusteModificaElTotalPeroSoloSiHayPagos() throws Exception {
        Reserva reserva = e.reservaConAdultos(e.salida, 1);
        assertThrows(ReglaNegocioException.class, () -> reserva.crearNotaAjuste(e.hoy, "error de cobro", -10));
        reserva.crearPago(e.hoy, 100, MedioPago.EFECTIVO, 1);
        reserva.crearNotaAjuste(e.hoy, "error de cobro", -10);
        assertEquals(90.0, reserva.getTotal(), 0.001);
    }

    @Test
    void cancelarConTreintaDiasDeAnticipacionDevuelveElNoventaPorCiento() throws Exception {
        Reserva reserva = e.reservaConAdultos(e.salida, 1);
        reserva.crearPago(e.hoy, 100, MedioPago.EFECTIVO, 1);
        Cancelacion cancelacion = reserva.cancelar(e.hoy, "El cliente no viaja", OrigenCancelacion.CLIENTE);
        assertEquals(90.0, cancelacion.calcularTotalDevuelto(), 0.001);
        assertEquals(EstadoReserva.ANULADA, reserva.getEstado());
    }

    @Test
    void lasDevolucionesSeHacenPorElMismoMedioDeCadaPago() throws Exception {
        Reserva reserva = e.reservaConAdultos(e.salida, 1);
        reserva.crearPago(e.hoy, 50, MedioPago.EFECTIVO, 1);
        reserva.crearPago(e.hoy, 50, MedioPago.TRANSFERENCIA, 1);
        Cancelacion cancelacion = reserva.cancelar(e.hoy, "Cambio de planes", OrigenCancelacion.AGENCIA);
        assertEquals(2, cancelacion.getDevoluciones().size());
        assertEquals(MedioPago.EFECTIVO, cancelacion.getDevoluciones().get(0).getMedioPago());
        assertEquals(MedioPago.TRANSFERENCIA, cancelacion.getDevoluciones().get(1).getMedioPago());
        assertEquals(100.0, cancelacion.calcularTotalDevuelto(), 0.001);   // cancela la agencia: se devuelve todo
    }

    @Test
    void unaReservaAnuladaNoAdmiteCambiosNiPagos() throws Exception {
        Reserva reserva = e.reservaConAdultos(e.salida, 1);
        reserva.cancelar(e.hoy, "Cambio de planes", OrigenCancelacion.CLIENTE);
        assertFalse(reserva.admitePagos());
        assertThrows(ReglaNegocioException.class, () -> e.agregarAdulto(reserva));
        assertThrows(ReglaNegocioException.class, () -> reserva.crearPago(e.hoy, 10, MedioPago.EFECTIVO, 1));
    }
}