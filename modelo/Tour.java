package modelo;

import excepcion.ReglaNegocioException;
import modelo.enums.CategoriaTour;
import modelo.enums.TipoActividad;
import modelo.enums.Temporada;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.LocalTime;
import java.time.LocalDate;
import java.io.Serializable;

public class Tour implements Serializable{
    private String codigo;
    private String nombre;
    private String descripcion;
    private int duracionDias;
    private CategoriaTour categoria;
    private int cupoMinimo;
    private int cupoMaximo;
    private boolean activo;

    private List<Actividad> actividades;
    private List<Tarifa> tarifas;
    private List<ServicioTour> servicios;

    public Tour(String codigo, String nombre, String descripcion, int duracionDias, CategoriaTour categoria, int cupoMinimo, int cupoMaximo) {
        this.actividades = new ArrayList<>();
        this.tarifas = new ArrayList<>();
        this.servicios = new ArrayList<>();
        this.activo = true;

        setCodigo(codigo);
        setNombre(nombre);
        setDescripcion(descripcion);
        validarDuracion(duracionDias);
        this.duracionDias = duracionDias;
        setCategoria(categoria);
        setCupos(cupoMinimo, cupoMaximo);
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
    public boolean estaActivo() {return activo;}

    public void darDeBaja() {
        this.activo = false;
    }

    private void setCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del tour es obligatorio.");
        }
        this.codigo = codigo.strip();
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del tour es obligatorio.");
        }
        this.nombre = nombre.strip();
    }

    public void setDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria.");
        }
        this.descripcion = descripcion.strip();
    }

    private void validarDuracion(int duracionDias) {
        if (duracionDias <= 0) {
            throw new IllegalArgumentException("La duración debe ser mayor a cero.");
        }
    }

    // modificar la duracion de un tour ya creado
    public void setDuracionDias(int duracionDias) throws ReglaNegocioException {
        validarDuracion(duracionDias);
        for (Actividad actividad : actividades) {
            if (actividad.getDia() > duracionDias) {
                throw new ReglaNegocioException("Existen actividades fuera de la nueva duración.");
            }
        }
        this.duracionDias = duracionDias;
    }

    private void setCategoria(CategoriaTour categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría es obligatoria.");
        }
        // un tour de aventura necesita edad mínima y dificultad, que están en TourAventura
        if (categoria == CategoriaTour.AVENTURA && !(this instanceof TourAventura)) {
            throw new IllegalArgumentException("Los tours de aventura deben crearse como TourAventura.");
        }
        this.categoria = categoria;
    }

    public void setCupos(int cupoMinimo, int cupoMaximo) {
        if (cupoMinimo <= 0 || cupoMaximo < cupoMinimo) {
            throw new IllegalArgumentException("Los cupos del tour no son válidos.");
        }
        this.cupoMinimo = cupoMinimo;
        this.cupoMaximo = cupoMaximo;
    }

    // actividades
    public Actividad agregarActividad(int dia, int orden, TipoActividad tipo, LocalTime hora, String descripcion, String lugar) throws ReglaNegocioException {
        Actividad nueva = new Actividad(dia, orden, tipo, hora, descripcion, lugar);
        if (dia > duracionDias) {
            throw new ReglaNegocioException("El día de la actividad está fuera de la duración del tour.");
        }
        validarOrdenUnico(dia, orden, null);
        validarHorario(dia, orden, hora, null);

        actividades.add(nueva);
        return nueva;
    }

    public void quitarActividad(Actividad actividad) {
        if (!actividades.remove(actividad)) {
            throw new IllegalArgumentException("La actividad no pertenece al tour.");
        }
    }

    public void modificarHorario(Actividad actividad, LocalTime horario) throws ReglaNegocioException {
        validarPertenece(actividad);
        if (horario == null) {
            throw new IllegalArgumentException("El horario es obligatorio.");
        }
        validarHorario(actividad.getDia(), actividad.getOrden(), horario, actividad);
        actividad.setHorario(horario);
    }

    public void modificarOrden(Actividad actividad, int orden) throws ReglaNegocioException {
        validarPertenece(actividad);
        if (orden <= 0) {
            throw new IllegalArgumentException("El orden debe ser mayor a cero.");
        }
        validarOrdenUnico(actividad.getDia(), orden, actividad);
        validarHorario(actividad.getDia(), orden, actividad.getHorario(), actividad);
        actividad.setOrden(orden);
    }

    public List<Actividad> getActividades() {
        List<Actividad> copia = new ArrayList<>(actividades);
        Collections.sort(copia);
        return copia;
    }

    private void validarPertenece(Actividad actividad) {
        if (actividad == null || !actividades.contains(actividad)) {
            throw new IllegalArgumentException("La actividad no pertenece al tour.");
        }
    }

    // "ignorar" es la actividad que se está modificando (para no compararla consigo misma)
    private void validarOrdenUnico(int dia, int orden, Actividad ignorar) throws ReglaNegocioException {
        for (Actividad otra : actividades) {
            if (otra != ignorar && otra.getDia() == dia && otra.getOrden() == orden) {
                throw new ReglaNegocioException("Ya existe una actividad con ese día y orden.");
            }
        }
    }

    // dentro del mismo día, a mayor orden le corresponde un horario posterior
    private void validarHorario(int dia, int orden, LocalTime horario, Actividad ignorar) throws ReglaNegocioException {
        for (Actividad otra : actividades) {
            if (otra != ignorar && otra.getDia() == dia) {
                if (otra.getOrden() < orden && !otra.getHorario().isBefore(horario)) {
                    throw new ReglaNegocioException("El horario debe ser posterior al de las actividades anteriores del día.");
                }
                if (otra.getOrden() > orden && !otra.getHorario().isAfter(horario)) {
                    throw new ReglaNegocioException("El horario debe ser anterior al de las actividades siguientes del día.");
                }
            }
        }
    }

    // tarifas
    public void agregarTarifa(Temporada temporada, LocalDate fechaDesde, LocalDate fechaHasta, double precioBase) throws ReglaNegocioException {
        Tarifa nueva = new Tarifa(temporada, fechaDesde, fechaHasta, precioBase); // valida los datos

        for (Tarifa tarifa : tarifas) {
            if (seSuperponen(fechaDesde, fechaHasta, tarifa.getFechaDesde(), tarifa.getFechaHasta())) {
                throw new ReglaNegocioException("Ya existe una tarifa con fechas superpuestas.");
            }
        }
        tarifas.add(nueva);
    }

    public void quitarTarifa(Tarifa tarifa) {
        if (!tarifas.remove(tarifa)) {
            throw new IllegalArgumentException("La tarifa no pertenece al tour.");
        }
    }

    public void modificarFechasTarifa(Tarifa tarifa, LocalDate fechaDesde, LocalDate fechaHasta) throws ReglaNegocioException {
        if (tarifa == null || !tarifas.contains(tarifa)) {
            throw new IllegalArgumentException("La tarifa no pertenece a este tour.");
        }
        if (fechaDesde == null || fechaHasta == null || fechaDesde.isAfter(fechaHasta)) {
            throw new IllegalArgumentException("Las fechas no son válidas.");
        }

        for (Tarifa otra : tarifas) {
            if (otra != tarifa && seSuperponen(fechaDesde, fechaHasta, otra.getFechaDesde(), otra.getFechaHasta())) {
                throw new ReglaNegocioException("Las fechas se superponen con otra tarifa.");
            }
        }
        tarifa.setFechas(fechaDesde, fechaHasta);
    }

    public Tarifa obtenerTarifaVigente(LocalDate fecha) throws ReglaNegocioException {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        for (Tarifa tarifa : tarifas) {
            if (tarifa.estaVigente(fecha)) {
                return tarifa;
            }
        }
        throw new ReglaNegocioException("No existe una tarifa vigente para la fecha indicada.");
    }

    public List<Tarifa> getTarifas() {
        return new ArrayList<>(tarifas);
    }

    private boolean seSuperponen(LocalDate desde1, LocalDate hasta1, LocalDate desde2, LocalDate hasta2) {
        return !hasta1.isBefore(desde2) && !desde1.isAfter(hasta2);
    }

    // servicios adicionales
    public void agregarServicio(ServicioAdicional servicioAdicional, boolean obligatorio) throws ReglaNegocioException {
        if (servicioAdicional == null) {
            throw new IllegalArgumentException("El servicio adicional es obligatorio.");
        }
        if (ofreceServicio(servicioAdicional)) {
            throw new ReglaNegocioException("El servicio ya está asociado a este tour.");
        }
        servicios.add(new ServicioTour(servicioAdicional, obligatorio));
    }

    public void quitarServicio(ServicioAdicional servicioAdicional) throws ReglaNegocioException {
        if (servicioAdicional == null) {
            throw new IllegalArgumentException("El servicio no puede ser nulo.");
        }
        ServicioTour servicioTour = buscarServicio(servicioAdicional);
        if (servicioTour == null) {
            throw new ReglaNegocioException("El servicio no pertenece a este tour.");
        }
        servicios.remove(servicioTour);
    }

    public void modificarObligatorio(ServicioAdicional servicioAdicional, boolean obligatorio)
            throws ReglaNegocioException {
        if (servicioAdicional == null) {
            throw new IllegalArgumentException("El servicio no puede ser nulo.");
        }
        ServicioTour servicioTour = buscarServicio(servicioAdicional);
        if (servicioTour == null) {
            throw new ReglaNegocioException("El servicio no pertenece a este tour.");
        }
        servicioTour.setObligatorio(obligatorio);
    }

    public boolean ofreceServicio(ServicioAdicional servicioAdicional) {
        return buscarServicio(servicioAdicional) != null;
    }

    public List<ServicioAdicional> getServiciosObligatorios() {
        List<ServicioAdicional> obligatorios = new ArrayList<>();
        for (ServicioTour servicio : servicios) {
            if (servicio.esObligatorio()) {
                obligatorios.add(servicio.getServicioAdicional());
            }
        }
        return obligatorios;
    }

    public List<ServicioTour> getServicios() {
        return new ArrayList<>(servicios);
    }

    private ServicioTour buscarServicio(ServicioAdicional servicioAdicional) {
        for (ServicioTour servicio : servicios) {
            if (servicio.getServicioAdicional().equals(servicioAdicional)) {
                return servicio;
            }
        }
        return null;
    }

    // reglas del tour
    public boolean cumpleCondiciones(Pasajero pasajero, LocalDate fechaSalida) {
        if (pasajero == null || fechaSalida == null) {
            throw new IllegalArgumentException("El pasajero y la fecha son obligatorios.");
        }
        return true;
    }

    // se llama antes de programar una salida
    public void validarConfiguracion() throws ReglaNegocioException {
        if (actividades.isEmpty()) {
            throw new ReglaNegocioException("El tour debe tener al menos una actividad.");
        }
        // tarifa para cada temporada
        for (Temporada temporada : Temporada.values()) {
            if (!tieneTarifaPara(temporada)) {
                throw new ReglaNegocioException("El tour debe tener una tarifa para la temporada " + temporada + ".");
            }
        }
    }

    private boolean tieneTarifaPara(Temporada temporada) {
        for (Tarifa tarifa : tarifas) {
            if (tarifa.getTemporada() == temporada) {
                return true;
            }
        }
        return false;
    }

    // dos tours son el mismo si tienen el mismo código
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tour)) return false;
        Tour otro = (Tour) o;
        return codigo.equals(otro.codigo);
    }

    @Override
    public int hashCode() {
        return codigo.hashCode();
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre + " (" + categoria + ", " + duracionDias + " días)";
    }
}
