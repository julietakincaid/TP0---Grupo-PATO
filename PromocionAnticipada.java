package modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PromocionAnticipada extends Promocion {

    public PromocionAnticipada() {
        super("Reserva anticipada", 0.15);
    }

    @Override
    public boolean esAplicable(
            LocalDate fechaReserva,
            LocalDate fechaSalida,
            int cantidadPasajeros
    ) {
        long diasAnticipacion = ChronoUnit.DAYS.between(
                fechaReserva, fechaSalida
        );

        return diasAnticipacion > 60;
    }
}
