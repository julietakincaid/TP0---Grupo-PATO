package controlador;

// Lo que un controlador necesita de la vista: poder ordenarle que muestre un mensaje.
public interface Pantalla {
    void mostrarMensaje(String mensaje);
    void mostrarError(String mensaje);
    void mostrarAviso(String mensaje);
}