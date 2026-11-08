package prueba;

import excepcion.ReglaNegocioException;
import modelo.Cliente;
import modelo.Reserva;
import modelo.Salida;
import modelo.Tour;
import modelo.Vehiculo;
import modelo.enums.CategoriaTour;
import modelo.enums.EstadoReserva;
import modelo.enums.MedioPago;
import modelo.enums.OrigenCancelacion;
import modelo.enums.TipoVehiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// La agencia controla las claves únicas y las reglas que cruzan varias clases (RN24 a RN26, RN17, RF01).
class AgenciaTest {
    private Escenario e;

    @BeforeEach
    void preparar() throws Exception {
        e = new Escenario();
    }

    @Test
    void unDocumentoDeClienteRepetidoSeRechazaAunEscritoConPuntos() {
        Cliente repetido = new Cliente("Otra", "Gil", "30.111.222", "1144445555", "otra@mail.com", "Chile");
        assertThrows(ReglaNegocioException.class, () -> e.agencia.registrarCliente(repetido));
    }

    @Test
    void unCodigoDeTourRepetidoSeRechaza() {
        Tour repetido = new Tour("T-01", "Otro", "Otro circuito", 2, CategoriaTour.NATURALEZA, 1, 5);
        assertThrows(ReglaNegocioException.class, () -> e.agencia.registrarTour(repetido));
    }

    @Test
    void unaPatenteRepetidaSeRechazaAunEscritaConOtroFormato() {
        Vehiculo repetido = new Vehiculo("ab-123-cd", TipoVehiculo.MICRO, 30);
        assertThrows(ReglaNegocioException.class, () -> e.agencia.registrarVehiculo(repetido));
    }

    @Test
    void unTourConSalidasVigentesNoSeDaDeBaja() throws Exception {
        assertThrows(ReglaNegocioException.class, () -> e.agencia.darDeBajaTour(e.tour));
        Tour sinSalidas = new Tour("T-02", "Otro", "Otro circuito", 2, CategoriaTour.NATURALEZA, 1, 5);
        e.agencia.registrarTour(sinSalidas);
        e.agencia.darDeBajaTour(sinSalidas);
        assertFalse(sinSalidas.estaActivo());
    }

    @Test
    void laCapacidadDeUnVehiculoNoPuedeBajarDelCupoDeSuSalida() throws Exception {
        e.asignarGuiaYVehiculo(e.salida);                          // el cupo máximo de la salida es 10
        assertThrows(ReglaNegocioException.class, () -> e.agencia.modificarCapacidadVehiculo(e.combi, 5));
        e.agencia.modificarCapacidadVehiculo(e.combi, 10);         // igual al cupo: se permite
        assertEquals(10, e.combi.getCapacidad());
    }

    @Test
    void unGuiaNoPierdeLaCategoriaDeUnaSalidaQueTieneAsignada() throws Exception {
        e.guia.agregarCategoria(CategoriaTour.NATURALEZA);
        e.asignarGuiaYVehiculo(e.salida);                          // es una salida cultural
        assertThrows(ReglaNegocioException.class,
                () -> e.agencia.quitarCategoriaGuia(e.guia, CategoriaTour.CULTURAL));
        e.agencia.quitarCategoriaGuia(e.guia, CategoriaTour.NATURALEZA);
        assertEquals(1, e.guia.getCategoriasHabilitadas().size());
    }

    @Test
    void lasReservasConDeudaVencidaSeAnulanPorFaltaDePago() throws Exception {
        Salida salida = e.agencia.programarSalida(e.hoy.plusDays(5), e.hoy.plusDays(7), 1, 10, e.tour);
        Reserva reserva = e.agencia.crearReserva(salida, e.cliente, e.vendedor, e.hoy.minusDays(6));
        e.agregarAdulto(reserva);
        e.calculadora.recalcularTotal(reserva);
        reserva.crearPago(e.hoy.minusDays(6), 30, MedioPago.EFECTIVO, 1);   // solo la seña: falta el saldo

        List<Reserva> anuladas = e.agencia.verificarPlazosPago(e.hoy);       // el saldo vencía 10 días antes de la salida

        assertTrue(anuladas.contains(reserva));
        assertEquals(EstadoReserva.ANULADA, reserva.getEstado());
        assertEquals(OrigenCancelacion.FALTA_PAGO, reserva.getCancelacion().getOrigen());
    }
}