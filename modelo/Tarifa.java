import java.time.LocalDate;
import modelo.enums.Temporada;
import java.io.Serializable;

public class Tarifa implements Serializable{
    private Temporada temporada;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private double precioBase;

    Tarifa(Temporada temporada, LocalDate fechaDesde, LocalDate fechaHasta, double precioBase) {
        setTemporada(temporada);
        setFechas(fechaDesde, fechaHasta);
        setPrecioBase(precioBase);
    }

    public Temporada getTemporada() {
        return temporada;
    }
    public LocalDate getFechaDesde() {
        return fechaDesde;
    }
    public LocalDate getFechaHasta() {
        return fechaHasta;
    }
    public double getPrecioBase() {
        return precioBase;
    }

    void setTemporada(Temporada temporada) {
        if (temporada == null) {
            throw new IllegalArgumentException("La temporada es obligatoria.");
        }
        this.temporada = temporada;
    }

    void setFechas(LocalDate fechaDesde, LocalDate fechaHasta) {
        if (fechaDesde == null || fechaHasta == null) {
            throw new IllegalArgumentException("Las fechas son obligatorias.");
        }

        if (fechaDesde.isAfter(fechaHasta)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
    }

    public void setPrecioBase(double precioBase) {
        if (!Double.isFinite(precioBase) || precioBase <= 0) {
            throw new IllegalArgumentException("El precio base debe ser mayor a cero.");
        }
        this.precioBase = precioBase;
    }

    public boolean estaVigente(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        return !fecha.isBefore(fechaDesde) && !fecha.isAfter(fechaHasta);
    }

    @Override
    public String toString() {
        return temporada + ": " + fechaDesde + " a " + fechaHasta + " - $" + precioBase;
    }
}
