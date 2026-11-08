package prueba;

import excepcion.ReglaNegocioException;
import modelo.LiquidacionGuia;
import modelo.Reserva;
import modelo.Salida;
import modelo.enums.MedioPago;
import modelo.enums.OrigenCancelacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import servicio.ServicioLiquidacion;
import servicio.ServicioReportes;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Liquidación de honorarios del guía (RN29) y reportes del administrador (CU09).
class LiquidacionYReportesTest {
    private Escenario e;
    private final ServicioLiquidacion liquidacion = new ServicioLiquidacion();
    private final ServicioReportes reportes = new ServicioReportes();
    private LocalDate desde;
    private LocalDate hasta;

    @BeforeEach
    void preparar() throws Exception {
        e = new Escenario();
        desde = e.hoy.minusDays(1);
        hasta = e.hoy.plusDays(1);
    }

    @Test
    void laLiquidacionSumaLosDiasMasElCincoPorCientoDeLosAdicionales() throws Exception {
        Salida terminada = e.salidaFinalizada();
        LiquidacionGuia resultado = liquidacion.generarLiquidacion(terminada, 1, e.hoy);
        assertEquals(3001.5, resultado.calcularTotal(), 0.001);   // 3 días x $1000 + 5% de la excursión de $30
    }

    @Test
    void unaSalidaSeLiquidaUnaSolaVez() throws Exception {
        Salida terminada = e.salidaFinalizada();
        liquidacion.generarLiquidacion(terminada, 1, e.hoy);
        assertThrows(ReglaNegocioException.class, () -> liquidacion.generarLiquidacion(terminada, 2, e.hoy));
    }

    @Test
    void noSeLiquidaUnaSalidaQueTodaviaNoFinalizo() {
        assertThrows(ReglaNegocioException.class, () -> liquidacion.generarLiquidacion(e.salida, 1, e.hoy));
    }

    @Test
    void lasVentasContabilizanSoloLasReservasConfirmadasOFinalizadas() throws Exception {
        e.reservaConAdultos(e.salida, 1);        // pendiente: no es una venta
        e.reservaPagada(e.salida, 2);            // confirmada: sí
        assertEquals(1, reportes.contarVentas(e.agencia.getSalidas(), e.vendedor, desde, hasta));
        assertEquals(200.0, reportes.calcularTotalVendido(e.agencia.getSalidas(), e.vendedor, desde, hasta), 0.001);
    }

    @Test
    void losIngresosNetosDescuentanLasDevoluciones() throws Exception {
        Reserva reserva = e.reservaConAdultos(e.salida, 1);
        reserva.crearPago(e.hoy, 100, MedioPago.EFECTIVO, 1);
        reserva.cancelar(e.hoy, "Cambio de planes", OrigenCancelacion.CLIENTE);   // a 30 días devuelve el 90%
        assertEquals(100.0, reportes.calcularIngresosCobrados(e.agencia.getSalidas(), e.tour, desde, hasta), 0.001);
        assertEquals(90.0, reportes.calcularDevoluciones(e.agencia.getSalidas(), e.tour, desde, hasta), 0.001);
        assertEquals(10.0, reportes.calcularIngresosNetos(e.agencia.getSalidas(), e.tour, desde, hasta), 0.001);
    }

    @Test
    void unPeriodoInvertidoSeRechaza() {
        assertThrows(IllegalArgumentException.class,
                () -> reportes.reporteOcupacion(e.agencia.getSalidas(), hasta, desde));
    }
}