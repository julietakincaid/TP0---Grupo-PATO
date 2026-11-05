package modelo;

import java.time.LocalDate;
import modelo.enums.EstadoSalida;

public class Salida {
    private int numero;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoSalida estado;
    private int cupoMinimo;
    private int cupoMaximo;
    private Tour tour;

    public Salida(int numero, LocalDate fechaInicio, LocalDate fechaFin, int cupoMinimo, int cupoMaximo, Tour tour) {
        setNumero(numero);
        setFechas(fechaInicio, fechaFin);
        setCupos(cupoMinimo, cupoMaximo);
        this.tour = tour;
        this.estado = EstadoSalida.PROGRAMADA;
    }

    public int getNumero() {
        return numero;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public EstadoSalida getEstado() {
        return estado;
    }

    public int getCupoMinimo() {
        return cupoMinimo;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public Tour getTour() {
        return tour;
    }

    private void setNumero(int numero) {
        if (numero <= 0) {
            throw new IllegalArgumentException("El número de salida debe ser mayor a cero.");
        }
        this.numero = numero;
    }

    public void setFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public void setCupos(int cupoMinimo, int cupoMaximo) {
        if (cupoMinimo <= 0 || cupoMaximo < cupoMinimo) {
            throw new IllegalArgumentException("Los cupos ingresados no son válidos.");
        }
        this.cupoMinimo = cupoMinimo;
        this.cupoMaximo = cupoMaximo;
    }
}