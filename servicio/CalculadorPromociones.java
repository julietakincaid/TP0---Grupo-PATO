package servicio;

import modelo.Promocion;

public interface CalculadorPromociones {
    Promocion obtenerMejorPromocion(int cantidadPasajeros, int diasAnticipacion);
}
