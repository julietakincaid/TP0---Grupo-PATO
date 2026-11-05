
package modelo;

public abstract class Promocion {

    private String nombre;
    private double porcentajeDescuento;

    public Promocion(String nombre, double porcentajeDescuento) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                "El nombre de la promocion es obligatorio."
            );
        }
        if (porcentajeDescuento < 0 || porcentajeDescuento > 1) {
            throw new IllegalArgumentException(
                "El descuento debe estar entre 0 y 1."
            );
        }

        this.nombre = nombre;
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public abstract boolean esAplicable(Reserva reserva);

    public double calcularDescuento(double importe) {
        if (importe < 0) {
            throw new IllegalArgumentException(
                "El importe no puede ser negativo."
            );
        }
        return importe * porcentajeDescuento;
    }
}
