package modelo;

import modelo.enums.MedioPago;
import java.io.Serializable;
import java.time.LocalDate;

public class Devolucion implements Serializable {
    private int numero;
    private LocalDate fecha;
    private double importe;
    private Pago pago;

    Devolucion(int numero, LocalDate fecha, double importe, Pago pago) {
        setNumero(numero);
        setFecha(fecha);
        setImporte(importe);
        setPago(pago);
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
    public Pago getPago() {
        return pago;
    }

    // la devolución se hace por el mismo medio del pago original
    public MedioPago getMedioPago() {
        return pago.getMedioPago();
    }

    private void setNumero(int numero) {
        if (numero <= 0) {
            throw new IllegalArgumentException("El número de devolución debe ser positivo.");
        }
        this.numero = numero;
    }

    private void setFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de devolución es obligatoria.");
        }
        this.fecha = fecha;
    }

    private void setImporte(double importe) {
        if (!Double.isFinite(importe) || importe <= 0) {
            throw new IllegalArgumentException("El importe de devolución debe ser positivo.");
        }
        this.importe = importe;
    }

    private void setPago(Pago pago) {
        if (pago == null) {
            throw new IllegalArgumentException("La devolución debe corresponder a un pago.");
        }
        this.pago = pago;
    }

    @Override
    public String toString() {
        return "Devolución N° " + numero + " (" + fecha + ") - $" + importe + " por " + getMedioPago();
    }
}
