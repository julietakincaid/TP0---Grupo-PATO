package modelo;

public class ServicioContratado {
    private ServicioAdicional servicioAdicional;
    private double precioAplicado;

    public ServicioContratado(ServicioAdicional servicioAdicional, double precioAplicado) {
        this.servicioAdicional = servicioAdicional;
        setPrecioAplicado(precioAplicado);
    }

    public ServicioAdicional getServicioAdicional() {
        return servicioAdicional;
    }

    public double getPrecioAplicado() {
        return precioAplicado;
    }

    public void setPrecioAplicado(double precioAplicado) {
        if (precioAplicado < 0) {
            throw new IllegalArgumentException("El precio aplicado no puede ser negativo.");
        }
        this.precioAplicado = precioAplicado;
    }

    public double calcularSubtotal() {
        return precioAplicado;
    }
}
