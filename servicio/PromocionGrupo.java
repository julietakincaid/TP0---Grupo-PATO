
package modelo;

public class PromocionGrupo extends Promocion {

    private int cantidadMinimaPasajeros;

    public PromocionGrupo() {
        super("Promocion por grupo", 0.10);
        this.cantidadMinimaPasajeros = 6;
    }

    @Override
    public boolean esAplicable(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException(
                "La reserva no puede ser nula."
            );
        }

        return reserva.cantidadPasajeros() >= cantidadMinimaPasajeros;
    }

    public int getCantidadMinimaPasajeros() {
        return cantidadMinimaPasajeros;
    }
}
