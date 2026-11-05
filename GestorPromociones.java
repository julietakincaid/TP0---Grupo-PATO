
package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GestorPromociones implements CalculadorPromociones {

    private List<Promocion> promociones;

    public GestorPromociones() {
        promociones = new ArrayList<>();

        promociones.add(new PromocionAnticipada());
        promociones.add(new PromocionGrupo());
    }

    public void agregarPromocion(Promocion promocion) {
        if (promocion == null) {
            throw new IllegalArgumentException(
                    "La promocion no puede ser nula."
            );
        }

        promociones.add(promocion);
    }

    @Override
    public double calcularMejorDescuento(
            LocalDate fechaReserva,
            LocalDate fechaSalida,
            int cantidadPasajeros,
            double subtotal
    ) {
        double mayorDescuento = 0;

        for (Promocion promocion : promociones) {

            if (promocion.esAplicable(
                    fechaReserva,
                    fechaSalida,
                    cantidadPasajeros
            )) {
                double descuento =
                        promocion.calcularDescuento(subtotal);

                if (descuento > mayorDescuento) {
                    mayorDescuento = descuento;
                }
            }
        }

        return mayorDescuento;
    }
}
