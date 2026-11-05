package modelo;

public class Administrador {

    private int id;
    private String nombre;
    private String email;

    public Administrador(int id, String nombre, String email) {
        setId(id);
        setNombre(nombre);
        setEmail(email);
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El ID debe ser mayor a cero."
            );
        }
        this.id = id;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre no puede estar vacio."
            );
        }
        this.nombre = nombre;
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El email no puede estar vacio."
            );
        }
        this.email = email;
    }
}
