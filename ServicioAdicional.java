package modelo;

import modelo.enums.TipoServicioAdicional;

public class ServicioAdicional {
    private int codigo;
    private String nombre;
    private String descripcion;
    private TipoServicioAdicional tipo;
    private double precio;

    public ServicioAdicional(int codigo, String nombre, String descripcion, TipoServicioAdicional tipo, double precio) {
        setCodigo(codigo);
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        setPrecio(precio);
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public TipoServicioAdicional getTipo() {
        return tipo;
    }

    public double getPrecio() {
        return precio;
    }

    private void setCodigo(int codigo) {
        if (codigo <= 0) {
            throw new IllegalArgumentException("El codigo debe ser positivo.");
        }
        this.codigo = codigo;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setTipo(TipoServicioAdicional tipo) {
        this.tipo = tipo;
    }

    public void setPrecio(double precio) {
        if (precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        this.precio = precio;
    }
}
