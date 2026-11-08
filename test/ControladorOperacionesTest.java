package prueba;

import controlador.ControladorOperaciones;
import modelo.Guia;
import modelo.Salida;
import modelo.enums.CategoriaTour;
import modelo.enums.EstadoSalida;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import servicio.ServicioAsignacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// El controlador del coordinador de operaciones: programar, asignar, emitir la lista, iniciar y finalizar salidas.
class ControladorOperacionesTest {
    private Escenario e;
    private PantallaFalsa pantalla;
    private ControladorOperaciones controlador;

    @BeforeEach
    void preparar() throws Exception {
        e = new Escenario();
        pantalla = new PantallaFalsa();
        controlador = new ControladorOperaciones(e.agencia, new ServicioAsignacion(), pantalla);
    }

    @Test
    void soloElCoordinadorProgramaSalidas() {
        boolean salio = controlador.programarSalida(e.vendedor, "T-01", e.hoy.plusDays(40), e.hoy.plusDays(42), 1, 8);
        assertFalse(salio);
        assertEquals("ERROR", pantalla.ultimoTipo);
        assertTrue(pantalla.ultimoTexto.contains("Solo el coordinador"));
    }

    @Test
    void programarUnaSalidaMuestraSuNumero() {
        boolean salio = controlador.programarSalida(e.coordinador, "T-01", e.hoy.plusDays(40), e.hoy.plusDays(42), 1, 8);
        assertTrue(salio);
        assertEquals("MENSAJE", pantalla.ultimoTipo);
        assertTrue(pantalla.ultimoTexto.contains("Se programó la salida N°"));
    }

    @Test
    void unGuiaNoHabilitadoParaLaCategoriaMuestraElError() throws Exception {
        e.agencia.registrarPersonal(new Guia(9, "Ale", "27111223", "1144445555", 900, "Español", CategoriaTour.AVENTURA));
        boolean salio = controlador.asignarGuia(e.coordinador, e.salida.getNumero(), 9);
        assertFalse(salio);
        assertEquals("ERROR", pantalla.ultimoTipo);
    }

    @Test
    void emitirLaListaSinReservasConfirmadasDevuelveNullYMuestraElError() {
        assertNull(controlador.emitirListaPasajeros(e.coordinador, e.salida.getNumero()));
        assertEquals("ERROR", pantalla.ultimoTipo);
    }

    @Test
    void noSePuedeIniciarUnaSalidaAntesDeSuFechaDeInicio() throws Exception {
        Salida lista = e.salidaLista();
        boolean salio = controlador.iniciarSalida(e.coordinador, lista.getNumero(), e.hoy);
        assertFalse(salio);
        assertEquals("ERROR", pantalla.ultimoTipo);
        assertTrue(pantalla.ultimoTexto.contains("empieza el"));
    }

    @Test
    void elCoordinadorIniciaYFinalizaLaSalidaCuandoLlegaLaFecha() throws Exception {
        Salida lista = e.salidaLista();
        assertTrue(controlador.iniciarSalida(e.coordinador, lista.getNumero(), lista.getFechaInicio()));
        assertTrue(pantalla.ultimoTexto.contains("está en curso"));
        assertTrue(controlador.finalizarSalida(e.coordinador, lista.getNumero()));
        assertEquals(EstadoSalida.FINALIZADA, lista.getEstado());
    }
}