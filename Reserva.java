package modelo;
//FIJARSE Y COMPLETAR
import modelo.enums.EstadoReserva;
import modelo.enums.EstadoSalida;
import modelo.enums.MedioPago;
import modelo.enums.OrigenCancelacion;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Reserva {
    private static final double PORCENTAJE_SENIA = 0.30;
    private static final int DIAS_LIMITE_SALDO = 10;
    private static final int DIAS_LIMITE_REEMPLAZO = 3;

    private final int numero;
    private final LocalDate fecha;
    private EstadoReserva estado;
    private double total;

    private final Cliente cliente;
    private final Salida salida;
    private final Vendedor vendedor;
    private final List<Pasajero> pasajeros = new ArrayList<>();
    private final List<Pago> pagos = new ArrayList<>();
    private final List<NotaAjuste> notasAjuste = new ArrayList<>();
    private Cancelacion cancelacion;

    public Reserva(int numero, LocalDate fecha, Cliente cliente, Salida salida,
                   Vendedor vendedor, List<Pasajero> pasajerosIniciales) {
        if (numero <= 0) {
            throw new IllegalArgumentException("El número de reserva debe ser positivo.");
        }
        if (fecha == null || cliente == null || salida == null || vendedor == null) {
            throw new IllegalArgumentException("Fecha, cliente, salida y vendedor son obligatorios.");
        }
        if (pasajerosIniciales == null || pasajerosIniciales.isEmpty()) {
            throw new IllegalArgumentException("La reserva debe incluir al menos un pasajero.");
        }
        EstadoSalida estadoSalida = salida.getEstado();
        if (estadoSalida != EstadoSalida.PROGRAMADA && estadoSalida != EstadoSalida.CONFIRMADA) {
            throw new IllegalStateException("Solo se puede reservar en salidas programadas o confirmadas.");
        }
        if (!salida.verificarCupo(pasajerosIniciales.size())) {
            throw new IllegalStateException("La salida no tiene cupo suficiente para "
                    + pasajerosIniciales.size() + " pasajero(s).");
        }
        this.numero = numero;
        this.fecha = fecha;
        this.cliente = cliente;
        this.salida = salida;
        this.vendedor = vendedor;
        this.estado = EstadoReserva.PENDIENTE;
        this.total = 0;
        for (Pasajero p : pasajerosIniciales) {
            validarPasajero(p, pasajeros);
            pasajeros.add(p);
        }
    }



    public void agregarPasajero(Pasajero pasajero) {
        validarModificable();
        validarPasajero(pasajero, pasajeros);
        if (!salida.verificarCupo(1)) {
            throw new IllegalStateException("La salida no tiene cupo disponible.");
        }
        pasajeros.add(pasajero);
    }

    public void eliminarPasajero(Pasajero pasajero) {
        validarModificable();
        if (!pasajeros.contains(pasajero)) {
            throw new IllegalArgumentException("El pasajero no pertenece a esta reserva.");
        }
        if (pasajeros.size() == 1) {
            throw new IllegalStateException("La reserva debe conservar al menos un pasajero.");
        }
        pasajeros.remove(pasajero);
    }


    public void reemplazarPasajero(Pasajero actual, Pasajero nuevo, LocalDate fechaCambio) {
        validarModificable();
        if (fechaCambio == null) {
            throw new IllegalArgumentException("La fecha del cambio es obligatoria.");
        }
        int indice = pasajeros.indexOf(actual);
        if (indice < 0) {
            throw new IllegalArgumentException("El pasajero a reemplazar no pertenece a esta reserva.");
        }
        if (ChronoUnit.DAYS.between(fechaCambio, salida.getFechaInicio()) < DIAS_LIMITE_REEMPLAZO) {
            throw new IllegalStateException("El reemplazo solo se permite hasta "
                    + DIAS_LIMITE_REEMPLAZO + " días antes de la salida.");
        }
        List<Pasajero> restantes = new ArrayList<>(pasajeros);
        restantes.remove(indice);
        validarPasajero(nuevo, restantes);
        pasajeros.set(indice, nuevo);
    }


    public double calcularTotalConAjustes() {
        double ajustes = 0;
        for (NotaAjuste n : notasAjuste) {
            ajustes += n.getImporteAjuste();
        }
        return total + ajustes;
    }


    public double calcularTotalAbonado() {
        double abonado = 0;
        for (Pago p : pagos) {
            abonado += p.getImporte();
        }
        return abonado;
    }

    public double calcularSaldo() {
        return Math.max(0, calcularTotalConAjustes() - calcularTotalAbonado());
    }


    public void registrarPago(Pago pago) {
        if (pago == null) {
            throw new IllegalArgumentException("El pago es obligatorio.");
        }
        validarModificable();
        if (pago.getImporte() > calcularSaldo() + 0.005) {
            throw new IllegalArgumentException("El importe supera el saldo pendiente ("
                    + calcularSaldo() + ").");
        }
        pagos.add(pago);
        if (estado == EstadoReserva.PENDIENTE && seniaCubierta()) {
            confirmar();
        }
    }


    public Pago crearPago(LocalDate fecha, double importe, MedioPago medio, int cuotas) {
        Pago pago = new Pago(pagos.size() + 1, fecha, importe, medio, cuotas);
        registrarPago(pago);
        return pago;
    }


    public void confirmar() {
        if (estado != EstadoReserva.PENDIENTE) {
            throw new IllegalStateException("Solo se puede confirmar una reserva pendiente.");
        }
        if (!seniaCubierta()) {
            throw new IllegalStateException("Falta abonar la seña del 30 % para confirmar la reserva.");
        }
        estado = EstadoReserva.CONFIRMADA;
    }


    public Cancelacion cancelar(LocalDate fechaCancelacion, String motivo, OrigenCancelacion origen) {
        validarModificable();
        cancelacion = new Cancelacion(fechaCancelacion, motivo, origen, salida.getFechaInicio(), pagos);
        cancelacion.registrarDevoluciones();
        estado = EstadoReserva.ANULADA;
        return cancelacion;
    }


    public boolean verificarPlazoPago(LocalDate hoy) {
        if (hoy == null) {
            throw new IllegalArgumentException("La fecha actual es obligatoria.");
        }
        if (estado != EstadoReserva.PENDIENTE && estado != EstadoReserva.CONFIRMADA) {
            return false;
        }
        boolean plazoVencido = ChronoUnit.DAYS.between(hoy, salida.getFechaInicio()) < DIAS_LIMITE_SALDO;
        if (plazoVencido && calcularSaldo() > 0.005) {
            estado = EstadoReserva.ANULADA;
            return true;
        }
        return false;
    }


    public void finalizar() {
        if (estado != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("Solo se puede finalizar una reserva confirmada.");
        }
        estado = EstadoReserva.FINALIZADA;
    }


    public NotaAjuste crearNotaAjuste(LocalDate fecha, String motivo, double importeAjuste) {
        if (estado == EstadoReserva.ANULADA) {
            throw new IllegalStateException("No se pueden registrar ajustes sobre una reserva anulada.");
        }
        if (pagos.isEmpty()) {
            throw new IllegalStateException("Solo se pueden ajustar reservas con pagos registrados.");
        }
        NotaAjuste nota = new NotaAjuste(notasAjuste.size() + 1, fecha, motivo, importeAjuste);
        nota.registrarAjuste();
        notasAjuste.add(nota);
        return nota;
    }

    public void actualizarTotal(double nuevoTotal) {
        validarModificable();
        if (nuevoTotal < 0) {
            throw new IllegalArgumentException("El total no puede ser negativo.");
        }
        this.total = nuevoTotal;
    }


    private boolean seniaCubierta() {
        return calcularTotalAbonado() + 0.005 >= calcularTotalConAjustes() * PORCENTAJE_SENIA;
    }

    private void validarModificable() {
        if (estado != EstadoReserva.PENDIENTE && estado != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("La reserva está " + estado + " y no admite cambios.");
        }
    }


    private void validarPasajero(Pasajero pasajero, List<Pasajero> existentes) {
        if (pasajero == null) {
            throw new IllegalArgumentException("El pasajero es obligatorio.");
        }
        for (Pasajero e : existentes) {
            if (e.getDocumento().equals(pasajero.getDocumento())) {
                throw new IllegalArgumentException("El pasajero con documento "
                        + pasajero.getDocumento() + " ya está en la reserva.");
            }
        }
        if (!salida.getTour().cumpleCondiciones(pasajero)) {
            throw new IllegalArgumentException("El pasajero " + pasajero.getDocumento()
                    + " no cumple las condiciones del tour.");
        }
    }



    public int getNumero() { return numero; }
    public LocalDate getFecha() { return fecha; }
    public EstadoReserva getEstado() { return estado; }
    public double getTotal() { return total; }
    public Cliente getCliente() { return cliente; }
    public Salida getSalida() { return salida; }
    public Vendedor getVendedor() { return vendedor; }
    public int cantidadPasajeros() { return pasajeros.size(); }
    public List<Pasajero> getPasajeros() { return Collections.unmodifiableList(pasajeros); }
    public List<Pago> getPagos() { return Collections.unmodifiableList(pagos); }
    public List<NotaAjuste> getNotasAjuste() { return Collections.unmodifiableList(notasAjuste); }
    public Cancelacion getCancelacion() { return cancelacion; }
}
