package modelo;

import excepcion.AsignacionInvalidaException;
import excepcion.CupoInsuficienteException;
import excepcion.EstadoInvalidoException;
import excepcion.ReglaNegocioException;
import modelo.enums.EstadoReserva;
import modelo.enums.EstadoSalida;
import modelo.enums.OrigenCancelacion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.Serializable;

public class Salida implements Serializable{
    private static final int DIAS_LIMITE_CUPO_MINIMO = 7;
    private static final int DIAS_LIMITE_RESERVA = 10;   // igual al plazo para pagar el saldo

    private int numero;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoSalida estado;
    private int cupoMinimo;
    private int cupoMaximo;
    private boolean listaPasajerosEmitida;
    private Tour tour;
    private Guia guia;
    private Vehiculo vehiculo;
    private LiquidacionGuia liquidacionGuia;
    private final List<Reserva> reservas;

    public Salida(int numero, LocalDate fechaInicio, LocalDate fechaFin, int cupoMinimo, int cupoMaximo, Tour tour) throws ReglaNegocioException {
        this.reservas = new ArrayList<>();
        this.estado = EstadoSalida.PROGRAMADA;
        this.listaPasajerosEmitida = false;

        setNumero(numero);
        validarTour(tour);
        this.tour = tour;
        validarFechas(fechaInicio, fechaFin);
        validarCupos(cupoMinimo, cupoMaximo);

        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.cupoMinimo = cupoMinimo;
        this.cupoMaximo = cupoMaximo;

        // tiene que existir una tarifa para la fecha de inicio (si no, lanza la excepción)
        tour.obtenerTarifaVigente(fechaInicio);
    }

