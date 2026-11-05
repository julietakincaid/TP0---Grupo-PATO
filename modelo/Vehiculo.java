package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.enums.EstadoSalida;
import modelo.enums.TipoVehiculo;

public class Vehiculo {
    private String patente;
    private TipoVehiculo tipo;
    private int capacidad;
    private final List<Salida> salidasAsignadas = new ArrayList<>();

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

    
    public boolean tieneCapacidad(int cupo) {
        return capacidad >= cupo;
    }

   
    public boolean estaDisponible(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null || inicio.isAfter(fin)) {
            throw new IllegalArgumentException("El rango de fechas es inválido.");
        }
        for (Salida s : salidasAsignadas) {
            if (s.getEstado() != EstadoSalida.CANCELADA && s.seSuperponeCon(inicio, fin)) {
                return false;
            }
        }
        return true;
    }

    public List<Salida> getSalidasAsignadas() {
        return Collections.unmodifiableList(salidasAsignadas);
    }

    void registrarSalida(Salida salida) {
        if (!salidasAsignadas.contains(salida)) {
            salidasAsignadas.add(salida);
        }
    }

    void quitarSalida(Salida salida) {
        salidasAsignadas.remove(salida);
    }
}
