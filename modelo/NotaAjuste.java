package modelo;

import java.time.LocalDate;
import java.io.Serializable;

public class NotaAjuste implements Serializable {
    private int numero;
    private LocalDate fecha;
    private String motivo;
    private double importe;

    NotaAjuste(int numero, LocalDate fecha, String motivo, double importe) {
        setNumero(numero);
        setFecha(fecha);
        setMotivo(motivo);
        setImporte(importe);
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
    public double getImporte() {
        return importe;
    }

    private void setNumero(int numero) {
        if (numero <= 0) {
            throw new IllegalArgumentException("El numero de ajuste debe ser positivo.");
        }
        this.numero = numero;
    }

    private void setFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de ajuste es obligatoria.");
        }
        this.fecha = fecha;
    }

    private void setMotivo(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo del ajuste es obligatorio.");
        }
        this.motivo = motivo.strip();
    }

    // puede ser positivo (suma al total) o negativo (resta), pero no cero
    private void setImporte(double importe) {
        if (!Double.isFinite(importe) || importe == 0) {
            throw new IllegalArgumentException("El importe del ajuste debe ser distinto de cero.");
        }
        this.importe = importe;
    }

    @Override
    public String toString() {
        return "Ajuste N° " + numero + " (" + fecha + ") - $" + importe + " - " + motivo;
    }
}
