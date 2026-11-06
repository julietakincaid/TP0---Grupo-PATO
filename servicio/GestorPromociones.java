
package servicio;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import modelo.Reserva;

public class GestorPromociones implements CalculadorPromociones {

    private List<Promocion> promociones;

    public GestorPromociones() {
        promociones = new ArrayList<>();

        promociones.add(new PromocionAnticipada());
        promociones.add(new PromocionGrupo());
    }

    public List<Promocion> getPromociones() {
        return new ArrayList<>(promociones);
    }

    public void agregarPromocion(Promocion promocion) {
        if (promocion == null) {
            throw new IllegalArgumentException(
                    "La promocion no puede ser nula.");
        }

        if (promociones.contains(promocion)) {
            throw new IllegalArgumentException(
                    "La promocion ya esta registrada.");
        }

        promociones.add(promocion);
    }

    @Override
    public double calcularMejorDescuento(Reserva reserva, double subtotal) {

        if (reserva == null) {
            throw new IllegalArgumentException(
                    "La reserva no puede ser nula.");
        }

        if (subtotal < 0) {
            throw new IllegalArgumentException(
                    "El subtotal no puede ser negativo.");
        }

        int cantidadPasajeros = reserva.cantidadPasajeros();

        long dias = ChronoUnit.DAYS.between(
                reserva.getFecha(),
                reserva.getSalida().getFechaInicio()
        );

        int diasAnticipacion = Math.toIntExact(dias);

        double mejorPorcentaje = 0;

        for (Promocion promocion : promociones) {

            if (promocion.esAplicable(
                    cantidadPasajeros,
                    diasAnticipacion)) {

                if (promocion.getPorcentajeDescuento() > mejorPorcentaje) {
                    mejorPorcentaje = promocion.getPorcentajeDescuento();
                }
            }
        }

        return subtotal * mejorPorcentaje / 100.0;
    }
}