    public int getNumero() {
        return numero;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public EstadoSalida getEstado() {
        return estado;
    }

    public int getCupoMinimo() {
        return cupoMinimo;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public Tour getTour() {
        return tour;
    }

    public Guia getGuia() {
        return guia;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public LiquidacionGuia getLiquidacionGuia() {
        return liquidacionGuia;
    }

    public boolean tieneListaEmitida() {
        return listaPasajerosEmitida;
    }

    private void setNumero(int numero) {
        if (numero <= 0) {
            throw new IllegalArgumentException("El número de salida debe ser mayor a cero.");
        }
        this.numero = numero;
    }

    private void validarTour(Tour tour) throws ReglaNegocioException {
        if (tour == null) {
            throw new IllegalArgumentException("La salida debe pertenecer a un tour.");
        }
        if (!tour.estaActivo()) {
            throw new ReglaNegocioException("El tour está dado de baja y no admite nuevas salidas.");
        }
        tour.validarConfiguracion();
    }

    // la fecha de fin tiene que coincidir con la duración del tour
    private void validarFechas(LocalDate inicio, LocalDate fin) throws ReglaNegocioException {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas.");
        }
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
        LocalDate finEsperado = inicio.plusDays(tour.getDuracionDias() - 1);
        if (!fin.equals(finEsperado)) {
            throw new ReglaNegocioException("La fecha de fin no coincide con la duración del tour (" + tour.getDuracionDias() + " días): debería ser " + finEsperado + ".");
        }
    }

    private void validarCupos(int cupoMinimo, int cupoMaximo) throws ReglaNegocioException {
        if (cupoMinimo <= 0 || cupoMaximo < cupoMinimo) {
            throw new IllegalArgumentException("Los cupos ingresados no son válidos.");
        }
        if (cupoMaximo > tour.getCupoMaximo()) {
            throw new ReglaNegocioException("El cupo máximo de la salida no puede superar el del tour (" + tour.getCupoMaximo() + ").");
        }
    }

    // la salida admite cambios mientras no haya comenzado ni esté cancelada
    private boolean admiteCambios() {
        return estado == EstadoSalida.PROGRAMADA
                || estado == EstadoSalida.CONFIRMADA
                || estado == EstadoSalida.COMPLETA;
    }

    public void modificarCupos(int cupoMinimo, int cupoMaximo) throws ReglaNegocioException {
        if (!admiteCambios()) {
            throw new EstadoInvalidoException("No se pueden modificar los cupos de una salida " + estado + ".");
        }
        validarCupos(cupoMinimo, cupoMaximo);
        if (cupoMaximo < calcularOcupacion()) {
            throw new ReglaNegocioException("El nuevo cupo máximo es menor que los lugares ya reservados.");
        }
        if (vehiculo != null && !vehiculo.tieneCapacidad(cupoMaximo)) {
            throw new AsignacionInvalidaException("El vehículo asignado no alcanza para el nuevo cupo máximo.");
        }
        this.cupoMinimo = cupoMinimo;
        this.cupoMaximo = cupoMaximo;
        actualizarEstado();
    }

    // reservas y cupo
    // la salida crea sus reservas, así no existe una reserva sin salida
    public Reserva crearReserva(int numero, LocalDate fecha, Cliente cliente, Vendedor vendedor)
            throws ReglaNegocioException {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de la reserva es obligatoria.");
        }
        if (estado != EstadoSalida.PROGRAMADA && estado != EstadoSalida.CONFIRMADA) {
            throw new EstadoInvalidoException("Solo se puede reservar en salidas programadas o confirmadas.");
        }
        if (fecha.isAfter(fechaInicio.minusDays(DIAS_LIMITE_RESERVA))) {
            throw new ReglaNegocioException("Las reservas se aceptan hasta " + DIAS_LIMITE_RESERVA + " días antes de la salida.");
        }
        Reserva reserva = new Reserva(numero, fecha, cliente, this, vendedor);
        reservas.add(reserva);
        return reserva;
    }

    public List<Reserva> getReservas() {
        return Collections.unmodifiableList(reservas);
    }

    // una reserva ocupa lugares mientras esté pendiente o confirmada, y también cuando ya finalizó
    // (los lugares que ocupó siguen contando para la ocupación de una salida terminada)
    private boolean ocupaLugares(Reserva reserva) {
        return reserva.getEstado() == EstadoReserva.PENDIENTE
                || reserva.getEstado() == EstadoReserva.CONFIRMADA
                || reserva.getEstado() == EstadoReserva.FINALIZADA;
    }

    public int calcularOcupacion() {
        int total = 0;
        for (Reserva reserva : reservas) {
            if (ocupaLugares(reserva)) {
                total += reserva.cantidadPasajeros();
            }
        }
        return total;
    }

    public int cuposDisponibles() {
        return cupoMaximo - calcularOcupacion();
    }

    public boolean verificarCupo(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de lugares debe ser mayor a cero.");
        }
        return calcularOcupacion() + cantidad <= cupoMaximo;
    }

    // lo usa Reserva antes de agregar pasajeros
    void validarCupo(int cantidad) throws ReglaNegocioException {
        if (estado != EstadoSalida.PROGRAMADA && estado != EstadoSalida.CONFIRMADA) {
            throw new EstadoInvalidoException("La salida está " + estado + " y no admite nuevos pasajeros.");
        }
        if (!verificarCupo(cantidad)) {
            throw new CupoInsuficienteException("No hay cupo suficiente: quedan " + cuposDisponibles() + " lugares.");
        }
    }

    // lo usa Reserva cada vez que cambia la cantidad de pasajeros o el estado de una reserva
    void actualizarEstado() {
        if (!admiteCambios()) {
            return;   // en curso, finalizada o cancelada: el estado ya no depende de la ocupación
        }
        int ocupacion = calcularOcupacion();
        if (ocupacion >= cupoMaximo) {
            estado = EstadoSalida.COMPLETA;
        } else if (ocupacion >= cupoMinimo) {
            estado = EstadoSalida.CONFIRMADA;
        } else {
            estado = EstadoSalida.PROGRAMADA;
        }
    }

