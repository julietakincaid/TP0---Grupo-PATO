package modelo;

import excepcion.ReglaNegocioException;
import modelo.enums.CategoriaTour;

import java.time.LocalDate;

public class TourAventura extends Tour{
    private static final String MENSAJE_SEGURO = "En un tour de aventura el seguro de viaje es obligatorio.";

    private int edadMinima;
    private String nivelDificultad;

    public TourAventura(String codigo, String nombre, String descripcion, int duracionDias, int cupoMinimo, int cupoMaximo, int edadMinima, String nivelDificultad) {
        super(codigo, nombre, descripcion, duracionDias, CategoriaTour.AVENTURA, cupoMinimo, cupoMaximo);
        setEdadMinima(edadMinima);
        setNivelDificultad(nivelDificultad);
    }

    public int getEdadMinima() {
        return edadMinima;
    }
    public String getNivelDificultad() {
        return nivelDificultad;
    }

    public void setEdadMinima(int edadMinima) {
        if (edadMinima < 0) {
            throw new IllegalArgumentException("La edad mínima no puede ser negativa.");
        }
        this.edadMinima = edadMinima;
    }

    public void setNivelDificultad(String nivelDificultad) {
        if (nivelDificultad == null || nivelDificultad.isBlank()) {
            throw new IllegalArgumentException("El nivel de dificultad es obligatorio.");
        }
        this.nivelDificultad = nivelDificultad.strip();
    }

    @Override
    public boolean cumpleCondiciones(Pasajero pasajero, LocalDate fechaSalida) {
        return super.cumpleCondiciones(pasajero, fechaSalida)
                && pasajero.calcularEdad(fechaSalida) >= edadMinima;
    }

    // el seguro tiene que estar siempre como servicio obligatorio
    @Override
    public void agregarServicio(ServicioAdicional servicioAdicional, boolean obligatorio) throws ReglaNegocioException {
        if (servicioAdicional != null && servicioAdicional.esSeguro() && !obligatorio) {
            throw new ReglaNegocioException(MENSAJE_SEGURO);
        }
        super.agregarServicio(servicioAdicional, obligatorio);
    }

    @Override
    public void quitarServicio(ServicioAdicional servicioAdicional) throws ReglaNegocioException {
        if (servicioAdicional != null && servicioAdicional.esSeguro()) {
            throw new ReglaNegocioException(MENSAJE_SEGURO);
        }
        super.quitarServicio(servicioAdicional);
    }

    @Override
    public void modificarObligatorio(ServicioAdicional servicioAdicional, boolean obligatorio) throws ReglaNegocioException {
        if (servicioAdicional != null && servicioAdicional.esSeguro() && !obligatorio) {
            throw new ReglaNegocioException(MENSAJE_SEGURO);
        }
        super.modificarObligatorio(servicioAdicional, obligatorio);
    }

    @Override
    public void validarConfiguracion() throws ReglaNegocioException {
        super.validarConfiguracion();

        boolean tieneSeguroObligatorio = false;
        for (ServicioAdicional servicio : getServiciosObligatorios()) {
            if (servicio.esSeguro()) {
                tieneSeguroObligatorio = true;
            }
        }
        if (!tieneSeguroObligatorio) {
            throw new ReglaNegocioException("El tour de aventura debe incluir el seguro de viaje como servicio obligatorio.");
        }
    }
}
