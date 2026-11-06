package servicio;

import modelo.Reserva;

public interface CalculadorPromociones {

    double calcularMejorDescuento(Reserva reserva, double subtotal);

}
