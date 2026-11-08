package modelo;

import java.time.LocalTime;
import modelo.enums.TipoActividad;
import java.io.Serializable;

public class Actividad implements Comparable<Actividad>, Serializable{
    private int dia;
    private int orden;
    private TipoActividad tipo;
    private LocalTime horario;
    private String descripcion;
    private String lugar;
    private Proveedor proveedor;

    Actividad(int dia, int orden, TipoActividad tipo, LocalTime horario, String descripcion, String lugar) {
        setDia(dia);
        setOrden(orden);
        setTipo(tipo);
        setHorario(horario);
        setDescripcion(descripcion);
        setLugar(lugar);
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

    void setDia(int dia) {
        if (dia <= 0) {
            throw new IllegalArgumentException("El día debe ser mayor a cero.");
        }
        this.dia = dia;
    }

    void setOrden(int orden) {
        if (orden <= 0) {
            throw new IllegalArgumentException("El orden debe ser mayor a cero.");
        }
        this.orden = orden;
    }

    public void setTipo(TipoActividad tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de actividad es obligatorio.");
        }
        this.tipo = tipo;
    }

    void setHorario(LocalTime horario) {
        if (horario == null) {
            throw new IllegalArgumentException("El horario es obligatorio.");
        }
        this.horario = horario;
    }

    public void setDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria.");
        }
        this.descripcion = descripcion;
    }

    public void setLugar(String lugar) {
        if (lugar == null || lugar.isBlank()) {
            throw new IllegalArgumentException("El lugar es obligatorio.");
        }
        this.lugar = lugar;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    // null = actividad sin servicio de terceros
    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public String obtenerDescripcion() {
        return "Día " + dia + " - " + horario + " - " + tipo + ": " + descripcion
                + " (" + lugar + ")"
                + (proveedor != null ? " | Proveedor: " + proveedor.getNombre() : "");
    }

    @Override
    public int compareTo(Actividad otra) {
        int porDia = Integer.compare(this.dia, otra.dia);
        if (porDia != 0) {
            return porDia;      // los días son distintos: ya sé cuál va antes
        } else {
            return Integer.compare(this.orden, otra.orden);  // mismo día: desempata por orden
        }
    }
}
