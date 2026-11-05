package modelo;

public class ServicioContratado {
    private final ServicioAdicional servicio;
    private final double precioAplicado;

    ServicioContratado(ServicioAdicional servicio) {
        if (servicio == null) {
            throw new IllegalArgumentException("El servicio adicional es obligatorio.");
        }
        this.servicio = servicio;
        this.precioAplicado = servicio.getPrecio();
    }

    public double calcularSubtotal() {
        return precioAplicado;
    }

    public ServicioAdicional getServicio() { return servicio; }
    public double getPrecioAplicado() { return precioAplicado; }
}
