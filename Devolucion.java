package modelo;

import java.time.LocalDate;

import modelo.enums.MedioPago;

public class Devolucion {
    private final LocalDate fecha;
    private final double importe;
    private final MedioPago medioPago;
    private boolean registrada;

    Devolucion(LocalDate fecha, double importe, MedioPago medioPago) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de la devolución es obligatoria.");
        }
        if (importe <= 0) {
            throw new IllegalArgumentException("El importe a devolver debe ser mayor a cero.");
        }
        if (medioPago == null) {
            throw new IllegalArgumentException("El medio de pago es obligatorio.");
        }
        this.fecha = fecha;
        this.importe = importe;
        this.medioPago = medioPago;
        this.registrada = false;
    }


    public void registrarDevolucion() {
        if (registrada) {
            throw new IllegalStateException("La devolución ya fue registrada.");
        }
        this.registrada = true;
    }

    public LocalDate getFecha() { return fecha; }
    public double getImporte() { return importe; }
    public MedioPago getMedioPago() { return medioPago; }
    public boolean isRegistrada() { return registrada; }
}
