package vista;

import controlador.ControladorOperaciones;
import modelo.Pasajero;
import modelo.Personal;
import modelo.Salida;

import java.util.List;

// Menú del guía: CU20 (consultar la lista de pasajeros de una salida suya).
public class VistaGuia {
    private final Consola consola;
    private final Listados listados;
    private final ControladorOperaciones controlador;
    private final Personal usuario;

    public VistaGuia(Consola consola, ControladorOperaciones controlador, Personal usuario) {
        this.consola = consola;
        this.listados = new Listados(consola);
        this.controlador = controlador;
        this.usuario = usuario;
    }

    public void ejecutar() {
        int opcion;
        do {
            opcion = consola.menu("🚩 Menú del guía",
                    new String[]{"📅 Ver mis salidas asignadas", "📋 Ver la lista de pasajeros de una salida"},
                    "🚪 Cerrar sesión");
            if (opcion == 1) {
                verMisSalidas();
            } else if (opcion == 2) {
                verListaPasajeros();
            }
        } while (opcion != 0);
    }

    private void verMisSalidas() {
        consola.subtitulo("Mis salidas");
        List<Salida> propias = controlador.getSalidasDelGuia(usuario);
        if (propias != null) {
            listados.mostrarSalidas(propias);
        }
    }

    private void verListaPasajeros() {
        List<Salida> propias = controlador.getSalidasDelGuia(usuario);
        if (propias == null) {
            return;
        }
        Salida salida = listados.elegirSalida(propias);
        if (salida == null) {
            return;
        }
        List<Pasajero> pasajeros = controlador.consultarListaPasajeros(usuario, salida.getNumero());
        if (pasajeros != null) {
            listados.mostrarPasajeros(salida, pasajeros);
        }
    }
}