package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import modelo.enums.EstadoReserva;
import modelo.enums.EstadoSalida;

public class Salida {
    private int numero;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoSalida estado;
    private int cupoMinimo;
    private int cupoMaximo;
    private Tour tour;
    private final List<Reserva> reservas = new ArrayList<>();

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


    void agregarReserva(Reserva reserva) {
        reservas.add(reserva);
    }


    public int calcularOcupacion() {
        int ocupados = 0;
        for (Reserva r : reservas) {
            if (r.getEstado() != EstadoReserva.ANULADA) {
                ocupados += r.cantidadPasajeros();
            }
        }
        return ocupados;
    }

    public boolean verificarCupo(int cantidad) {
        return calcularOcupacion() + cantidad <= cupoMaximo;
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
