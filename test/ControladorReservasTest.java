package prueba;

import controlador.ControladorReservas;
import modelo.Cancelacion;
import modelo.Pago;
import modelo.Reserva;
import modelo.enums.MedioPago;
import modelo.enums.OrigenCancelacion;
import modelo.enums.TipoPasajero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// El controlador del vendedor: controla el rol, delega en el modelo y, si algo falla, le ordena a la pantalla
// mostrar el error (nunca lanza excepciones hacia la vista).
class ControladorReservasTest {
    private Escenario e;
    private PantallaFalsa pantalla;
    private ControladorReservas controlador;

    @BeforeEach
    void preparar() throws Exception {
        e = new Escenario();
        pantalla = new PantallaFalsa();
        controlador = new ControladorReservas(e.agencia, e.calculadora, pantalla);
    }

    @Test
    void soloUnVendedorPuedeRegistrarClientes() {
        boolean salio = controlador.registrarCliente(e.administrador, "Beto", "Gil", "40111222",
                "1144445555", "beto@mail.com", "Chile");
        assertFalse(salio);
        assertEquals("ERROR", pantalla.ultimoTipo);
        assertTrue(pantalla.ultimoTexto.contains("Solo un vendedor"));
    }

    @Test
    void registrarUnClienteMuestraElMensajeDeExito() {
        boolean salio = controlador.registrarCliente(e.vendedor, "Beto", "Gil", "40111222",
                "1144445555", "beto@mail.com", "Chile");
        assertTrue(salio);
        assertEquals("MENSAJE", pantalla.ultimoTipo);
        assertTrue(pantalla.ultimoTexto.contains("Cliente registrado"));
    }

    @Test
    void unClienteRepetidoMuestraElErrorSinLanzarExcepcion() {
        boolean salio = controlador.registrarCliente(e.vendedor, "Otra", "Gil", "30111222",
                "1144445555", "otra@mail.com", "Chile");   // el documento de Ana ya está registrado
        assertFalse(salio);
        assertEquals("ERROR", pantalla.ultimoTipo);
    }

    @Test
    void unCorreoInvalidoTambienMuestraElError() {
        boolean salio = controlador.registrarCliente(e.vendedor, "Beto", "Gil", "40111222",
                "1144445555", "sin-arroba", "Chile");
        assertFalse(salio);
        assertEquals("ERROR", pantalla.ultimoTipo);
    }

    @Test
    void crearLaReservaYAgregarPasajerosRecalculaElTotal() {
        Reserva reserva = controlador.crearReserva(e.vendedor, "30111222", e.salida.getNumero(), e.hoy);
        assertNotNull(reserva);
        assertTrue(controlador.agregarPasajero(e.vendedor, reserva.getNumero(), "Ana", "Perez", "50111222",
                LocalDate.of(1990, 1, 1), TipoPasajero.ADULTO, "Contacto 11-1111", null, null));
        assertTrue(controlador.agregarPasajero(e.vendedor, reserva.getNumero(), "Luis", "Gomez", "50222333",
                LocalDate.of(1991, 1, 1), TipoPasajero.ADULTO, "Contacto 11-1111", null, null));
        assertEquals(200.0, reserva.getTotal(), 0.001);
    }

    @Test
    void crearUnaReservaParaUnClienteInexistenteMuestraElError() {
        assertNull(controlador.crearReserva(e.vendedor, "99999999", e.salida.getNumero(), e.hoy));
        assertEquals("ERROR", pantalla.ultimoTipo);
    }

    @Test
    void unPagoMayorAlSaldoDevuelveNullYMuestraElError() throws Exception {
        Reserva reserva = e.reservaConAdultos(e.salida, 1);
        Pago pago = controlador.registrarPago(e.vendedor, reserva.getNumero(), 500, MedioPago.EFECTIVO, 1, e.hoy);
        assertNull(pago);
        assertEquals("ERROR", pantalla.ultimoTipo);
        assertTrue(pantalla.ultimoTexto.contains("supera el saldo"));
    }

    @Test
    void cancelarUnaReservaDevuelveLaCancelacionConLoQueSeDevuelve() throws Exception {
        Reserva reserva = e.reservaPagada(e.salida, 1);
        Cancelacion cancelacion = controlador.cancelarReserva(e.vendedor, reserva.getNumero(), "No viaja",
                OrigenCancelacion.CLIENTE, e.hoy);
        assertNotNull(cancelacion);
        assertEquals(90.0, cancelacion.calcularTotalDevuelto(), 0.001);
    }

    @Test
    void unVendedorNoPuedeCancelarPorFaltaDePago() throws Exception {
        Reserva reserva = e.reservaPagada(e.salida, 1);
        assertNull(controlador.cancelarReserva(e.vendedor, reserva.getNumero(), "x",
                OrigenCancelacion.FALTA_PAGO, e.hoy));   // la falta de pago la registra el sistema
        assertEquals("ERROR", pantalla.ultimoTipo);
    }

    @Test
    void paraCobrarSeOfrecenSoloLasReservasConSaldo() throws Exception {
        e.reservaPagada(e.salida, 1);                       // ya pagada: no se ofrece
        Reserva conSaldo = e.reservaConAdultos(e.salida, 1);  // con saldo: sí
        assertEquals(1, controlador.getReservasConSaldo().size());
        assertEquals(conSaldo, controlador.getReservasConSaldo().get(0));
    }

    @Test
    void sinReservasConSaldoElControladorAvisaYDevuelveNull() {
        assertNull(controlador.getReservasConSaldo());
        assertEquals("ERROR", pantalla.ultimoTipo);
    }

    @Test
    void buscarUnaReservaInexistenteDevuelveNullYMuestraElError() {
        assertNull(controlador.buscarReserva(99));
        assertEquals("ERROR", pantalla.ultimoTipo);
    }
}