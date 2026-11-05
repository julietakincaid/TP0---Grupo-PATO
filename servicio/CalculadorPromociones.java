package modelo;

public interface CalculadorPromociones {

    double calcularMejorDescuento(
            Reserva reserva,
            double subtotal
    );
}
