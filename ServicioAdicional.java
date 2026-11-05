package modelo;

import modelo.enums.TipoServicioAdicional;

public class ServicioAdicional {
    private String nombre;
    private double precio;
    private TipoServicioAdicional tipo;

    public ServicioAdicional(String nombre, double precio, TipoServicioAdicional tipo) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del servicio es obligatorio.");
        }
        if (precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de servicio es obligatorio.");
        }
        this.nombre = nombre;
        this.precio = precio;
        this.tipo = tipo;
    }

    public double getPrecio() { return precio; }
    public String getNombre() { return nombre; }
    public TipoServicioAdicional getTipo() { return tipo; }


    public void setPrecio(double precio) {
        if (precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        this.precio = precio;
    }

    @Override
    public String toString() {
        return nombre + " (" + tipo + ") $" + precio;
    }
}
