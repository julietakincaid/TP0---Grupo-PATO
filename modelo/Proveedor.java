package modelo;

import java.util.Objects;
import java.io.Serializable;

public class Proveedor implements Serializable{
    private String nombre;
    private String cuit;
    private String contacto;
    private String telefono;
    private String servicioPrestado;

    public Proveedor(String nombre, String cuit, String contacto, String telefono, String servicioPrestado) {
        setNombre(nombre);
        setCuit(cuit);
        setContacto(contacto);
        setTelefono(telefono);
        setServicioPrestado(servicioPrestado);
    }

    public String getNombre() {
        return nombre;
    }
    public String getCuit() {
        return cuit;
    }
    public String getContacto() {
        return contacto;
    }
    public String getTelefono() {
        return telefono;
    }
    public String getServicioPrestado() { return servicioPrestado; }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        this.nombre = nombre;
    }

    private void setCuit(String cuit) {
        if (cuit == null || cuit.isBlank()) {
            throw new IllegalArgumentException("El CUIT no puede estar vacío.");
        }

        // saco guiones y espacios
        String soloDigitos = cuit.replace("-", "").replace(" ", "");

        if (soloDigitos.length() != 11) {
            throw new IllegalArgumentException("El CUIT debe tener 11 dígitos.");
        }

        // reviso que cada caracter sea un número
        for (int i = 0; i < soloDigitos.length(); i++) {
            if (!Character.isDigit(soloDigitos.charAt(i))) {
                throw new IllegalArgumentException("El CUIT solo puede contener números.");
            }
        }
        this.cuit = soloDigitos;
    }

    public void setContacto(String contacto) {
        if (contacto == null || contacto.isBlank()) {
            throw new IllegalArgumentException("El contacto no puede estar vacío.");
        }
        this.contacto = contacto;
    }

    public void setTelefono(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("El teléfono no puede estar vacío.");
        }

        String limpio = telefono.strip();
        int cantidadDigitos = 0;

        for (int i = 0; i < limpio.length(); i++) {
            char c = limpio.charAt(i);
            if (Character.isDigit(c)) {
                cantidadDigitos++;
            } else if (c != '+' && c != '-' && c != '(' && c != ')' && c != ' ') {
                throw new IllegalArgumentException("El teléfono contiene caracteres inválidos.");
            }
        }
        if (cantidadDigitos < 6) {
            throw new IllegalArgumentException("El teléfono debe tener al menos 6 dígitos.");
        }
        this.telefono = limpio;
    }

    public void setServicioPrestado(String servicioPrestado) {
        if (servicioPrestado == null || servicioPrestado.isBlank()) {
            throw new IllegalArgumentException("El servicio prestado no puede estar vacío.");
        }
        this.servicioPrestado = servicioPrestado.strip();
    }

    // dos proveedores son el mismo si tienen el mismo CUIT
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Proveedor)) return false;
        Proveedor otro = (Proveedor) o;
        return cuit.equals(otro.cuit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cuit);
    }

    @Override
    public String toString() {
        return nombre + " (CUIT " + cuit.substring(0, 2) + "-" + cuit.substring(2, 10)
                + "-" + cuit.substring(10) + ") - " + servicioPrestado;
    }
}
