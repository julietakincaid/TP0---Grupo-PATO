package modelo;

import java.time.LocalTime;
import modelo.enums.TipoActividad;

public class Actividad {
    private int dia;
    private int orden;
    private TipoActividad tipo;
    private LocalTime horario;
    private String descripcion;
    private String lugar;

    public Actividad(int dia, int orden, TipoActividad tipo, LocalTime horario, String descripcion, String lugar) {
        setDia(dia);
        setOrden(orden);
        this.tipo = tipo;
        this.horario = horario;
        this.descripcion = descripcion;
        this.lugar = lugar;
    }

    public int getDia() {
        return dia;
    }

    public int getOrden() {
        return orden;
    }

    public TipoActividad getTipo() {
        return tipo;
    }

    public LocalTime getHorario() {
        return horario;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getLugar() {
        return lugar;
    }

    public void setDia(int dia) {
        if (dia <= 0) {
            throw new IllegalArgumentException("El día debe ser mayor a cero.");
        }
        this.dia = dia;
    }

    public void setOrden(int orden) {
        if (orden <= 0) {
            throw new IllegalArgumentException("El orden debe ser mayor a cero.");
        }
        this.orden = orden;
    }

    public void setTipo(TipoActividad tipo) {
        this.tipo = tipo;
    }

    public void setHora(LocalTime hora) {
        this.horario = horario;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }
}
