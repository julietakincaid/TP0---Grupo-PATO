package modelo;

import java.io.Serializable;

public abstract class Promocion implements Serializable {
    private String nombre;
    private double porcentajeDescuento;    // de 0 a 1 (0.15 equivale al 15%)

    public Promocion(String nombre, double porcentajeDescuento) {
        setNombre(nombre);
        setPorcentajeDescuento(porcentajeDescuento);
    }

    public String getNombre() {
        return nombre;
    }
    public double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    private void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la promoción es obligatorio.");
        }
        this.nombre = nombre.strip();
    }

    public void setPorcentajeDescuento(double porcentajeDescuento) {
        if (!Double.isFinite(porcentajeDescuento) || porcentajeDescuento < 0 || porcentajeDescuento > 1) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 1.");
        }
        this.porcentajeDescuento = porcentajeDescuento;
    }

    // cada tipo de promoción define su propia condición (polimorfismo)
    public abstract boolean esAplicable(int cantidadPasajeros, int diasAnticipacion);

    // dos promociones son la misma si tienen el mismo nombre
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Promocion)) {
            return false;
        }
        Promocion otra = (Promocion) o;
        return nombre.equals(otra.nombre);
    }

    @Override
    public int hashCode() {
        return nombre.hashCode();
    }

    @Override
    public String toString() {
        return nombre + " (" + Math.round(porcentajeDescuento * 100) + "%)";
    }
}
