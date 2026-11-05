package modelo;

import java.time.LocalDate;

public interface CalculadorPromociones {

    double calcularMejorDescuento(
            LocalDate fechaReserva,
            LocalDate fechaSalida,
            int cantidadPasajeros,
            double subtotal
    );
}
