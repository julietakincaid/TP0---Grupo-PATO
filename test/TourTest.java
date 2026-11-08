package prueba;

import excepcion.ReglaNegocioException;
import modelo.ServicioAdicional;
import modelo.Tour;
import modelo.TourAventura;
import modelo.enums.CategoriaTour;
import modelo.enums.Temporada;
import modelo.enums.TipoActividad;
import modelo.enums.TipoServicioAdicional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Reglas del tour: itinerario (CU02), tarifas (CU04) y tours de aventura (RN02 y RN15).
class TourTest {
    private final LocalDate hoy = LocalDate.now();
    private Tour tour;

    @BeforeEach
    void preparar() {
        tour = new Tour("T-01", "Museos", "Circuito cultural", 3, CategoriaTour.CULTURAL, 1, 10);
    }

    @Test
    void unHorarioIncoherenteConElOrdenDelDiaSeRechaza() throws Exception {
        tour.agregarActividad(1, 1, TipoActividad.VISITA, LocalTime.of(9, 0), "Visita", "Centro");
        assertThrows(ReglaNegocioException.class,
                () -> tour.agregarActividad(1, 2, TipoActividad.COMIDA, LocalTime.of(8, 0), "Almuerzo", "Plaza"));
    }

    @Test
    void unaActividadFueraDeLaDuracionDelTourSeRechaza() {
        assertThrows(ReglaNegocioException.class,
                () -> tour.agregarActividad(9, 1, TipoActividad.VISITA, LocalTime.of(9, 0), "Visita", "Centro"));
    }

    @Test
    void dosTarifasConFechasSuperpuestasSeRechazan() throws Exception {
        tour.agregarTarifa(Temporada.BAJA, hoy, hoy.plusDays(100), 100);
        assertThrows(ReglaNegocioException.class,
                () -> tour.agregarTarifa(Temporada.MEDIA, hoy.plusDays(50), hoy.plusDays(200), 150));
    }

    @Test
    void unTourNecesitaUnaActividadYUnaTarifaParaCadaTemporada() throws Exception {
        assertThrows(ReglaNegocioException.class, tour::validarConfiguracion);
        tour.agregarActividad(1, 1, TipoActividad.VISITA, LocalTime.of(9, 0), "Visita", "Centro");
        tour.agregarTarifa(Temporada.BAJA, hoy, hoy.plusDays(100), 100);
        assertThrows(ReglaNegocioException.class, tour::validarConfiguracion);   // todavía faltan dos temporadas
        tour.agregarTarifa(Temporada.MEDIA, hoy.plusDays(101), hoy.plusDays(200), 150);
        tour.agregarTarifa(Temporada.ALTA, hoy.plusDays(201), hoy.plusDays(300), 200);
        tour.validarConfiguracion();
    }

    @Test
    void laTarifaVigenteDependeDeLaFecha() throws Exception {
        tour.agregarTarifa(Temporada.BAJA, hoy, hoy.plusDays(100), 100);
        tour.agregarTarifa(Temporada.MEDIA, hoy.plusDays(101), hoy.plusDays(200), 150);
        assertEquals(Temporada.BAJA, tour.obtenerTarifaVigente(hoy.plusDays(10)).getTemporada());
        assertEquals(150.0, tour.obtenerTarifaVigente(hoy.plusDays(150)).getPrecioBase(), 0.001);
        assertThrows(ReglaNegocioException.class, () -> tour.obtenerTarifaVigente(hoy.plusDays(500)));
    }

    @Test
    void unTourDeAventuraNoSeCreaComoTourComun() {
        assertThrows(IllegalArgumentException.class,
                () -> new Tour("T-AV", "Rafting", "Descenso", 2, CategoriaTour.AVENTURA, 1, 5));
    }

    @Test
    void unTourDeAventuraExigeNivelDeDificultad() {
        assertThrows(IllegalArgumentException.class,
                () -> new TourAventura("T-AV", "Rafting", "Descenso", 2, 1, 5, 12, " "));
    }

    @Test
    void elSeguroEsObligatorioEnUnTourDeAventura() throws Exception {
        TourAventura aventura = new TourAventura("T-AV", "Rafting", "Descenso", 2, 1, 5, 12, "Alta");
        ServicioAdicional seguro = new ServicioAdicional(1, "Seguro", "Cobertura", TipoServicioAdicional.SEGURO, 50);
        assertThrows(ReglaNegocioException.class, () -> aventura.agregarServicio(seguro, false));
        aventura.agregarServicio(seguro, true);
        assertThrows(ReglaNegocioException.class, () -> aventura.quitarServicio(seguro));
    }
}