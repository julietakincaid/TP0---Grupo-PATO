package servicio;

public class PromocionAnticipada extends Promocion {
    public PromocionAnticipada() {
        super("Reserva anticipada", 15);
    }

    @Override
    public boolean esAplicable(int cantidadPasajeros, int diasAnticipacion) {
        return diasAnticipacion > 60;
    }
}
