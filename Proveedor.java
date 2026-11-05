package modelo;

public class Proveedor {
    private String nombre;
    private String cuit;
    private String contacto;
    private String telefono;

    public Proveedor(String nombre, String cuit, String contacto, String telefono) {
        setNombre(nombre);
        setCuit(cuit);
        setContacto(contacto);
        setTelefono(telefono);
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

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        this.nombre = nombre;
    }

    public void setCuit(String cuit) {
        if (cuit == null || cuit.isBlank()) {
            throw new IllegalArgumentException("El CUIT no puede estar vacío.");
        }
        this.cuit = cuit;
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
        this.telefono = telefono;
    }
}
