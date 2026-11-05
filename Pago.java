package modelo;

import java.time.LocalDate;
import modelo.enums.MedioPago;

public class Pago {
    private static final double RECARGO_TARJETA_CUOTAS = 0.07;

    private final int numero;
    private final LocalDate fecha;
    private final double importe;
    private final MedioPago medioPago;
    private final int cantidadCuotas;

    Pago(int numero, LocalDate fecha, double importe, MedioPago medioPago, int cantidadCuotas) {
        if (numero <= 0) {
            throw new IllegalArgumentException("El número de pago debe ser positivo.");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha del pago es obligatoria.");
        }
        if (importe <= 0) {
            throw new IllegalArgumentException("El importe debe ser mayor a cero.");
        }
        if (medioPago == null) {
            throw new IllegalArgumentException("El medio de pago es obligatorio.");
        }
        if (cantidadCuotas < 1) {
            throw new IllegalArgumentException("La cantidad de cuotas debe ser al menos 1.");
        }
        if (cantidadCuotas > 1 && medioPago != MedioPago.TARJETA) {
            throw new IllegalArgumentException("Solo se puede pagar en cuotas con tarjeta.");
        }
        this.numero = numero;
        this.fecha = fecha;
        this.importe = importe;
        this.medioPago = medioPago;
        this.cantidadCuotas = cantidadCuotas;
    }


    public boolean aplicaRecargo() {
        return medioPago == MedioPago.TARJETA && cantidadCuotas > 1;
    }

    public double calcularRecargo() {
        return aplicaRecargo() ? importe * RECARGO_TARJETA_CUOTAS : 0;
    }


    public double calcularImporteFinal() {
        return importe + calcularRecargo();
    }

    public int getNumero() { return numero; }
    public LocalDate getFecha() { return fecha; }
    public double getImporte() { return importe; }
    public MedioPago getMedioPago() { return medioPago; }
    public int getCantidadCuotas() { return cantidadCuotas; }
}
