package prueba;

import controlador.ControladorAdministracion;
import controlador.ControladorCatalogo;
import modelo.LiquidacionGuia;
import modelo.Salida;
import modelo.enums.CategoriaTour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import servicio.ServicioLiquidacion;
import servicio.ServicioReportes;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Los controladores del administrador: catálogo (CU01 a CU07) y administración (CU08 y CU09).
class ControladoresDelAdministradorTest {
    private Escenario e;
    private PantallaFalsa pantalla;
    private ControladorCatalogo catalogo;
    private ControladorAdministracion administracion;

    @BeforeEach
    void preparar() throws Exception {
        e = new Escenario();
        pantalla = new PantallaFalsa();
        catalogo = new ControladorCatalogo(e.agencia, pantalla);
        administracion = new ControladorAdministracion(e.agencia, new ServicioLiquidacion(), new ServicioReportes(), pantalla);
    }

    @Test
    void soloElAdministradorRegistraTours() {
        boolean salio = catalogo.registrarTour(e.vendedor, "T-02", "Nuevo", "Circuito", 3, CategoriaTour.CULTURAL, 1, 10);
        assertFalse(salio);
        assertTrue(pantalla.ultimoTexto.contains("Solo el administrador"));
    }

    @Test
    void registrarUnTourConUnCodigoRepetidoMuestraElError() {
        boolean salio = catalogo.registrarTour(e.administrador, "T-01", "Otro", "Circuito", 3, CategoriaTour.CULTURAL, 1, 10);
        assertFalse(salio);
        assertEquals("ERROR", pantalla.ultimoTipo);
    }

    @Test
    void unTourConSalidasVigentesNoCambiaDeDuracion() {
        assertTrue(catalogo.getToursConSalidasVigentes().contains(e.tour));
        assertFalse(catalogo.modificarDuracionTour(e.administrador, "T-01", 5));
        assertEquals("ERROR", pantalla.ultimoTipo);
    }

    @Test
    void unTourSinSalidasSiCambiaDeDuracion() {
        assertTrue(catalogo.registrarTour(e.administrador, "T-02", "Nuevo", "Circuito", 3, CategoriaTour.NATURALEZA, 1, 10));
        assertTrue(catalogo.modificarDuracionTour(e.administrador, "T-02", 4));
        assertEquals("MENSAJE", pantalla.ultimoTipo);
    }

    @Test
    void soloElAdministradorPideReportes() {
        LocalDate desde = e.hoy.minusDays(1);
        LocalDate hasta = e.hoy.plusDays(100);
        assertNull(administracion.reporteOcupacion(e.vendedor, desde, hasta));
        assertEquals("ERROR", pantalla.ultimoTipo);
    }

    @Test
    void unPeriodoInvalidoMuestraElErrorYDevuelveNull() {
        assertNull(administracion.reporteOcupacion(e.administrador, e.hoy.plusDays(100), e.hoy.minusDays(1)));
        assertEquals("ERROR", pantalla.ultimoTipo);
    }

    @Test
    void liquidarUnaSalidaFinalizadaGeneraLaLiquidacion() throws Exception {
        Salida terminada = e.salidaFinalizada();
        LiquidacionGuia liquidacion = administracion.liquidarHonorarios(e.administrador, terminada.getNumero(), e.hoy);
        assertNotNull(liquidacion);
        assertEquals(3001.5, liquidacion.calcularTotal(), 0.001);
        assertTrue(pantalla.ultimoTexto.contains("Se generó la liquidación"));
    }

    @Test
    void noHaySalidasParaLiquidarSiNingunaFinalizo() {
        assertNull(administracion.getSalidasPendientesDeLiquidar());
        assertEquals("ERROR", pantalla.ultimoTipo);
    }
}