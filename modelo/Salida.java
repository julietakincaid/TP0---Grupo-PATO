package modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import modelo.enums.EstadoReserva;
import modelo.enums.EstadoSalida;
import modelo.enums.OrigenCancelacion;

public class Salida {
    private static final int DIAS_LIMITE_CUPO_MINIMO = 7;

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
    private final List<Reserva> reservas = new ArrayList<>();   // 0..* (composición)
    private LiquidacionGuia liquidacion;

    public Salida(int numero, LocalDate fechaInicio, LocalDate fechaFin, int cupoMinimo, int cupoMaximo, Tour tour) {
        if (tour == null) {
            throw new IllegalArgumentException("La salida debe pertenecer a un tour.");
        }
        setNumero(numero);
        setFechas(fechaInicio, fechaFin);
        setCupos(cupoMinimo, cupoMaximo);
        this.tour = tour;
        this.estado = EstadoSalida.PROGRAMADA;
        this.listaPasajerosEmitida = false;
    }


    public int calcularOcupacion() {
        int ocupados = 0;
        for (Reserva r : reservas) {
            if (r.getEstado() != EstadoReserva.ANULADA) {
                ocupados += r.cantidadPasajeros();
            }
        }
        return ocupados;
    }

    public boolean verificarCupo(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de pasajeros debe ser mayor a cero.");
        }
        return calcularOcupacion() + cantidad <= cupoMaximo;
    }


    void agregarReserva(Reserva reserva) {
        if (reserva == null || reserva.getSalida() != this) {
            throw new IllegalArgumentException("La reserva no corresponde a esta salida.");
        }
        reservas.add(reserva);
        actualizarEstadoPorOcupacion();
    }


    public void actualizarEstadoPorOcupacion() {
        if (estado != EstadoSalida.PROGRAMADA && estado != EstadoSalida.CONFIRMADA
                && estado != EstadoSalida.COMPLETA) {
            return;
        }
        int ocupacion = calcularOcupacion();
        if (ocupacion >= cupoMaximo) {
            estado = EstadoSalida.COMPLETA;
        } else if (estado == EstadoSalida.COMPLETA || ocupacion >= cupoMinimo) {
            estado = EstadoSalida.CONFIRMADA;
        }
    }


    public void confirmar() {
        actualizarEstadoPorOcupacion();
        if (estado == EstadoSalida.PROGRAMADA) {
            throw new IllegalStateException("La salida no alcanzó el cupo mínimo de " + cupoMinimo + ".");
        }
        if (estado != EstadoSalida.CONFIRMADA && estado != EstadoSalida.COMPLETA) {
            throw new IllegalStateException("No se puede confirmar una salida " + estado + ".");
        }
    }



    public void asignarGuia(Guia nuevo) {
        validarNoIniciada();
        if (nuevo == null) {
            throw new IllegalArgumentException("El guía es obligatorio.");
        }
        if (nuevo == guia) {
            return;
        }
        if (!nuevo.estaHabilitado(tour.getCategoria())) {
            throw new IllegalStateException("El guía no está habilitado para tours de categoría "
                    + tour.getCategoria() + ".");
        }
        if (!nuevo.estaDisponible(fechaInicio, fechaFin)) {
            throw new IllegalStateException("El guía tiene otra salida superpuesta en esas fechas.");
        }
        if (guia != null) {
            guia.quitarSalida(this);
        }
        guia = nuevo;
        guia.registrarSalida(this);
    }

    public void asignarVehiculo(Vehiculo nuevo) {
        validarNoIniciada();
        if (nuevo == null) {
            throw new IllegalArgumentException("El vehículo es obligatorio.");
        }
        if (nuevo == vehiculo) {
            return;
        }
        if (!nuevo.tieneCapacidad(cupoMaximo)) {
            throw new IllegalStateException("La capacidad del vehículo (" + nuevo.getCapacidad()
                    + ") es menor que el cupo de la salida (" + cupoMaximo + ").");
        }
        if (!nuevo.estaDisponible(fechaInicio, fechaFin)) {
            throw new IllegalStateException("El vehículo tiene otra salida superpuesta en esas fechas.");
        }
        if (vehiculo != null) {
            vehiculo.quitarSalida(this);
        }
        vehiculo = nuevo;
        vehiculo.registrarSalida(this);
    }


    public List<Pasajero> emitirListaPasajeros() {
        if (estado == EstadoSalida.CANCELADA || estado == EstadoSalida.FINALIZADA) {
            throw new IllegalStateException("No se puede emitir la lista de una salida " + estado + ".");
        }
        List<Pasajero> lista = new ArrayList<>();
        for (Reserva r : reservas) {
            if (r.getEstado() != EstadoReserva.ANULADA) {
                lista.addAll(r.getPasajeros());
            }
        }
        lista.sort(Comparator.comparing(Pasajero::getApellido).thenComparing(Pasajero::getNombre));
        listaPasajerosEmitida = true;
        return Collections.unmodifiableList(lista);
    }


    public void iniciar() {
        actualizarEstadoPorOcupacion();
        if (estado != EstadoSalida.CONFIRMADA && estado != EstadoSalida.COMPLETA) {
            throw new IllegalStateException("Solo se puede iniciar una salida confirmada o completa.");
        }
        if (guia == null) {
            throw new IllegalStateException("No se puede iniciar la salida sin guía asignado.");
        }
        if (!listaPasajerosEmitida) {
            throw new IllegalStateException("No se puede iniciar la salida sin emitir la lista de pasajeros.");
        }
        estado = EstadoSalida.EN_CURSO;
    }

    public void finalizar() {
        if (estado != EstadoSalida.EN_CURSO) {
            throw new IllegalStateException("Solo se puede finalizar una salida en curso.");
        }
        estado = EstadoSalida.FINALIZADA;
        for (Reserva r : reservas) {
            if (r.getEstado() == EstadoReserva.CONFIRMADA) {
                r.finalizar();
            }
        }
    }


    public void cancelar(LocalDate fecha, String motivo) {
        actualizarEstadoPorOcupacion();
        if (estado == EstadoSalida.EN_CURSO || estado == EstadoSalida.FINALIZADA
                || estado == EstadoSalida.CANCELADA) {
            throw new IllegalStateException("No se puede cancelar una salida " + estado + ".");
        }
        estado = EstadoSalida.CANCELADA;
        for (Reserva r : new ArrayList<>(reservas)) {
            if (r.getEstado() == EstadoReserva.PENDIENTE || r.getEstado() == EstadoReserva.CONFIRMADA) {
                r.cancelar(fecha, motivo, OrigenCancelacion.AGENCIA);
            }
        }
    }


    public boolean cancelarSiNoAlcanzaMinimo(LocalDate hoy) {
        if (hoy == null) {
            throw new IllegalArgumentException("La fecha actual es obligatoria.");
        }
        actualizarEstadoPorOcupacion();
        if (estado != EstadoSalida.PROGRAMADA) {
            return false;
        }
        if (ChronoUnit.DAYS.between(hoy, fechaInicio) <= DIAS_LIMITE_CUPO_MINIMO) {
            cancelar(hoy, "La salida no alcanzó el cupo mínimo.");
            return true;
        }
        return false;
    }




    public double calcularVentasAdicionales() {
        double total = 0;
        for (Reserva r : reservas) {
            if (r.getEstado() != EstadoReserva.ANULADA) {
                for (Pasajero p : r.getPasajeros()) {
                    total += p.calcularTotalServicios();
                }
            }
        }
        return total;
    }

    public void registrarLiquidacion(LiquidacionGuia nueva) {
        if (estado != EstadoSalida.FINALIZADA) {
            throw new IllegalStateException("Solo se liquidan salidas finalizadas.");
        }
        if (nueva == null || guia == null || nueva.getGuia() != guia) {
            throw new IllegalArgumentException("La liquidación debe corresponder al guía de la salida.");
        }
        if (liquidacion != null) {
            throw new IllegalStateException("La salida ya fue liquidada.");
        }
        liquidacion = nueva;
    }

    public boolean seSuperponeCon(LocalDate inicio, LocalDate fin) {
        return !fechaInicio.isAfter(fin) && !inicio.isAfter(fechaFin);
    }

    private void validarNoIniciada() {
        if (estado != EstadoSalida.PROGRAMADA && estado != EstadoSalida.CONFIRMADA
                && estado != EstadoSalida.COMPLETA) {
            throw new IllegalStateException("No se pueden cambiar asignaciones de una salida " + estado + ".");
        }
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
        actualizarEstadoPorOcupacion();
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

    public boolean isListaPasajerosEmitida() {
        return listaPasajerosEmitida;
    }

    public LiquidacionGuia getLiquidacion() {
        return liquidacion;
    }

    public List<Reserva> getReservas() {
        return Collections.unmodifiableList(reservas);
    }

    private void setNumero(int numero) {
        if (numero <= 0) {
            throw new IllegalArgumentException("El número de salida debe ser mayor a cero.");
        }
        this.numero = numero;
    }

    public void setFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas son obligatorias.");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public void setCupos(int cupoMinimo, int cupoMaximo) {
        if (cupoMinimo <= 0 || cupoMaximo < cupoMinimo) {
            throw new IllegalArgumentException("Los cupos ingresados no son válidos.");
        }
        this.cupoMinimo = cupoMinimo;
        this.cupoMaximo = cupoMaximo;
    }

    @Override
    public String toString() {
        return "Salida #" + numero + " (" + fechaInicio + " a " + fechaFin + ") " + estado;
    }
}