    // guía y vehículo
    public void asignarGuia(Guia guia) throws AsignacionInvalidaException {
        if (guia == null) {
            throw new IllegalArgumentException("El guía no puede ser nulo.");
        }
        validarAdmiteAsignacion();
        if (!guia.estaHabilitado(tour.getCategoria())) {
            throw new AsignacionInvalidaException("El guía no está habilitado para esta categoría de tour.");
        }
        this.guia = guia;
    }

    public void asignarVehiculo(Vehiculo vehiculo) throws AsignacionInvalidaException {
        if (vehiculo == null) {
            throw new IllegalArgumentException("El vehículo no puede ser nulo.");
        }
        validarAdmiteAsignacion();
        if (!vehiculo.tieneCapacidad(cupoMaximo)) {
            throw new AsignacionInvalidaException("La capacidad del vehículo debe ser igual o mayor al cupo máximo de la salida.");
        }
        this.vehiculo = vehiculo;
    }

    private void validarAdmiteAsignacion() throws AsignacionInvalidaException {
        if (!admiteCambios()) {
            throw new AsignacionInvalidaException("No se puede asignar a una salida " + estado + ".");
        }
    }

    // lista de pasajeros
    // los pasajeros que viajan: reservas confirmadas (o finalizadas), ordenados por apellido y nombre
    private List<Pasajero> pasajerosQueViajan() {
        List<Pasajero> lista = new ArrayList<>();
        for (Reserva reserva : reservas) {
            if (reserva.getEstado() == EstadoReserva.CONFIRMADA || reserva.getEstado() == EstadoReserva.FINALIZADA) {
                lista.addAll(reserva.getPasajeros());
            }
        }
        // Comparator personalizado: primero por apellido y, si empatan, por nombre
        lista.sort((a, b) -> {
            int porApellido = a.getApellido().compareToIgnoreCase(b.getApellido());
            if (porApellido != 0) {
                return porApellido;
            }
            return a.getNombre().compareToIgnoreCase(b.getNombre());
        });
        return lista;
    }

    public List<Pasajero> emitirListaPasajeros() throws ReglaNegocioException {
        if (estado != EstadoSalida.CONFIRMADA && estado != EstadoSalida.COMPLETA) {
            throw new EstadoInvalidoException("Solo se puede emitir la lista de una salida confirmada o completa.");
        }
        List<Pasajero> lista = pasajerosQueViajan();
        if (lista.isEmpty()) {
            throw new ReglaNegocioException("No hay pasajeros confirmados para emitir la lista.");
        }
        listaPasajerosEmitida = true;
        return lista;
    }

    // lo usa el guía: solo se puede consultar si ya fue emitida
    public List<Pasajero> consultarListaPasajeros() throws ReglaNegocioException {
        if (!listaPasajerosEmitida) {
            throw new ReglaNegocioException("La lista de pasajeros todavía no fue emitida.");
        }
        return pasajerosQueViajan();
    }

    // lo usa Reserva cuando cambian sus pasajeros: la lista emitida queda desactualizada
    void invalidarListaPasajeros() {
        this.listaPasajerosEmitida = false;
    }

