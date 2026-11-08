package modelo;

public class PromocionAnticipada extends Promocion {
    private int diasMinimoAnticipacion;

    public PromocionAnticipada() {
        super("Reserva anticipada", 0.15);
        setDiasMinimoAnticipacion(60);
    }

    public int getDiasMinimoAnticipacion() {
        return diasMinimoAnticipacion;
    }

    public void setDiasMinimoAnticipacion(int diasMinimoAnticipacion) {
        if (diasMinimoAnticipacion < 0) {
            throw new IllegalArgumentException("Los días de anticipación no pueden ser negativos.");
        }
        this.diasMinimoAnticipacion = diasMinimoAnticipacion;
    }

    @Override
    public boolean esAplicable(int cantidadPasajeros, int diasAnticipacion) {
        return diasAnticipacion > diasMinimoAnticipacion;
    }
}
