package modelo;

import java.io.Serializable;

public class ServicioTour implements Serializable{
    private final ServicioAdicional servicioAdicional;
    private boolean obligatorio;

    ServicioTour(ServicioAdicional servicioAdicional, boolean obligatorio) {
        if (servicioAdicional == null) {
            throw new IllegalArgumentException("El servicio adicional es obligatorio.");
        }
        this.servicioAdicional = servicioAdicional;
        this.obligatorio = obligatorio;
    }

    public ServicioAdicional getServicioAdicional() {
        return servicioAdicional;
    }

    public boolean esObligatorio() {
        return obligatorio;
    }

    void setObligatorio(boolean obligatorio) {
        this.obligatorio = obligatorio;
    }

    @Override
    public String toString() {
        return servicioAdicional.getNombre() + (obligatorio ? " (obligatorio)" : " (opcional)");
    }
}