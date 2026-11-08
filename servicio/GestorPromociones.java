package servicio;

import excepcion.ReglaNegocioException;
import modelo.Promocion;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

public class GestorPromociones implements CalculadorPromociones, Serializable {
    private List<Promocion> promociones;

    public GestorPromociones() {
        promociones = new ArrayList<>();
    }

    public List<Promocion> getPromociones() {
        return new ArrayList<>(promociones);
    }

    public void agregarPromocion(Promocion promocion) throws ReglaNegocioException {
        if (promocion == null) {
            throw new IllegalArgumentException("La promoción no puede ser nula.");
        }
        if (promociones.contains(promocion)) {
            throw new ReglaNegocioException("La promoción ya está registrada.");
        }
        promociones.add(promocion);
    }

    public void quitarPromocion(Promocion promocion) throws ReglaNegocioException {
        if (promocion == null) {
            throw new IllegalArgumentException("La promoción no puede ser nula.");
        }
        if (!promociones.remove(promocion)) {
            throw new ReglaNegocioException("La promoción no está registrada.");
        }
    }

    @Override
    public Promocion obtenerMejorPromocion(int cantidadPasajeros, int diasAnticipacion) {
        Promocion mejor = null;

        for (Promocion promocion : promociones) {
            if (promocion.esAplicable(cantidadPasajeros, diasAnticipacion)) {
                if (mejor == null || promocion.getPorcentajeDescuento() > mejor.getPorcentajeDescuento()) {
                    mejor = promocion;
                }
            }
        }
        return mejor;
    }
}
