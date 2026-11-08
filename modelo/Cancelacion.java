package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

import modelo.enums.OrigenCancelacion;

public class Cancelacion implements Serializable {
    private int numero;
    private LocalDate fecha;
    private String motivo;
    private OrigenCancelacion origen;
    private List<Devolucion> devoluciones;

    Cancelacion(int numero, LocalDate fecha, String motivo, OrigenCancelacion origen) {
        this.devoluciones = new ArrayList<>();
        setNumero(numero);
        setFecha(fecha);
        setMotivo(motivo);
        setOrigen(origen);
    }

    public int getNumero() {
        return numero;
    }
    public LocalDate getFecha() {
        return fecha;
    }
    public String getMotivo() {
        return motivo;
    }
    public OrigenCancelacion getOrigen() {
        return origen;
    }
    public List<Devolucion> getDevoluciones() {
        return new ArrayList<>(devoluciones);
    }

    private void setNumero(int numero) {
        if (numero <= 0) {
            throw new IllegalArgumentException("El numero de cancelacion debe ser positivo.");
        }
        this.numero = numero;
    }

    private void setFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de cancelación es obligatoria.");
        }
        this.fecha = fecha;
    }

    private void setMotivo(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de cancelacion es obligatorio.");
        }
        this.motivo = motivo.strip();
    }

    private void setOrigen(OrigenCancelacion origen) {
        if (origen == null) {
            throw new IllegalArgumentException("El origen de cancelacion es obligatorio.");
        }
        this.origen = origen;
    }

    void generarDevolucion(int numero, LocalDate fecha, double importe, Pago pago) {
        if (pago == null) {
            throw new IllegalArgumentException("El pago no puede ser nulo.");
        }

        if (!Double.isFinite(importe) || importe <= 0) {
            throw new IllegalArgumentException("El importe de devolución debe ser positivo.");
        }

        double totalDevuelto = 0;

        for (Devolucion devolucion : devoluciones) {
            if (devolucion.getPago() == pago) {
                totalDevuelto += devolucion.getImporte();
            }
        }

        // no se puede devolver más de lo que se cobró en ese pago (importe más recargo)
        if (totalDevuelto + importe > pago.calcularTotalPago() + 0.005) {
            throw new IllegalArgumentException("La devolución supera lo que se cobró en el pago.");
        }

        Devolucion devolucion = new Devolucion(numero, fecha, importe, pago);
        devoluciones.add(devolucion);
    }

    public double calcularTotalDevuelto() {
        double total = 0;
        for (Devolucion devolucion : devoluciones) {
            total += devolucion.getImporte();
        }
        return Math.round(total * 100.0) / 100.0;
    }

    @Override
    public String toString() {
        return "Cancelación N° " + numero + " (" + fecha + ") - " + origen + " - " + motivo;
    }
}
