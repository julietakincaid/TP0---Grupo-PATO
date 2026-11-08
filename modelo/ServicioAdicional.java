package modelo;

import modelo.enums.TipoServicioAdicional;
import java.io.Serializable;

public class ServicioAdicional implements Serializable{
    private int codigo;
    private String nombre;
    private String descripcion;
    private TipoServicioAdicional tipo;
    private double precio;

    public ServicioAdicional(int codigo, String nombre, String descripcion, TipoServicioAdicional tipo, double precio) {
        setCodigo(codigo);
        setNombre(nombre);
        setDescripcion(descripcion);
        setTipo(tipo);
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
            throw new IllegalArgumentException("El código debe ser positivo.");
        }
        this.codigo = codigo;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria.");
        }
        this.descripcion = descripcion;
    }

    private void setTipo(TipoServicioAdicional tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de servicio es obligatorio.");
        }
        this.tipo = tipo;
    }

    public void setPrecio(double precio) {
        if (!Double.isFinite(precio) || precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero.");
        }
        this.precio = precio;
    }

    public boolean esSeguro() {
        return tipo == TipoServicioAdicional.SEGURO;
    }

    // dos servicios son el mismo si tienen el mismo código
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServicioAdicional)) return false;
        ServicioAdicional otro = (ServicioAdicional) o;
        return codigo == otro.codigo;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(codigo);
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre + " (" + tipo + ") - $" + precio;
    }
}
