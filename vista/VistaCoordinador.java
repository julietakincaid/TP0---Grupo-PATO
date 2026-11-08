package vista;

import controlador.ControladorOperaciones;
import modelo.Guia;
import modelo.Pasajero;
import modelo.Personal;
import modelo.Salida;
import modelo.Tour;
import modelo.Vehiculo;

import java.time.LocalDate;
import java.util.List;

// Menú del coordinador de operaciones: CU10 (programar salida), CU11 (asignar guía y vehículo), CU12 (emitir la lista e iniciar la salida) y CU13 (finalizar la salida).
public class VistaCoordinador {
    private final Consola consola;
    private final Listados listados;
    private final ControladorOperaciones controlador;
    private final Personal usuario;
    private final LocalDate hoy;
    private final Runnable guardar;

    public VistaCoordinador(Consola consola, ControladorOperaciones controlador, Personal usuario, LocalDate hoy, Runnable guardar) {
        this.consola = consola;
        this.listados = new Listados(consola);
        this.controlador = controlador;
        this.usuario = usuario;
        this.hoy = hoy;
        this.guardar = guardar;
    }

    public void ejecutar() {
        int opcion;
        do {
            opcion = consola.menu("🧭 Menú del coordinador de operaciones", new String[]{
                    "📅 Ver las salidas",
                    "🆕 Programar una salida",
                    "👤 Asignar un guía a una salida",
                    "🚌 Asignar un vehículo a una salida",
                    "📋 Emitir la lista de pasajeros de una salida",
                    "🟢 Iniciar una salida",
                    "🏁 Finalizar una salida"}, "🚪 Cerrar sesión");
            if (opcion == 1) {
                verSalidas();
            } else if (opcion == 2) {
                programarSalida();
            } else if (opcion == 3) {
                asignarGuia();
            } else if (opcion == 4) {
                asignarVehiculo();
            } else if (opcion == 5) {
                emitirLista();
            } else if (opcion == 6) {
                iniciarSalida();
            } else if (opcion == 7) {
                finalizarSalida();
            }
        } while (opcion != 0);
    }

    private void verSalidas() {
        consola.subtitulo("Salidas");
        listados.mostrarSalidas(controlador.getSalidas());
    }

    // CU10
    private void programarSalida() {
        List<Tour> tours = controlador.getToursActivos();
        if (tours == null) {
            return;
        }
        consola.subtitulo("Programar una salida");
        Tour tour = tours.get(consola.elegirPosicion("Tour", tours));
        consola.info("El tour dura " + tour.getDuracionDias() + " días y admite hasta "
                + tour.getCupoMaximo() + " pasajeros.");
        LocalDate inicio = consola.leerFecha("Fecha de inicio");
        LocalDate fin = consola.leerFecha("Fecha de fin");
        int cupoMinimo = consola.leerEntero("Cupo mínimo");
        int cupoMaximo = consola.leerEntero("Cupo máximo");
        if (controlador.programarSalida(usuario, tour.getCodigo(), inicio, fin, cupoMinimo, cupoMaximo)) {
            guardar.run();
        }
    }

    // CU11
    private void asignarGuia() {
        consola.subtitulo("Asignar un guía");
        List<Guia> guias = controlador.getGuias();
        if (guias == null) {
            return;
        }
        Salida salida = listados.elegirSalida(controlador.getSalidas());
        if (salida == null) {
            return;
        }
        Guia guia = guias.get(consola.elegirPosicion("Guía", guias));
        if (controlador.asignarGuia(usuario, salida.getNumero(), guia.getLegajo())) {
            guardar.run();
        }
    }

    // CU11
    private void asignarVehiculo() {
        consola.subtitulo("Asignar un vehículo");
        List<Vehiculo> vehiculos = controlador.getVehiculos();
        if (vehiculos == null) {
            return;
        }
        Salida salida = listados.elegirSalida(controlador.getSalidas());
        if (salida == null) {
            return;
        }
        Vehiculo vehiculo = vehiculos.get(consola.elegirPosicion("Vehículo", vehiculos));
        if (controlador.asignarVehiculo(usuario, salida.getNumero(), vehiculo.getPatente())) {
            guardar.run();
        }
    }

    // CU12
    private void emitirLista() {
        consola.subtitulo("Emitir la lista de pasajeros");
        Salida salida = listados.elegirSalida(controlador.getSalidas());
        if (salida == null) {
            return;
        }
        List<Pasajero> pasajeros = controlador.emitirListaPasajeros(usuario, salida.getNumero());
        if (pasajeros != null) {
            listados.mostrarPasajeros(salida, pasajeros);
            guardar.run();
        }
    }

    // CU12
    private void iniciarSalida() {
        consola.subtitulo("Iniciar una salida");
        Salida salida = listados.elegirSalida(controlador.getSalidas());
        if (salida == null) {
            return;
        }
        if (!consola.confirmar("¿Iniciar la salida N° " + salida.getNumero() + "?")) {
            consola.info("Operación cancelada.");
            return;
        }
        if (controlador.iniciarSalida(usuario, salida.getNumero(), hoy)) {
            guardar.run();
        }
    }

    // CU13
    private void finalizarSalida() {
        consola.subtitulo("Finalizar una salida");
        Salida salida = listados.elegirSalida(controlador.getSalidas());
        if (salida == null) {
            return;
        }
        if (!consola.confirmar("¿Finalizar la salida N° " + salida.getNumero() + "?")) {
            consola.info("Operación cancelada.");
            return;
        }
        if (controlador.finalizarSalida(usuario, salida.getNumero())) {
            guardar.run();
        }
    }
}
