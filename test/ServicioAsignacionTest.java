package prueba;

import excepcion.AsignacionInvalidaException;
import modelo.Guia;
import modelo.Salida;
import modelo.Vehiculo;
import modelo.enums.CategoriaTour;
import modelo.enums.TipoVehiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import servicio.ServicioAsignacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Asignación de guías y vehículos a las salidas (CU11, RN24, RN25 y RN26).
class ServicioAsignacionTest {
    private Escenario e;
    private final ServicioAsignacion asignacion = new ServicioAsignacion();

    @BeforeEach
    void preparar() throws Exception {
        e = new Escenario();
    }

    @Test
    void seAsignaUnGuiaHabilitadoParaLaCategoriaDelTour() throws Exception {
        asignacion.asignarGuia(e.salida, e.guia, e.agencia.getSalidas());
        assertEquals(e.guia, e.salida.getGuia());
    }

    @Test
    void unGuiaNoHabilitadoParaLaCategoriaSeRechaza() {
        Guia deAventura = new Guia(9, "Ale", "27111223", "1144445555", 900, "Español", CategoriaTour.AVENTURA);
        assertThrows(AsignacionInvalidaException.class,
                () -> asignacion.asignarGuia(e.salida, deAventura, e.agencia.getSalidas()));
    }

    @Test
    void unGuiaNoPuedeTenerDosSalidasQueSeSuperponen() throws Exception {
        Salida otra = e.nuevaSalida(30, 1, 10);   // mismas fechas que la salida del escenario
        asignacion.asignarGuia(e.salida, e.guia, e.agencia.getSalidas());
        assertThrows(AsignacionInvalidaException.class,
                () -> asignacion.asignarGuia(otra, e.guia, e.agencia.getSalidas()));
    }

    @Test
    void unVehiculoConMenosCapacidadQueElCupoDeLaSalidaSeRechaza() {
        Vehiculo chico = new Vehiculo("ZZ999ZZ", TipoVehiculo.CAMIONETA, 5);   // la salida admite hasta 10
        assertThrows(AsignacionInvalidaException.class,
                () -> asignacion.asignarVehiculo(e.salida, chico, e.agencia.getSalidas()));
    }

    @Test
    void unVehiculoNoPuedeTenerDosSalidasQueSeSuperponen() throws Exception {
        Salida otra = e.nuevaSalida(30, 1, 10);
        asignacion.asignarVehiculo(e.salida, e.combi, e.agencia.getSalidas());
        assertThrows(AsignacionInvalidaException.class,
                () -> asignacion.asignarVehiculo(otra, e.combi, e.agencia.getSalidas()));
    }
}