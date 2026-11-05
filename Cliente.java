package modelo;

public class Cliente {
    private String id;
    private String nombre;
    private String apellido;
    private String documento;
    private String telefono;
    private String correoElectronico;
    private String paisResidencia;

    public Cliente(String id, String nombre, String apellido, String documento,
                   String telefono, String correoElectronico, String paisResidencia) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id del cliente es obligatorio");
        }
        if (nombre == null || nombre.isBlank() || apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("Nombre y apellido son obligatorios.");
        }
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio.");
        }
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.documento = documento;
        this.telefono = telefono;
        this.correoElectronico = correoElectronico;
        this.paisResidencia = paisResidencia;
    }
    
    public void actualizarDatos(String telefono, String correoElectronico, String paisResidencia) {
        this.telefono = telefono;
        this.correoElectronico = correoElectronico;
        this.paisResidencia = paisResidencia;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getDocumento() { return documento; }
    public String getTelefono() { return telefono; }
    public String getCorreoElectronico() { return correoElectronico; }
    public String getPaisResidencia() { return paisResidencia; }

    @Override
    public String toString() {
        return apellido + ", " + nombre + " (" + documento + ")";
    }
}
