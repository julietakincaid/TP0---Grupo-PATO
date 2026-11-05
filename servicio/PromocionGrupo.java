package servicio;

public class PromocionGrupo extends Promocion {
    public PromocionGrupo() {
        super("Promocion grupal", 10);
    }

    @Override
    public boolean esAplicable(int cantidadPasajeros, int diasAnticipacion) {
        return cantidadPasajeros >= 6;
    }
}
