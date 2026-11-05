package modelo;

import java.time.LocalDate;
import modelo.enums.Temporada;

public class Tarifa {
    private Temporada temporada;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private double precioBase;

    public Tarifa(Temporada temporada, LocalDate fechaDesde, LocalDate fechaHasta, double precioBase) {
        this.temporada = temporada;
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

    public void setTemporada(Temporada temporada) {
        this.temporada = temporada;
    }

    public void setFechas(LocalDate fechaDesde, LocalDate fechaHasta) {
        if (fechaDesde.isAfter(fechaHasta)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
    }

    public void setPrecioBase(double precioBase) {
        if (precioBase <= 0) {
            throw new IllegalArgumentException("El precio base debe ser mayor a cero.");
        }
        this.precioBase = precioBase;
    }

    public boolean estaVigente(LocalDate fecha) {
        return !fecha.isBefore(fechaDesde) && !fecha.isAfter(fechaHasta);
    }
}
