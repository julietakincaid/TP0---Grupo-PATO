package modelo;

import java.time.LocalDate;

public abstract class Promocion {

    private String nombre;
    private double porcentajeDescuento;

    public Promocion(String nombre, double porcentajeDescuento) {
        this.nombre = nombre;
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public abstract boolean esAplicable(
            LocalDate fechaReserva,
            LocalDate fechaSalida,
            int cantidadPasajeros
    );

    public double calcularDescuento(double subtotal) {
        return subtotal * porcentajeDescuento;
    }
}
