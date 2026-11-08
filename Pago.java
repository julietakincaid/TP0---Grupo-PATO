package modelo;

import java.time.LocalDate;
import modelo.enums.MedioPago;
import java.io.Serializable;

public class Pago implements Serializable {
    private static final double RECARGO_TARJETA = 0.07;

    private int numero;
    private LocalDate fecha;
    private double importe;
    private MedioPago medioPago;
    private int cantidadCuotas;

    Pago(int numero, LocalDate fecha, double importe, MedioPago medioPago, int cantidadCuotas) {
        setNumero(numero);
        setFecha(fecha);
        setImporte(importe);
        setMedioPago(medioPago);
        setCantidadCuotas(cantidadCuotas);
    }

    public int getNumero() {
        return numero;
    }
    public LocalDate getFecha() {
        return fecha;
    }
    public double getImporte() {
        return importe;
    }
    public MedioPago getMedioPago() {
        return medioPago;
    }
    public int getCantidadCuotas() {
        return cantidadCuotas;
    }

    private void setNumero(int numero) {
        if (numero <= 0) {
            throw new IllegalArgumentException("El numero de pago debe ser positivo.");
        }
        this.numero = numero;
    }

    private void setFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de pago es obligatoria.");
        }
        this.fecha = fecha;
    }

    private void setImporte(double importe) {
        if (!Double.isFinite(importe) || importe <= 0) {
            throw new IllegalArgumentException("El importe debe ser mayor a cero.");
        }
        this.importe = importe;
    }

    private void setMedioPago(MedioPago medioPago) {
        if (medioPago == null) {
            throw new IllegalArgumentException("El medio de pago es obligatorio.");
        }
        this.medioPago = medioPago;
    }

    private void setCantidadCuotas(int cantidadCuotas) {
        if (cantidadCuotas <= 0) {
            throw new IllegalArgumentException("La cantidad de cuotas debe ser positiva.");
        }
        this.cantidadCuotas = cantidadCuotas;
    }

    public double calcularRecargo() {
        if (medioPago == MedioPago.TARJETA && cantidadCuotas > 1) {
            return redondear(importe * RECARGO_TARJETA);
        }
        return 0;
    }

    // lo que realmente se le cobra al cliente: importe más recargo
    public double calcularTotalPago() {
        return redondear(importe + calcularRecargo());
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    @Override
    public String toString() {
        return "Pago N° " + numero + " (" + fecha + ") - $" + importe + " por " + medioPago
                + (cantidadCuotas > 1 ? " en " + cantidadCuotas + " cuotas" : "");
    }
}
