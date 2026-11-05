package modelo;

import modelo.enums.MedioPago;
import modelo.enums.OrigenCancelacion;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Cancelacion {
    private static final double PCT_30_DIAS_O_MAS = 0.90;
    private static final double PCT_15_A_29_DIAS = 0.50;
    private static final double PCT_7_A_14_DIAS = 0.20;
    private static final double PCT_MENOS_7_DIAS = 0.0;
    private static final double PCT_AGENCIA = 1.0;

    private final LocalDate fecha;
    private final String motivo;
    private final OrigenCancelacion origen;
    private final LocalDate fechaSalida;
    private final List<Pago> pagosRealizados;
    private final List<Devolucion> devoluciones;
    private final double importeDevuelto;

    public Cancelacion(LocalDate fecha, String motivo, OrigenCancelacion origen,
                       LocalDate fechaSalida, List<Pago> pagosRealizados) {
        if (fecha == null || origen == null || fechaSalida == null || pagosRealizados == null) {
            throw new IllegalArgumentException("Fecha, origen, fecha de salida y pagos son obligatorios.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de la cancelación es obligatorio.");
        }
        this.fecha = fecha;
        this.motivo = motivo;
        this.origen = origen;
        this.fechaSalida = fechaSalida;
        this.pagosRealizados = List.copyOf(pagosRealizados);

        List<Devolucion> generadas = new ArrayList<>();
        for (Map.Entry<MedioPago, Double> e : montosPorMedio().entrySet()) {
            if (e.getValue() > 0) {
                generadas.add(new Devolucion(fecha, e.getValue(), e.getKey()));
            }
        }
        this.devoluciones = generadas;
        this.importeDevuelto = calcularDevolucion();
    }


    public double calcularDevolucion() {
        double total = 0;
        for (double monto : montosPorMedio().values()) {
            total += monto;
        }
        return redondear(total);
    }


    public double porcentajeDevolucion() {
        if (origen == OrigenCancelacion.AGENCIA) {
            return PCT_AGENCIA;
        }
        long dias = ChronoUnit.DAYS.between(fecha, fechaSalida);
        if (dias >= 30) return PCT_30_DIAS_O_MAS;
        if (dias >= 15) return PCT_15_A_29_DIAS;
        if (dias >= 7) return PCT_7_A_14_DIAS;
        return PCT_MENOS_7_DIAS;
    }


    public void registrarDevoluciones() {
        for (Devolucion d : devoluciones) {
            d.registrarDevolucion();
        }
    }

    private Map<MedioPago, Double> montosPorMedio() {
        Map<MedioPago, Double> montos = new EnumMap<>(MedioPago.class);
        for (Pago p : pagosRealizados) {
            montos.merge(p.getMedioPago(), p.calcularImporteFinal(), Double::sum);
        }
        double pct = porcentajeDevolucion();
        montos.replaceAll((medio, abonado) -> redondear(abonado * pct));
        return montos;
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    public LocalDate getFecha() { return fecha; }
    public String getMotivo() { return motivo; }
    public OrigenCancelacion getOrigen() { return origen; }
    public double getImporteDevuelto() { return importeDevuelto; }
    public List<Devolucion> getDevoluciones() { return Collections.unmodifiableList(devoluciones); }
}
