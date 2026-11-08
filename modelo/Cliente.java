package modelo;

import java.io.Serializable;

public class Cliente implements Serializable{
    private String nombre;
    private String apellido;
    private String documento;
    private String telefono;
    private String correoElectronico;
    private String paisResidencia;

    public Cliente(String nombre, String apellido, String documento, String telefono, String correoElectronico, String paisResidencia) {
        setDocumento(documento);
        actualizarDatos(nombre, apellido, telefono, correoElectronico, paisResidencia);
    }

    public String getNombre() {
        return nombre;
    }
    public String getApellido() {
        return apellido;
    }
    public String getDocumento() {
        return documento;
    }
    public String getTelefono() {
        return telefono;
    }
    public String getCorreoElectronico() {
        return correoElectronico;
    }
    public String getPaisResidencia() {
        return paisResidencia;
    }

    private void setDocumento(String documento) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio.");
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

    // valida los cinco datos primero y recién después los asigna:
    // si uno falla, el cliente queda como estaba
    public void actualizarDatos(String nombre, String apellido, String telefono,
                                String correoElectronico, String paisResidencia) {
        String nombreValido = validarTexto(nombre, "El nombre es obligatorio.");
        String apellidoValido = validarTexto(apellido, "El apellido es obligatorio.");
        String telefonoValido = validarTelefono(telefono);
        String correoValido = validarCorreo(correoElectronico);
        String paisValido = validarTexto(paisResidencia, "El país de residencia es obligatorio.");

        this.nombre = nombreValido;
        this.apellido = apellidoValido;
        this.telefono = telefonoValido;
        this.correoElectronico = correoValido;
        this.paisResidencia = paisValido;
    }

    private String validarTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor.strip();
    }

    private String validarTelefono(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("El teléfono es obligatorio.");
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
        return limpio;
    }

    private String validarCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio.");
        }

        String limpio = correo.strip();

        if (limpio.contains(" ") || !limpio.contains("@") || !limpio.contains(".")
                || limpio.indexOf('@') > limpio.lastIndexOf('.')) {
            throw new IllegalArgumentException("El correo electrónico no es válido.");
        }

        return limpio.toLowerCase();
    }

    // dos clientes son el mismo si tienen el mismo documento
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cliente)) {
            return false;
        }
        Cliente otro = (Cliente) o;
        return documento.equals(otro.documento);
    }

    @Override
    public int hashCode() {
        return documento.hashCode();
    }

    @Override
    public String toString() {
        return apellido + ", " + nombre + " (" + documento + ")";
    }
}
