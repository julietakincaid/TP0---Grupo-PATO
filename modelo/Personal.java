package modelo;

public abstract class Personal {
    private int legajo;
    private String nombre;
    private String documento;
    private String telefono;

    public Personal(int legajo, String nombre, String documento, String telefono) {
        setLegajo(legajo);
        this.nombre = nombre;
        this.documento = documento;
        this.telefono = telefono;
    }

    /** Cada tipo de personal informa su rol (método sobreescrito en las subclases). */
    public abstract String getRol();

    /** El legajo es la identidad del empleado y no se modifica. */
    public void actualizarDatos(String nombre, String documento, String telefono) {
        this.nombre = nombre;
        this.documento = documento;
        this.telefono = telefono;
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
        this.nombre = nombre;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    @Override
    public String toString() {
        return getRol() + " #" + legajo + " - " + nombre;
    }
}
