package modelo;

import java.time.LocalDate;
import java.io.Serializable;

public class LiquidacionGuia implements Serializable {
    private int numero;
    private LocalDate fecha;
    private double honorarios;
    private double comisionAdicionales;
    private Guia guia;

    LiquidacionGuia(int numero, LocalDate fecha, double honorarios, double comisionAdicionales, Guia guia) {
        setNumero(numero);
        setFecha(fecha);
        setHonorarios(honorarios);
        setComisionAdicionales(comisionAdicionales);
        setGuia(guia);
    }

    public int getNumero() {return numero;}
    public LocalDate getFecha() {return fecha;}
    public double getHonorarios() {return honorarios;}
    public double getComisionAdicionales() {return comisionAdicionales;}
    public Guia getGuia() {return guia;}

    // todos los setters son privados: una liquidación emitida no se modifica
    private void setNumero(int numero) {
        if (numero <= 0) {
            throw new IllegalArgumentException("El número de liquidación debe ser positivo.");
        }
        this.numero = numero;
    }

    private void setFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de liquidación es obligatoria.");
        }
        this.fecha = fecha;
    }

    private void setHonorarios(double honorarios) {
        if (!Double.isFinite(honorarios) || honorarios < 0) {
            throw new IllegalArgumentException("Los honorarios no pueden ser negativos ni inválidos.");
        }
        this.honorarios = honorarios;
    }

    private void setComisionAdicionales(double comisionAdicionales) {
        if (!Double.isFinite(comisionAdicionales) || comisionAdicionales < 0) {
            throw new IllegalArgumentException("La comisión no puede ser negativa ni inválida.");
        }
        this.comisionAdicionales = comisionAdicionales;
    }

    private void setGuia(Guia guia) {
        if (guia == null) {
            throw new IllegalArgumentException("La liquidación debe tener un guía asignado.");
        }
        this.guia = guia;
    }

    public double calcularTotal() {
        return honorarios + comisionAdicionales;
    }

    // dos liquidaciones son la misma si tienen el mismo número
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LiquidacionGuia)) {
            return false;
        }
        LiquidacionGuia otra = (LiquidacionGuia) o;
        return numero == otra.numero;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(numero);
    }

    @Override
    public String toString() {
        return "Liquidación N° " + numero + " (" + fecha + ") - Guía: " + guia.getNombre()
                + " - Total: $" + calcularTotal();
    }
}