    // ciclo de vida de la salida
    public void iniciarSalida(LocalDate hoy) throws ReglaNegocioException {
        if (hoy == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        if (estado != EstadoSalida.CONFIRMADA && estado != EstadoSalida.COMPLETA) {
            throw new EstadoInvalidoException("La salida debe estar confirmada o completa para comenzar.");
        }
        if (hoy.isBefore(fechaInicio)) {
            throw new ReglaNegocioException("La salida empieza el "
                    + String.format("%02d/%02d/%d", fechaInicio.getDayOfMonth(), fechaInicio.getMonthValue(), fechaInicio.getYear())
                    + ": no se puede iniciar antes de esa fecha.");
        }
        if (guia == null) {
            throw new ReglaNegocioException("La salida debe tener un guía asignado.");
        }
        if (vehiculo == null) {
            throw new ReglaNegocioException("La salida debe tener un vehículo asignado.");
        }
        if (!listaPasajerosEmitida) {
            throw new ReglaNegocioException("La salida debe tener la lista de pasajeros emitida.");
        }
        estado = EstadoSalida.EN_CURSO;
    }

    // finaliza la salida y las reservas confirmadas (las pendientes ya se anularon por falta de pago)
    public void finalizarSalida() throws ReglaNegocioException {
        if (estado != EstadoSalida.EN_CURSO) {
            throw new EstadoInvalidoException("Solo se puede finalizar una salida que está en curso.");
        }
        for (Reserva reserva : reservas) {
            if (reserva.getEstado() == EstadoReserva.CONFIRMADA) {
                reserva.finalizar();
            }
        }
        estado = EstadoSalida.FINALIZADA;
    }

    // cancelación decidida por la agencia: se cancelan las reservas activas con devolución del 100%
    public void cancelar(LocalDate hoy, String motivo) throws ReglaNegocioException {
        if (hoy == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        if (!admiteCambios()) {
            throw new EstadoInvalidoException("Solo se puede cancelar una salida que todavía no comenzó.");
        }
        estado = EstadoSalida.CANCELADA;
        for (Reserva reserva : new ArrayList<>(reservas)) {
            if (ocupaLugares(reserva)) {
                reserva.cancelar(hoy, motivo, OrigenCancelacion.AGENCIA);
            }
        }
    }

    // a 7 días de la salida sin el cupo mínimo, se cancela. Devuelve true si la canceló.
    public boolean cancelarSiNoAlcanzaMinimo(LocalDate hoy) throws ReglaNegocioException {
        if (hoy == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        if (estado != EstadoSalida.PROGRAMADA) {
            return false;   // confirmada o completa: ya alcanzó el mínimo
        }
        LocalDate fechaLimite = fechaInicio.minusDays(DIAS_LIMITE_CUPO_MINIMO);
        if (!hoy.isBefore(fechaLimite) && calcularOcupacion() < cupoMinimo) {
            cancelar(hoy, "No se alcanzó el cupo mínimo de pasajeros.");
            return true;
        }
        return false;
    }

    // liquidación del guía
    // la liquidación la calcula ServicioLiquidacion; la salida valida y la registra
    public LiquidacionGuia crearLiquidacion(int numero, LocalDate fecha, double honorarios, double comisionAdicionales) throws ReglaNegocioException {
        if (estado != EstadoSalida.FINALIZADA) {
            throw new EstadoInvalidoException("La salida debe estar finalizada.");
        }
        if (liquidacionGuia != null) {
            throw new ReglaNegocioException("La salida ya tiene una liquidación.");
        }
        // el guía existe siempre: no se puede iniciar una salida sin él
        this.liquidacionGuia = new LiquidacionGuia(numero, fecha, honorarios, comisionAdicionales, guia);
        return liquidacionGuia;
    }

    // consultas
    public boolean seSuperponeCon(Salida otra) {
        if (otra == null) {
            throw new IllegalArgumentException("La salida no puede ser nula.");
        }
        return !fechaFin.isBefore(otra.getFechaInicio()) && !fechaInicio.isAfter(otra.getFechaFin());
    }

    // porcentaje de lugares ocupados, con 2 decimales (lo usa el reporte de ocupación)
    public double calcularPorcentajeOcupacion() {
        return Math.round(calcularOcupacion() * 10000.0 / cupoMaximo) / 100.0;
    }

    // dos salidas son la misma si tienen el mismo número
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Salida)) {
            return false;
        }
        Salida otra = (Salida) o;
        return numero == otra.numero;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(numero);
    }

    @Override
    public String toString() {
        return "Salida N° " + numero + " - " + tour.getNombre()
                + " (" + fechaInicio + " a " + fechaFin + ") - " + estado;
    }
}
