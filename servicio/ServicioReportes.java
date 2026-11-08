package servicio;

import modelo.Cancelacion;
import modelo.Devolucion;
import modelo.Pago;
import modelo.Reserva;
import modelo.Salida;
import modelo.Tour;
import modelo.Vendedor;
import modelo.enums.EstadoReserva;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServicioReportes {

    // Ocupación por salida
    // las salidas que empiezan dentro del período, de la más próxima a la más lejana.
    public List<Salida> reporteOcupacion(List<Salida> salidas, LocalDate desde, LocalDate hasta) {
        validar(salidas, desde, hasta);

        List<Salida> resultado = new ArrayList<>();
        for (Salida salida : salidas) {
            if (estaEnPeriodo(salida.getFechaInicio(), desde, hasta)) {
                resultado.add(salida);
            }
        }
        // Comparator personalizado: por fecha de inicio
        resultado.sort((a, b) -> a.getFechaInicio().compareTo(b.getFechaInicio()));
        return resultado;
    }

    // Ventas por vendedor
    public int contarVentas(List<Salida> salidas, Vendedor vendedor, LocalDate desde, LocalDate hasta) {
        validar(salidas, desde, hasta);
        validarNoNulo(vendedor, "El vendedor es obligatorio.");

        int cantidad = 0;
        for (Reserva reserva : reunirReservas(salidas)) {
            if (esVenta(reserva, vendedor, desde, hasta)) {
                cantidad++;
            }
        }
        return cantidad;
    }

    public double calcularTotalVendido(List<Salida> salidas, Vendedor vendedor, LocalDate desde, LocalDate hasta) {
        validar(salidas, desde, hasta);
        validarNoNulo(vendedor, "El vendedor es obligatorio.");

        double total = 0;
        for (Reserva reserva : reunirReservas(salidas)) {
            if (esVenta(reserva, vendedor, desde, hasta)) {
                total += reserva.getTotal();
            }
        }
        return redondear(total);
    }

    // Ingresos por tour
    public double calcularIngresosCobrados(List<Salida> salidas, Tour tour, LocalDate desde, LocalDate hasta) {
        validar(salidas, desde, hasta);
        validarNoNulo(tour, "El tour es obligatorio.");

        double total = 0;
        for (Reserva reserva : reunirReservas(salidas)) {
            if (reserva.getSalida().getTour().equals(tour)) {
                for (Pago pago : reserva.getPagos()) {
                    if (estaEnPeriodo(pago.getFecha(), desde, hasta)) {
                        total += pago.calcularTotalPago();
                    }
                }
            }
        }
        return redondear(total);
    }

    // lo devuelto en el período, según la fecha de cada devolución
    public double calcularDevoluciones(List<Salida> salidas, Tour tour, LocalDate desde, LocalDate hasta) {
        validar(salidas, desde, hasta);
        validarNoNulo(tour, "El tour es obligatorio.");

        double total = 0;
        for (Reserva reserva : reunirReservas(salidas)) {
            if (reserva.getSalida().getTour().equals(tour) && reserva.getCancelacion() != null) {
                for (Devolucion devolucion : reserva.getCancelacion().getDevoluciones()) {
                    if (estaEnPeriodo(devolucion.getFecha(), desde, hasta)) {
                        total += devolucion.getImporte();
                    }
                }
            }
        }
        return redondear(total);
    }

    public double calcularIngresosNetos(List<Salida> salidas, Tour tour, LocalDate desde, LocalDate hasta) {
        return redondear(calcularIngresosCobrados(salidas, tour, desde, hasta)
                - calcularDevoluciones(salidas, tour, desde, hasta));
    }

    // Cancelaciones
    public List<Reserva> reporteCancelaciones(List<Salida> salidas, LocalDate desde, LocalDate hasta) {
        validar(salidas, desde, hasta);

        List<Reserva> resultado = new ArrayList<>();
        for (Reserva reserva : reunirReservas(salidas)) {
            Cancelacion cancelacion = reserva.getCancelacion();
            if (cancelacion != null && estaEnPeriodo(cancelacion.getFecha(), desde, hasta)) {
                resultado.add(reserva);
            }
        }
        // Comparator personalizado: por fecha de cancelación
        resultado.sort((a, b) -> a.getCancelacion().getFecha().compareTo(b.getCancelacion().getFecha()));
        return resultado;
    }

    // total devuelto en las cancelaciones de una lista de reservas (para el pie del reporte)
    public double calcularTotalDevuelto(List<Reserva> reservasCanceladas) {
        validarNoNulo(reservasCanceladas, "La lista de reservas no puede ser nula.");

        double total = 0;
        for (Reserva reserva : reservasCanceladas) {
            if (reserva.getCancelacion() != null) {
                total += reserva.getCancelacion().calcularTotalDevuelto();
            }
        }
        return redondear(total);
    }

    // todas las reservas de todas las salidas
    private List<Reserva> reunirReservas(List<Salida> salidas) {
        List<Reserva> reservas = new ArrayList<>();
        for (Salida salida : salidas) {
            reservas.addAll(salida.getReservas());
        }
        return reservas;
    }

    private boolean esVenta(Reserva reserva, Vendedor vendedor, LocalDate desde, LocalDate hasta) {
        boolean vendida = reserva.getEstado() == EstadoReserva.CONFIRMADA
                || reserva.getEstado() == EstadoReserva.FINALIZADA;
        return vendida
                && reserva.getVendedor().equals(vendedor)
                && estaEnPeriodo(reserva.getFecha(), desde, hasta);
    }

    private boolean estaEnPeriodo(LocalDate fecha, LocalDate desde, LocalDate hasta) {
        return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private void validarNoNulo(Object objeto, String mensaje) {
        if (objeto == null) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private void validar(List<Salida> salidas, LocalDate desde, LocalDate hasta) {
        validarNoNulo(salidas, "La lista de salidas no puede ser nula.");
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("El período es obligatorio.");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta.");
        }
    }
}