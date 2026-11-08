package prueba;

import controlador.Pantalla;

import java.util.ArrayList;
import java.util.List;

// Una pantalla falsa para las pruebas de los controladores: en lugar de mostrar los mensajes, los anota,
// así las pruebas pueden comprobar qué le ordenó mostrar el controlador.
class PantallaFalsa implements Pantalla {
    final List<String> mensajes = new ArrayList<>();
    String ultimoTipo = "";
    String ultimoTexto = "";

    @Override
    public void mostrarMensaje(String texto) {
        mensajes.add("MENSAJE: " + texto);
        ultimoTipo = "MENSAJE";
        ultimoTexto = texto;
    }

    @Override
    public void mostrarError(String texto) {
        mensajes.add("ERROR: " + texto);
        ultimoTipo = "ERROR";
        ultimoTexto = texto;
    }

    @Override
    public void mostrarAviso(String texto) {
        mensajes.add("AVISO: " + texto);
        ultimoTipo = "AVISO";
        ultimoTexto = texto;
    }
}