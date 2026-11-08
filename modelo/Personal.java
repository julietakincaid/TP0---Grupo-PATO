package modelo;

import java.io.Serializable;

public abstract class Personal implements Serializable{
    private int legajo;
    private String nombre;
    private String documento;
    private String telefono;

    public Personal(int legajo, String nombre, String documento, String telefono) {
        setLegajo(legajo);
        setNombre(nombre);
        setDocumento(documento);
        setTelefono(telefono);
    }

    public int getLegajo() {
        return legajo;
    }
    public String getNombre() {
        return nombre;
    }
    public String getDocumento() {
        return documento;
    }
    public String getTelefono() {
        return telefono;
    }

    private void setLegajo(int legajo) {
        if (legajo <= 0) {
            throw new IllegalArgumentException("El legajo debe ser mayor a cero.");
        }
        this.legajo = legajo;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        this.nombre = nombre.strip();
    }

    public void setDocumento(String documento) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento no puede estar vacío.");
        }

        // saco puntos, guiones y espacios, y paso a mayúsculas
        String limpio = documento.replace(".", "").replace("-", "").replace(" ", "").toUpperCase();

        if (limpio.length() < 6 || limpio.length() > 15) {
            throw new IllegalArgumentException("El documento debe tener entre 6 y 15 caracteres.");
        }

        // reviso que cada caracter sea una letra o un número
        for (int i = 0; i < limpio.length(); i++) {
            if (!Character.isLetterOrDigit(limpio.charAt(i))) {
                throw new IllegalArgumentException("El documento solo puede contener letras y números.");
            }
        }
        this.documento = limpio;
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

    // dos empleados son el mismo si tienen el mismo legajo
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Personal)) {
            return false;
        }
        Personal otro = (Personal) o;
        return legajo == otro.legajo;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(legajo);
    }

    @Override
    public String toString() {
        return legajo + " - " + nombre;
    }
}
