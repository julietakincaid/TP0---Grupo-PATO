package modelo;

public class Vendedor extends Personal{
    private double comision;

    public Vendedor(int legajo, String nombre, String documento, String telefono, double comision) {
        super(legajo, nombre, documento, telefono);
        setComision(comision);
    }

    public double calcularComision(double ventas) {
        if (ventas < 0) {
            throw new IllegalArgumentException("Las ventas no pueden ser negativas.");
        }
        return ventas * comision;
    }

    public double getComision() { return comision; }

    public void setComision(double comision) {
        if (comision < 0 || comision > 1) {
            throw new IllegalArgumentException("La comisión debe estar entre 0 y 1 (ej: 0.05 = 5%).");
        }
        this.comision = comision;
    }
}
