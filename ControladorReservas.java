package controlador;

import java.time.LocalDate;
import modelo.CalculadoraPrecioReserva;
import modelo.CalculadorPromociones;
import modelo.GestorPromociones;

public class ControladorReservas {

    private CalculadoraPrecioReserva calculadoraPrecio;

    public ControladorReservas() {
        CalculadorPromociones gestor = new GestorPromociones();
        this.calculadoraPrecio =
                new CalculadoraPrecioReserva(gestor);
    }

    public double consultarPrecioFinal(
            double subtotal,
            LocalDate fechaReserva,
            LocalDate fechaSalida,
            int cantidadPasajeros) {

        return calculadoraPrecio.calcularPrecioFinal(
                subtotal,
                fechaReserva,
                fechaSalida,
                cantidadPasajeros
        );
    }
}

