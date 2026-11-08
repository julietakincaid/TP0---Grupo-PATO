package controlador;

import excepcion.ReglaNegocioException;
import modelo.CoordinadorOperaciones;
import modelo.Guia;
import modelo.Pasajero;
import modelo.Personal;
import modelo.Reserva;
import modelo.Salida;
import modelo.Tour;
import modelo.Vehiculo;
import modelo.enums.EstadoSalida;
import servicio.Agencia;
import servicio.ServicioAsignacion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Casos de uso del coordinador: CU10 (programar salida), CU11 (asignar guía y vehículo),CU12 (iniciar salida) y CU13 (finalizar salida). El guía consulta su lista de pasajeros (CU20).
// También controla los plazos (RF18 y RN07), que el programa corre al arrancar.
public class ControladorOperaciones {
    private final Agencia agencia;
    private final ServicioAsignacion servicioAsignacion;
    private final Pantalla pantalla;

    public ControladorOperaciones(Agencia agencia, ServicioAsignacion servicioAsignacion, Pantalla pantalla) {
        if (agencia == null || servicioAsignacion == null || pantalla == null) {
            throw new IllegalArgumentException("La agencia, el servicio de asignación y la pantalla son obligatorios.");
        }
        this.agencia = agencia;
        this.servicioAsignacion = servicioAsignacion;
        this.pantalla = pantalla;
    }

    public List<Salida> getSalidas() {
        return agencia.getSalidas();
    }

    // los tours que se pueden usar para programar una salida (los dados de baja no se ofrecen);
    // si no hay ninguno, muestra el error y devuelve null
    public List<Tour> getToursActivos() {
        List<Tour> activos = new ArrayList<>();
        for (Tour tour : agencia.getTours()) {
            if (tour.estaActivo()) {
                activos.add(tour);
            }
        }
        if (activos.isEmpty()) {
            pantalla.mostrarError("No hay tours activos. El administrador tiene que cargar uno.");
            return null;
        }
        return activos;
    }

    // los guías que se pueden asignar; si no hay ninguno, muestra el error y devuelve null
    public List<Guia> getGuias() {
        List<Guia> guias = agencia.getGuias();
        if (guias.isEmpty()) {
            pantalla.mostrarError("No hay guías cargados.");
            return null;
        }
        return guias;
    }

    // los vehículos que se pueden asignar; si no hay ninguno, muestra el error y devuelve null
    public List<Vehiculo> getVehiculos() {
        List<Vehiculo> vehiculos = agencia.getVehiculos();
        if (vehiculos.isEmpty()) {
            pantalla.mostrarError("No hay vehículos cargados.");
            return null;
        }
        return vehiculos;
    }

