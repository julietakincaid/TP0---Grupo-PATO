package modelo;

import java.time.LocalDate;

public class LiquidacionGuia {
    private int numero;
    private LocalDate fecha;
    private double honorarios;
    private double comisionAdicionales;
    private final Guia guia;

    public LiquidacionGuia(int numero, LocalDate fecha, Guia guia, double honorarios, double comisionAdicionales) {
        if (guia == null) {
            throw new IllegalArgumentException("La liquidacion debe corresponder a un guia.");
        }
        setNumero(numero);
        setFecha(fecha);
        setHonorarios(honorarios);
        setComisionAdicionales(comisionAdicionales);
        this.guia = guia;
    }

    public int getNumero() {
        return numero;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public double getHonorarios() {
        return honorarios;
    }

    public double getComisionAdicionales() {
        return comisionAdicionales;
    }

    public Guia getGuia() {
        return guia;
    }

    private void setNumero(int numero) {
        if (numero <= 0) {
            throw new IllegalArgumentException("El numero de liquidacion debe ser positivo.");
        }
        this.numero = numero;
    }

    public void setFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de liquidacion es obligatoria.");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de liquidacion no puede ser futura.");
        }
        this.fecha = fecha;
    }

    public void setHonorarios(double honorarios) {
        if (honorarios < 0) {
            throw new IllegalArgumentException("Los honorarios no pueden ser negativos.");
        }
        this.honorarios = honorarios;
    }

    public void setComisionAdicionales(double comisionAdicionales) {
        if (comisionAdicionales < 0) {
            throw new IllegalArgumentException("La comision no puede ser negativa.");
        }
        this.comisionAdicionales = comisionAdicionales;
    }

    public double calcularTotal() {
        return honorarios + comisionAdicionales;
    }
}
