package modelo;

public class Vendedor extends Personal {
    private double comision;

    public Vendedor(int legajo, String nombre, String documento, String telefono, double comision) {
        super(legajo, nombre, documento, telefono);
        setComision(comision);
    }

    public double getComision() {
        return comision;
    }

    public void setComision(double comision) {
        if (comision < 0 || comision > 100) {
            throw new IllegalArgumentException("La comisión debe estar entre 0 y 100.");
        }
        this.comision = comision;
    }

    public double calcularComision(double importe) {
        if (importe < 0) {
            throw new IllegalArgumentException("El importe no puede ser negativo.");
        }
        return importe * comision / 100;
    }
}
