package modelo;

import java.time.LocalDate;

public class PromocionGrupo extends Promocion {

    public PromocionGrupo() {
        super("Promocion por grupo", 0.10);
    }

    @Override
    public boolean esAplicable(
            LocalDate fechaReserva,
            LocalDate fechaSalida,
            int cantidadPasajeros
    ) {
        return cantidadPasajeros >= 6;
    }
}
