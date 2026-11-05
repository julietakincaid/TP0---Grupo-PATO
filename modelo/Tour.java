package modelo;

import modelo.enums.CategoriaTour;
import modelo.enums.TipoActividad;
import modelo.enums.Temporada;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalTime;
import java.time.LocalDate;

public class Tour {
    private String codigo;
    private String nombre;
    private String descripcion;
    private int duracionDias;
    private CategoriaTour categoria;
    private int cupoMinimo;
    private int cupoMaximo;
    private List<Actividad> actividades;
    private List<Tarifa> tarifas;
    private List<ServicioTour> servicios;

    public Tour(String codigo, String nombre, String descripcion, int duracionDias, CategoriaTour categoria, int cupoMinimo, int cupoMaximo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.duracionDias = duracionDias;
        this.categoria = categoria;
        this.cupoMinimo = cupoMinimo;
        this.cupoMaximo = cupoMaximo;
        this.actividades = new ArrayList<>();
        this.tarifas = new ArrayList<>();
        this.servicios = new ArrayList<>();
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getDuracionDias() {
        return duracionDias;
    }

    public CategoriaTour getCategoria() {
        return categoria;
    }

    public int getCupoMinimo() {
        return cupoMinimo;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setDuracionDias(int duracionDias) {
        this.duracionDias = duracionDias;
    }

    public void setCupoMinimo(int cupoMinimo) {
        this.cupoMinimo = cupoMinimo;
    }

    public void setCupoMaximo(int cupoMaximo) {
        this.cupoMaximo = cupoMaximo;
    }

    public void agregarActividad(int dia, int orden, TipoActividad tipo, LocalTime hora, String descripcion, String lugar) {
        if (dia < 1 || dia > duracionDias) {
            throw new IllegalArgumentException("El dia debe estar dentro de la duracion del tour.");
        }

        for (Actividad actividad : actividades) {
            if (actividad.getDia() == dia && actividad.getOrden() == orden) {
                throw new IllegalArgumentException("Ya existe una actividad con ese dia y orden.");
            }
        }

        Actividad nueva = new Actividad(dia, orden, tipo, hora, descripcion, lugar);
        actividades.add(nueva);
    }

    public void quitarActividad(Actividad actividad) {
        actividades.remove(actividad);
    }

    public List<Actividad> getActividades() {
        return new ArrayList<>(actividades);
    }

    public void agregarTarifa(Temporada temporada, LocalDate fechaDesde, LocalDate fechaHasta, double precioBase) {
        for (Tarifa tarifa : tarifas) {
            if (tarifa.getTemporada() == temporada &&
                    !fechaHasta.isBefore(tarifa.getFechaDesde()) &&
                    !fechaDesde.isAfter(tarifa.getFechaHasta())) {

                throw new IllegalArgumentException("Ya existe una tarifa de esa temporada con fechas superpuestas.");
            }
        }
        Tarifa nueva = new Tarifa(temporada, fechaDesde, fechaHasta, precioBase);
        tarifas.add(nueva);
    }

    public void quitarTarifa(Tarifa tarifa) {
        tarifas.remove(tarifa);
    }

    public List<Tarifa> getTarifas() {
        return new ArrayList<>(tarifas);
    }

    public void agregarServicio(ServicioAdicional servicioAdicional, boolean obligatorio) {
        for (ServicioTour servicio : servicios) {
            if (servicio.getServicioAdicional() == servicioAdicional) {
                throw new IllegalArgumentException("El servicio ya esta asociado a este tour.");
            }
        }

        ServicioTour nuevo = new ServicioTour(this, servicioAdicional, obligatorio);
        servicios.add(nuevo);
    }

    public List<ServicioTour> getServicios() {
        return new ArrayList<>(servicios);
    }
}
