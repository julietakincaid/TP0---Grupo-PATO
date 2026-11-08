package modelo;

import modelo.enums.TipoVehiculo;
import java.io.Serializable;

public class Vehiculo implements Serializable{
    private String patente;    // se guarda en mayúsculas, sin espacios ni guiones
    private TipoVehiculo tipo;
    private int capacidad;

    public Vehiculo(String patente, TipoVehiculo tipo, int capacidad) {
        setPatente(patente);
        setTipo(tipo);
        setCapacidad(capacidad);
    }

    public String getPatente() {
        return patente;
    }
    public TipoVehiculo getTipo() {
        return tipo;
    }
    public int getCapacidad() {
        return capacidad;
    }

    private void setPatente(String patente) {
        if (patente == null || patente.isBlank()) {
            throw new IllegalArgumentException("La patente es obligatoria.");
        }

        // saco espacios y guiones, y paso a mayúsculas
        String limpia = patente.replace(" ", "").replace("-", "").toUpperCase();

        if (limpia.length() < 6 || limpia.length() > 7) {
            throw new IllegalArgumentException("La patente debe tener 6 o 7 caracteres.");
        }

        // reviso que cada caracter sea una letra o un número
        for (int i = 0; i < limpia.length(); i++) {
            if (!Character.isLetterOrDigit(limpia.charAt(i))) {
                throw new IllegalArgumentException("La patente solo puede contener letras y números.");
            }
        }
        this.patente = limpia;
    }

    public void setTipo(TipoVehiculo tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de vehículo es obligatorio.");
        }
        this.tipo = tipo;
    }

    public void setCapacidad(int capacidad) {
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor a cero.");
        }
        this.capacidad = capacidad;
    }

    // true si el vehículo alcanza para ese cupo
    public boolean tieneCapacidad(int cupo) {
        if (cupo < 0) {
            throw new IllegalArgumentException("El cupo no puede ser negativo.");
        }
        return capacidad >= cupo;
    }

    // dos vehículos son el mismo si tienen la misma patente
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Vehiculo)) {
            return false;
        }
        Vehiculo otro = (Vehiculo) o;
        return patente.equals(otro.patente);
    }

    @Override
    public int hashCode() {
        return patente.hashCode();
    }

    @Override
    public String toString() {
        return patente + " - " + tipo + " (capacidad " + capacidad + ")";
    }
}
