package servicio;

import excepcion.AsignacionInvalidaException;
import modelo.Guia;
import modelo.Salida;
import modelo.Vehiculo;
import modelo.enums.EstadoSalida;

import java.util.List;

public class ServicioAsignacion {
    public void asignarGuia(Salida salida, Guia guia, List<Salida> salidas) throws AsignacionInvalidaException {
        if (salida == null || guia == null || salidas == null) {
            throw new IllegalArgumentException("Los parámetros no pueden ser nulos.");
        }
        for (Salida otra : salidas) {
            if (otra != null
                    && otra != salida
                    && guia.equals(otra.getGuia())
                    && otra.getEstado() != EstadoSalida.CANCELADA
                    && salida.seSuperponeCon(otra)) {
                throw new AsignacionInvalidaException("El guía ya está asignado a otra salida en esas fechas.");
            }
        }
        salida.asignarGuia(guia);   // valida habilitación y estado de la salida
    }

    public void asignarVehiculo(Salida salida, Vehiculo vehiculo, List<Salida> salidas) throws AsignacionInvalidaException {
        if (salida == null || vehiculo == null || salidas == null) {
            throw new IllegalArgumentException("Los parámetros no pueden ser nulos.");
        }
        for (Salida otra : salidas) {
            if (otra != null
                    && otra != salida
                    && vehiculo.equals(otra.getVehiculo())
                    && otra.getEstado() != EstadoSalida.CANCELADA
                    && salida.seSuperponeCon(otra)) {
                throw new AsignacionInvalidaException("El vehículo ya está asignado a otra salida en esas fechas.");
            }
        }
        salida.asignarVehiculo(vehiculo);   // valida capacidad y estado de la salida
    }
}
