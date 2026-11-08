package servicio;

import excepcion.ReglaNegocioException;
import modelo.NotaAjuste;
import modelo.Pasajero;
import modelo.Promocion;
import modelo.Reserva;
import modelo.enums.TipoPasajero;

public class CalculadoraPrecioReserva {
    private static final int EDAD_GRATIS = 3;                 // menores de 3 años no pagan
    private static final int EDAD_MENOR = 12;                 // menores de 12 años pagan el 60%
    private static final double PORCENTAJE_MENOR = 0.60;
    private static final double PORCENTAJE_JUBILADO = 0.90;   // 10% de descuento

    private final CalculadorPromociones calculadorPromociones;

    public CalculadoraPrecioReserva(CalculadorPromociones calculadorPromociones) {
        if (calculadorPromociones == null) {
            throw new IllegalArgumentException("El calculador de promociones es obligatorio.");
        }
        this.calculadorPromociones = calculadorPromociones;
    }

    // aclaración: de 12 a 17 años pagan la tarifa completa
    public double calcularPrecioPasajero(double tarifaBase, int edad, TipoPasajero tipo) {
        if (!Double.isFinite(tarifaBase) || tarifaBase < 0) {
            throw new IllegalArgumentException("La tarifa no puede ser negativa.");
        }
        if (edad < 0) {
            throw new IllegalArgumentException("La edad no puede ser negativa.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de pasajero es obligatorio.");
        }

        double precio;
        if (edad < EDAD_GRATIS) {
            precio = 0;
        } else if (edad < EDAD_MENOR) {
            precio = tarifaBase * PORCENTAJE_MENOR;
        } else if (tipo == TipoPasajero.JUBILADO) {
            precio = tarifaBase * PORCENTAJE_JUBILADO;
        } else {
            precio = tarifaBase;
        }
        return redondear(precio);
    }

    // la tarifa de todos los pasajeros, sin servicios ni descuentos (usa la tarifa fijada en la reserva)
    public double calcularSubtotalTarifas(Reserva reserva) {
        validarReserva(reserva);
        double subtotal = 0;
        for (Pasajero pasajero : reserva.getPasajeros()) {
            int edad = pasajero.calcularEdad(reserva.getSalida().getFechaInicio());
            subtotal += calcularPrecioPasajero(reserva.getPrecioBase(), edad, pasajero.getTipo());
        }
        return subtotal;
    }

    // los servicios adicionales contratados por todos los pasajeros
    public double calcularTotalServicios(Reserva reserva) {
        validarReserva(reserva);
        double total = 0;
        for (Pasajero pasajero : reserva.getPasajeros()) {
            total += pasajero.calcularTotalServicios();
        }
        return total;
    }

    // la promoción más favorable según la cantidad de pasajeros y la anticipación con que se reservó
    public Promocion obtenerPromocion(Reserva reserva) {
        validarReserva(reserva);
        return calculadorPromociones.obtenerMejorPromocion(reserva.cantidadPasajeros(), calcularDiasAnticipacion(reserva));
    }

    // calcula el total sin modificar la reserva:
    // (tarifas + servicios) con el descuento de la promoción + notas de ajuste
    public double calcularTotal(Reserva reserva) {
        validarReserva(reserva);

        double subtotal = calcularSubtotalTarifas(reserva) + calcularTotalServicios(reserva);
        double descuento = 0;
        Promocion promocion = obtenerPromocion(reserva);
        if (promocion != null) {
            descuento = promocion.getPorcentajeDescuento();
        }

        double total = subtotal * (1 - descuento);
        for (NotaAjuste nota : reserva.getNotasAjuste()) {
            total += nota.getImporte();
        }
        return redondear(total);
    }

    // calcula el total y lo guarda en la reserva junto con la promoción aplicada
    public double recalcularTotal(Reserva reserva) throws ReglaNegocioException {
        double total = calcularTotal(reserva);
        if (total < 0) {
            throw new ReglaNegocioException("El total de la reserva no puede ser negativo.");
        }
        reserva.asignarPromocion(obtenerPromocion(reserva));
        reserva.actualizarTotal(total);
        return total;
    }

    // días entre el día en que se reservó y el inicio de la salida
    private int calcularDiasAnticipacion(Reserva reserva) {
        long dias = reserva.getSalida().getFechaInicio().toEpochDay() - reserva.getFecha().toEpochDay();
        return (int) dias;
    }

    private void validarReserva(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula.");
        }
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
