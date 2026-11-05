package servicio;

import modelo.enums.TipoPasajero;

public class CalculadoraPrecioReserva {
    private CalculadorPromociones calculadorPromociones;

    public CalculadoraPrecioReserva(CalculadorPromociones calculadorPromociones) {
        this.calculadorPromociones = calculadorPromociones;
    }

    public double calcularPrecioPasajero(double tarifaBase, int edad, TipoPasajero tipo) {
        if (tarifaBase < 0) {
            throw new IllegalArgumentException(
                    "La tarifa no puede ser negativa.");
        }

        if (edad < 0) {
            throw new IllegalArgumentException(
                    "La edad no puede ser negativa.");
        }

        if (edad < 3) {
            return 0;
        } else if (edad <= 11) {
            return tarifaBase * 0.60;
        } else if (edad <= 17) {
            return tarifaBase;
        } else if (tipo == TipoPasajero.JUBILADO) {
            return tarifaBase * 0.90;
        } else {
            return tarifaBase;
        }
    }

    public double aplicarDescuento(double subtotal, double porcentajeDescuento) {
        if (subtotal < 0) {
            throw new IllegalArgumentException(
                    "El subtotal no puede ser negativo.");
        }

        if (porcentajeDescuento < 0 || porcentajeDescuento > 1) {
            throw new IllegalArgumentException(
                    "El descuento debe estar entre 0 y 1.");
        }
        return subtotal * (1 - porcentajeDescuento);
    }

    public double calcularPrecioConDescuento(
            double subtotal,
            int cantidadPasajeros,
            int diasAnticipacion) {

        if (cantidadPasajeros <= 0) {
            throw new IllegalArgumentException("La cantidad de pasajeros debe ser positiva.");
        }

        double descuento = calculadorPromociones.obtenerMejorDescuento(cantidadPasajeros, diasAnticipacion);
        return aplicarDescuento(subtotal, descuento);
    }
}
