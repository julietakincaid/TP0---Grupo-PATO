package controlador;

import modelo.Reserva;
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
            Reserva reserva,
            double subtotal) {

        if (reserva == null) {
            throw new IllegalArgumentException(
                    "La reserva no puede ser nula.");
        }

        if (subtotal < 0) {
            throw new IllegalArgumentException(
                    "El subtotal no puede ser negativo.");
        }

        if (reserva.getSalida().getFechaInicio()
                .isBefore(reserva.getFecha())) {
            throw new IllegalArgumentException(
                    "La fecha de salida no puede ser anterior a la reserva.");
        }

        return calculadoraPrecio.calcularPrecioConDescuento(
                reserva,
                subtotal
        );
    }
}
