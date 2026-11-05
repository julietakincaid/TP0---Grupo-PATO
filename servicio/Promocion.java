package servicio;

public abstract class Promocion {
    private String nombre;
    private double porcentajeDescuento;

    public Promocion(String nombre, double porcentajeDescuento) {
        this.nombre = nombre;
        setPorcentajeDescuento(porcentajeDescuento);
    }

    public String getNombre() {
        return nombre;
    }

    public double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPorcentajeDescuento(double porcentajeDescuento) {
        if (porcentajeDescuento < 0 || porcentajeDescuento > 100) {
            throw new IllegalArgumentException(
                    "El porcentaje debe estar entre 0 y 100."
            );
        }
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public abstract boolean esAplicable(int cantidadPasajeros, int diasAnticipacion);
}
