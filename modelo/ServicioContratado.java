package modelo;

import java.io.Serializable;

public class ServicioContratado implements Serializable {
    private final ServicioAdicional servicioAdicional;
    private final double precioAplicado;

    ServicioContratado(ServicioAdicional servicioAdicional) {
        if (servicioAdicional == null) {
            throw new IllegalArgumentException("El servicio adicional no puede ser nulo.");
        }
        this.servicioAdicional = servicioAdicional;
        this.precioAplicado = servicioAdicional.getPrecio();
    }

    public ServicioAdicional getServicioAdicional() {
        return servicioAdicional;
    }
    public double getPrecioAplicado() {
        return precioAplicado;
    }

    public double calcularSubtotal() {
        return precioAplicado;
    }

    @Override
    public String toString() {
        return servicioAdicional.getNombre() + " - $" + precioAplicado;
    }
}
