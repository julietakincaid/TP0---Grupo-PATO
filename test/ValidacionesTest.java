package prueba;

import excepcion.ReglaNegocioException;
import modelo.Cliente;
import modelo.Guia;
import modelo.Proveedor;
import modelo.Reserva;
import modelo.Vehiculo;
import modelo.Vendedor;
import modelo.enums.CategoriaTour;
import modelo.enums.TipoPasajero;
import modelo.enums.TipoVehiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

// Las validaciones de los datos: cada clase rechaza lo inválido apenas se la crea o se la modifica.
class ValidacionesTest {
    private Escenario e;

    @BeforeEach
    void preparar() throws Exception {
        e = new Escenario();
    }

    @Test
    void unCorreoSinArrobaSeRechaza() {
        assertThrows(IllegalArgumentException.class,
                () -> new Cliente("Beto", "Gil", "40111222", "1144445555", "sin-arroba", "Chile"));
    }

    @Test
    void unaComisionMayorACienSeRechaza() {
        assertThrows(IllegalArgumentException.class,
                () -> new Vendedor(9, "Vale", "21111222", "1144445555", 150));
    }

    @Test
    void unVehiculoSinCapacidadSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> new Vehiculo("ZZ999ZZ", TipoVehiculo.MICRO, 0));
    }

    @Test
    void unTelefonoInvalidoDeProveedorSeRechaza() {
        assertThrows(IllegalArgumentException.class,
                () -> new Proveedor("Hotel Sur", "30-71234567-8", "Ana", "abc", "Alojamiento"));
    }

    @Test
    void unGuiaDebeConservarAlMenosUnIdioma() {
        Guia guia = new Guia(8, "Gabi", "27111999", "1144445555", 1000, "Español", CategoriaTour.CULTURAL);
        assertThrows(ReglaNegocioException.class, () -> guia.quitarIdioma("Español"));
    }

    @Test
    void unaFechaDeNacimientoFuturaSeRechaza() throws Exception {
        Reserva reserva = e.agencia.crearReserva(e.salida, e.cliente, e.vendedor, e.hoy);
        assertThrows(IllegalArgumentException.class,
                () -> e.agregarPasajero(reserva, e.hoy.plusDays(1), TipoPasajero.ADULTO));
    }

    @Test
    void elTipoDePasajeroDebeCorresponderConLaEdad() throws Exception {
        Reserva reserva = e.agencia.crearReserva(e.salida, e.cliente, e.vendedor, e.hoy);
        assertThrows(ReglaNegocioException.class,
                () -> e.agregarPasajero(reserva, e.salida.getFechaInicio().minusYears(9), TipoPasajero.ADULTO));
    }
}