    // ---- CU10: programar salida ----
    public boolean programarSalida(Personal usuario, String codigoTour, LocalDate fechaInicio, LocalDate fechaFin, int cupoMinimo, int cupoMaximo) {
        try {
            validarCoordinador(usuario);
            Tour tour = agencia.buscarTour(codigoTour);
            Salida salida = agencia.programarSalida(fechaInicio, fechaFin, cupoMinimo, cupoMaximo, tour);
            pantalla.mostrarMensaje("Se programó la salida N° " + salida.getNumero() + ".");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    // CU11: asignar guía y vehículo
    public boolean asignarGuia(Personal usuario, int numeroSalida, int legajoGuia) {
        try {
            validarCoordinador(usuario);
            Salida salida = agencia.buscarSalida(numeroSalida);
            Personal persona = agencia.buscarPersonal(legajoGuia);
            if (!(persona instanceof Guia)) {
                throw new ReglaNegocioException("El legajo " + legajoGuia + " no corresponde a un guía.");
            }
            servicioAsignacion.asignarGuia(salida, (Guia) persona, agencia.getSalidas());
            pantalla.mostrarMensaje("Se asignó a " + persona.getNombre() + " a la salida N° " + numeroSalida + ".");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean asignarVehiculo(Personal usuario, int numeroSalida, String patente) {
        try {
            validarCoordinador(usuario);
            Salida salida = agencia.buscarSalida(numeroSalida);
            Vehiculo vehiculo = agencia.buscarVehiculo(patente);
            servicioAsignacion.asignarVehiculo(salida, vehiculo, agencia.getSalidas());
            pantalla.mostrarMensaje("Se asignó el vehículo " + vehiculo.getPatente() + " a la salida N° " + numeroSalida + ".");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    // CU12: lista de pasajeros e inicio de la salida
    public List<Pasajero> emitirListaPasajeros(Personal usuario, int numeroSalida) {
        try {
            validarCoordinador(usuario);
            List<Pasajero> pasajeros = agencia.buscarSalida(numeroSalida).emitirListaPasajeros();
            pantalla.mostrarMensaje("La lista quedó emitida.");
            return pasajeros;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return null;
        }
    }

    // antes de iniciar se controlan los plazos, y si la lista no estaba emitida se emite sola
    public boolean iniciarSalida(Personal usuario, int numeroSalida, LocalDate hoy) {
        try {
            validarCoordinador(usuario);
            Salida salida = agencia.buscarSalida(numeroSalida);
            agencia.verificarPlazosPago(hoy);
            agencia.verificarCuposMinimos(hoy);
            if (!salida.tieneListaEmitida()) {
                salida.emitirListaPasajeros();
            }
            salida.iniciarSalida(hoy);
            pantalla.mostrarMensaje("La salida N° " + numeroSalida + " está en curso.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    // CU13: finalizar salida (finaliza también sus reservas confirmadas)
    public boolean finalizarSalida(Personal usuario, int numeroSalida) {
        try {
            validarCoordinador(usuario);
            agencia.buscarSalida(numeroSalida).finalizarSalida();
            pantalla.mostrarMensaje("La salida N° " + numeroSalida + " finalizó. Ya se puede liquidar al guía.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    // CU20: el guía consulta la lista de pasajeros de su salida
    // las salidas (no canceladas) que tiene asignadas el guía, o null si el usuario no es un guía
    public List<Salida> getSalidasDelGuia(Personal usuario) {
        if (!(usuario instanceof Guia)) {
            pantalla.mostrarError("Solo un guía tiene salidas asignadas.");
            return null;
        }
        List<Salida> propias = new ArrayList<>();
        for (Salida salida : agencia.getSalidas()) {
            if (usuario.equals(salida.getGuia()) && salida.getEstado() != EstadoSalida.CANCELADA) {
                propias.add(salida);
            }
        }
        return propias;
    }

    // devuelve los pasajeros de la lista emitida, o null si el guía no puede verla
    public List<Pasajero> consultarListaPasajeros(Personal usuario, int numeroSalida) {
        try {
            if (!(usuario instanceof Guia)) {
                throw new ReglaNegocioException("Solo un guía puede consultar la lista de pasajeros de su salida.");
            }
            Salida salida = agencia.buscarSalida(numeroSalida);
            if (!usuario.equals(salida.getGuia())) {
                throw new ReglaNegocioException("El guía no está asignado a esa salida.");
            }
            return salida.consultarListaPasajeros();
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return null;
        }
    }

    // control de plazos: el programa lo corre al arrancar y al cambiar la fecha
    public boolean controlarPlazos(LocalDate hoy) {
        try {
            boolean huboCambios = false;
            for (Reserva reserva : agencia.verificarPlazosPago(hoy)) {
                pantalla.mostrarAviso("Se anuló la reserva N° " + reserva.getNumero() + " por falta de pago del saldo.");
                huboCambios = true;
            }
            for (Salida salida : agencia.verificarCuposMinimos(hoy)) {
                pantalla.mostrarAviso("Se canceló la salida N° " + salida.getNumero() + " por no alcanzar el cupo mínimo.");
                huboCambios = true;
            }
            return huboCambios;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError("No se pudieron controlar los plazos: " + e.getMessage());
            return false;
        }
    }

    private void validarCoordinador(Personal usuario) throws ReglaNegocioException {
        if (!(usuario instanceof CoordinadorOperaciones)) {
            throw new ReglaNegocioException("Solo el coordinador de operaciones puede realizar esta operación.");
        }
    }
}