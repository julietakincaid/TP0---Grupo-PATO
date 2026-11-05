package modelo;

import java.time.LocalDate;

public class CalculadoraPrecioReserva {

    private CalculadorPromociones calculadorPromociones;

    public CalculadoraPrecioReserva(
            CalculadorPromociones calculadorPromociones) {

        if (calculadorPromociones == null) {
            throw new IllegalArgumentException(
                    "El calculador de promociones no puede ser nulo."
            );
        }

        this.calculadorPromociones = calculadorPromociones;
    }

    public double calcularPrecioFinal(
            double subtotal,
            LocalDate fechaReserva,
            LocalDate fechaSalida,
            int cantidadPasajeros) {

        if (subtotal < 0) {
            throw new IllegalArgumentException(
                    "El subtotal no puede ser negativo."
            );
        }

        double descuento =
                calculadorPromociones.calcularMejorDescuento(
                        fechaReserva,
                        fechaSalida,
                        cantidadPasajeros,
                        subtotal
                );

        return subtotal - descuento;
    }
}
