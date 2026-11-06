
package controlador;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import servicio.CalculadoraPrecioReserva;
import servicio.CalculadorPromociones;
import servicio.GestorPromociones;

public class ControladorReservas {

    private final CalculadoraPrecioReserva calculadoraPrecio;

    public ControladorReservas() {
        CalculadorPromociones gestor = new GestorPromociones();
        this.calculadoraPrecio = new CalculadoraPrecioReserva(gestor);
    }

    public double consultarPrecioFinal(
            double subtotal,
            LocalDate fechaReserva,
            LocalDate fechaSalida,
            int cantidadPasajeros) {

        if (fechaReserva == null || fechaSalida == null) {
            throw new IllegalArgumentException(
                    "Las fechas de reserva y salida son obligatorias.");
        }

        if (fechaSalida.isBefore(fechaReserva)) {
            throw new IllegalArgumentException(
                    "La fecha de salida no puede ser anterior a la reserva.");
        }

        long dias = ChronoUnit.DAYS.between(
                fechaReserva,
                fechaSalida
        );

        int diasAnticipacion = Math.toIntExact(dias);

        return calculadoraPrecio.calcularPrecioConDescuento(
                subtotal,
                cantidadPasajeros,
                diasAnticipacion
        );
    }
}
