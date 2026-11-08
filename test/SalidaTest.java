package prueba;

import excepcion.ReglaNegocioException;
import modelo.Reserva;
import modelo.Salida;
import modelo.enums.EstadoReserva;
import modelo.enums.EstadoSalida;
import modelo.enums.MedioPago;
import modelo.enums.OrigenCancelacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Reglas de la salida: estados según la ocupación (RN06), reservas, inicio y fin (RN27 y RN28) y cupo mínimo (RN07).
class SalidaTest {
    private Escenario e;

    @BeforeEach
    void preparar() throws Exception {
        e = new Escenario();
    }

    @Test
    void unaSalidaNuevaEstaProgramada() {
        assertEquals(EstadoSalida.PROGRAMADA, e.salida.getEstado());
    }

    @Test
    void alAlcanzarElCupoMinimoLaSalidaSeConfirma() throws Exception {
        Salida salida = e.nuevaSalida(30, 2, 4);
        Reserva reserva = e.reservaConAdultos(salida, 1);
        assertEquals(EstadoSalida.PROGRAMADA, salida.getEstado());
        e.agregarAdulto(reserva);
        assertEquals(EstadoSalida.CONFIRMADA, salida.getEstado());
    }

    @Test
    void alAlcanzarElCupoMaximoLaSalidaQuedaCompletaYNoAdmiteReservas() throws Exception {
        Salida salida = e.nuevaSalida(30, 1, 2);
        e.reservaConAdultos(salida, 2);
        assertEquals(EstadoSalida.COMPLETA, salida.getEstado());
        assertThrows(ReglaNegocioException.class,
                () -> e.agencia.crearReserva(salida, e.cliente, e.vendedor, e.hoy));
    }

    @Test
    void noSeReservaConMenosDeDiezDiasDeAnticipacion() {
        assertThrows(ReglaNegocioException.class,
                () -> e.agencia.crearReserva(e.salida, e.cliente, e.vendedor, e.salida.getFechaInicio().minusDays(9)));
    }

    @Test
    void unaReservaAnuladaLiberaSusLugares() throws Exception {
        Reserva reserva = e.reservaConAdultos(e.salida, 2);
        assertEquals(2, e.salida.calcularOcupacion());
        reserva.cancelar(e.hoy, "Cambio de planes", OrigenCancelacion.CLIENTE);
        assertEquals(0, e.salida.calcularOcupacion());
    }

    @Test
    void laFechaDeFinDebeCoincidirConLaDuracionDelTour() {
        assertThrows(ReglaNegocioException.class,
                () -> e.agencia.programarSalida(e.hoy.plusDays(30), e.hoy.plusDays(40), 1, 10, e.tour));
    }

    @Test
    void losCuposDeLaSalidaNoPuedenSuperarLosDelTour() {
        assertThrows(ReglaNegocioException.class, () -> e.nuevaSalida(30, 1, 20));
    }

    // ---------- iniciar y finalizar ----------

    @Test
    void noSePuedeIniciarUnaSalidaAntesDeSuFechaDeInicio() throws Exception {
        Salida lista = e.salidaLista();
        assertThrows(ReglaNegocioException.class, () -> lista.iniciarSalida(e.hoy));
        assertEquals(EstadoSalida.CONFIRMADA, lista.getEstado());
    }

    @Test
    void laSalidaSeIniciaYSeFinalizaDesdeSuFechaDeInicio() throws Exception {
        Salida lista = e.salidaLista();
        lista.iniciarSalida(lista.getFechaInicio());
        assertEquals(EstadoSalida.EN_CURSO, lista.getEstado());
        lista.finalizarSalida();
        assertEquals(EstadoSalida.FINALIZADA, lista.getEstado());
        assertEquals(EstadoReserva.FINALIZADA, lista.getReservas().get(0).getEstado());   // RF22: termina las reservas
    }

    @Test
    void noSeIniciaUnaSalidaSinGuiaAsignado() throws Exception {
        Salida sinGuia = e.nuevaSalida(30, 1, 10);
        e.reservaPagada(sinGuia, 1);
        assertThrows(ReglaNegocioException.class, () -> sinGuia.iniciarSalida(sinGuia.getFechaInicio()));
    }

    @Test
    void noSeIniciaUnaSalidaSinLaListaDePasajerosEmitida() throws Exception {
        Salida sinLista = e.nuevaSalida(30, 1, 10);
        e.reservaPagada(sinLista, 1);
        e.asignarGuiaYVehiculo(sinLista);
        assertThrows(ReglaNegocioException.class, () -> sinLista.iniciarSalida(sinLista.getFechaInicio()));
    }

    @Test
    void noSeFinalizaUnaSalidaQueNoEstaEnCurso() throws Exception {
        Salida lista = e.salidaLista();
        assertThrows(ReglaNegocioException.class, lista::finalizarSalida);
    }

    // ---------- cupo mínimo (RN07) ----------

    @Test
    void unaSalidaSinElCupoMinimoSeCancelaSieteDiasAntesDeSuInicio() throws Exception {
        Salida salida = e.agencia.programarSalida(e.hoy.plusDays(5), e.hoy.plusDays(7), 3, 10, e.tour);
        Reserva reserva = e.agencia.crearReserva(salida, e.cliente, e.vendedor, e.hoy.minusDays(6));
        e.agregarAdulto(reserva);
        e.calculadora.recalcularTotal(reserva);
        reserva.crearPago(e.hoy.minusDays(6), reserva.getTotal(), MedioPago.EFECTIVO, 1);

        List<Salida> canceladas = e.agencia.verificarCuposMinimos(e.hoy);

        assertTrue(canceladas.contains(salida));
        assertEquals(EstadoSalida.CANCELADA, salida.getEstado());
    }
}