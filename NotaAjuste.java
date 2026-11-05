package modelo;

import java.time.LocalDate;

public class NotaAjuste {
    private final int numero;
    private final LocalDate fecha;
    private final String motivo;
    private final double importeAjuste;
    private boolean registrada;

    public NotaAjuste(int numero, LocalDate fecha, String motivo, double importeAjuste) {
        if (numero <= 0) {
            throw new IllegalArgumentException("El número de nota debe ser positivo.");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo es obligatorio.");
        }
        if (importeAjuste == 0) {
            throw new IllegalArgumentException("El importe del ajuste no puede ser cero.");
        }
        this.numero = numero;
        this.fecha = fecha;
        this.motivo = motivo;
        this.importeAjuste = importeAjuste;
        this.registrada = false;
    }


    public void registrarAjuste() {
        if (registrada) {
            throw new IllegalStateException("La nota de ajuste " + numero + " ya fue registrada.");
        }
        registrada = true;
    }

    public int getNumero() { return numero; }
    public LocalDate getFecha() { return fecha; }
    public String getMotivo() { return motivo; }
    public double getImporteAjuste() { return importeAjuste; }
    public boolean isRegistrada() { return registrada; }
}
