package servicio;

import java.util.ArrayList;
import java.util.List;
import servicio.Promocion;
import servicio.PromocionAnticipada;
import servicio.PromocionGrupo;

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
        if (promociones.contains(promocion)) {
            throw new IllegalArgumentException("La promocion ya esta registrada.");
        }
        promociones.add(promocion);
    }

    @Override
    public double obtenerMejorDescuento(int cantidadPasajeros, int diasAnticipacion) {
        double mejorDescuento = 0;
        for (Promocion promocion : promociones) {
            if (promocion.esAplicable(cantidadPasajeros, diasAnticipacion)) {
                if (promocion.getPorcentajeDescuento() > mejorDescuento) {
                    mejorDescuento = promocion.getPorcentajeDescuento();
                }
            }
        }
        return mejorDescuento;
    }
}
