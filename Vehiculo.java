package modelo;

import modelo.enums.TipoVehiculo;

public class Vehiculo {
    private String patente;
    private TipoVehiculo tipo;
    private int capacidad;

    public Vehiculo(String patente, TipoVehiculo tipo, int capacidad) {
        this.patente = patente;
        this.tipo = tipo;
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

    public void setTipo(TipoVehiculo tipo) {
        this.tipo = tipo;
    }

    public void setCapacidad(int capacidad) {
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor a cero.");
        }
        this.capacidad = capacidad;
    }
}