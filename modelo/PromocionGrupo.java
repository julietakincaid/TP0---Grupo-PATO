package modelo;

public class PromocionGrupo extends Promocion {
    private int cantidadMinimaPasajeros;

    public PromocionGrupo() {
        super("Promoción grupal", 0.10);
        setCantidadMinimaPasajeros(6);
    }

    public int getCantidadMinimaPasajeros() {
        return cantidadMinimaPasajeros;
    }

    public void setCantidadMinimaPasajeros(int cantidadMinimaPasajeros) {
        if (cantidadMinimaPasajeros < 2) {
            throw new IllegalArgumentException("Un grupo tiene al menos 2 pasajeros.");
        }
        this.cantidadMinimaPasajeros = cantidadMinimaPasajeros;
    }

    @Override
    public boolean esAplicable(int cantidadPasajeros, int diasAnticipacion) {
        return cantidadPasajeros >= cantidadMinimaPasajeros;
    }
}
