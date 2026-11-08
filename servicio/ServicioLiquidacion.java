package servicio;

import excepcion.EstadoInvalidoException;
import excepcion.ReglaNegocioException;
import modelo.LiquidacionGuia;
import modelo.Pasajero;
import modelo.Reserva;
import modelo.Salida;
import modelo.enums.EstadoReserva;
import modelo.enums.EstadoSalida;

import java.time.LocalDate;

public class ServicioLiquidacion {
    private static final double PORCENTAJE_COMISION_ADICIONALES = 0.05;

    // calcula los montos y le pide a la salida que cree la liquidación
    public LiquidacionGuia generarLiquidacion(Salida salida, int numero, LocalDate fecha) throws ReglaNegocioException {
        if (salida == null) {
            throw new IllegalArgumentException("La salida no puede ser nula.");
        }
        if (salida.getEstado() != EstadoSalida.FINALIZADA) {
            throw new EstadoInvalidoException("La salida debe estar finalizada.");
        }

        double honorarios = salida.getGuia().calcularHonorarios(salida.getTour().getDuracionDias());
        double comisionAdicionales = calcularTotalAdicionales(salida) * PORCENTAJE_COMISION_ADICIONALES;

        return salida.crearLiquidacion(numero, fecha, honorarios, comisionAdicionales);
    }

    // solo cuentan los adicionales de reservas finalizadas (no de canceladas ni anuladas)
    public double calcularTotalAdicionales(Salida salida) {
        double total = 0;
        for (Reserva reserva : salida.getReservas()) {
            if (reserva.getEstado() == EstadoReserva.FINALIZADA) {
                for (Pasajero pasajero : reserva.getPasajeros()) {
                    total += pasajero.calcularTotalServicios();
                }
            }
        }
        return total;
    }
}