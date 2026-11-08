package modelo;

import excepcion.EstadoInvalidoException;
import excepcion.ReglaNegocioException;
import modelo.enums.EstadoReserva;
import modelo.enums.EstadoSalida;
import modelo.enums.MedioPago;
import modelo.enums.OrigenCancelacion;
import modelo.enums.TipoPasajero;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.Serializable;

public class Reserva implements Serializable {
    private static final double PORCENTAJE_SENIA = 0.30;
    private static final int DIAS_LIMITE_SALDO = 10;
    private static final int DIAS_LIMITE_REEMPLAZO = 3;
    private static final double TOLERANCIA = 0.005;

    private int numero;
    private LocalDate fecha;
    private EstadoReserva estado;
    private double total;
    private double precioBase;

    private Cliente cliente;
    private Salida salida;
    private Vendedor vendedor;
    private final List<Pasajero> pasajeros;
    private final List<Pago> pagos;
    private final List<NotaAjuste> notasAjuste;
    private final PoliticaCancelacion politicaCancelacion;
    private Cancelacion cancelacion;
    private Promocion promocionAplicada;

    Reserva(int numero, LocalDate fecha, Cliente cliente, Salida salida, Vendedor vendedor) throws ReglaNegocioException{
        this.pasajeros = new ArrayList<>();
        this.pagos = new ArrayList<>();
        this.notasAjuste = new ArrayList<>();
        this.politicaCancelacion = new PoliticaCancelacion();

        setNumero(numero);
        setFecha(fecha);
        setCliente(cliente);
        setSalida(salida);
        setVendedor(vendedor);

        this.precioBase = salida.getTour().obtenerTarifaVigente(salida.getFechaInicio()).getPrecioBase();
        this.estado = EstadoReserva.PENDIENTE;
        this.total = 0;
    }

    // gestión de pasajeros
    public void agregarPasajero(String nombre, String apellido, String documento, LocalDate fechaNacimiento, TipoPasajero tipo, String contactoEmergencia, String restriccionesSalud, String restriccionesAlimentarias) throws ReglaNegocioException {
        validarModificable();
        Pasajero pasajero = new Pasajero(nombre, apellido, documento, fechaNacimiento, tipo, contactoEmergencia, restriccionesSalud, restriccionesAlimentarias);   // valida los datos
        validarPasajero(pasajero, null);
        salida.validarCupo(1);

        pasajeros.add(pasajero);
        // los servicios obligatorios del tour (por ejemplo, el seguro en aventura) se contratan solos
        for (ServicioAdicional obligatorio : salida.getTour().getServiciosObligatorios()) {
            pasajero.agregarServicio(obligatorio);
        }
        salida.actualizarEstado();
        salida.invalidarListaPasajeros();
    }

    public void eliminarPasajero(Pasajero pasajero) throws ReglaNegocioException {
        validarModificable();
        validarSinPagos("quitar pasajeros");
        validarPertenece(pasajero);
        if (pasajeros.size() == 1) {
            throw new ReglaNegocioException("La reserva debe conservar al menos un pasajero. Si no viaja nadie, cancélela.");
        }
        pasajeros.remove(pasajero);
        salida.actualizarEstado();
        salida.invalidarListaPasajeros();
    }

    public void reemplazarPasajero(Pasajero actual, String nombre, String apellido, String documento, LocalDate fechaNacimiento, TipoPasajero tipo, String contactoEmergencia, String restriccionesSalud, String restriccionesAlimentarias, LocalDate fechaCambio) throws ReglaNegocioException {
        validarModificable();
        validarPertenece(actual);
        if (fechaCambio == null) {
            throw new IllegalArgumentException("La fecha del cambio es obligatoria.");
        }
        if (fechaCambio.isAfter(salida.getFechaInicio().minusDays(DIAS_LIMITE_REEMPLAZO))) {
            throw new ReglaNegocioException("Se superó el plazo para reemplazar pasajeros (" + DIAS_LIMITE_REEMPLAZO + " días antes de la salida).");
        }

        Pasajero nuevo = new Pasajero(nombre, apellido, documento, fechaNacimiento, tipo, contactoEmergencia, restriccionesSalud, restriccionesAlimentarias);
        validarPasajero(nuevo, actual);

        nuevo.heredarServiciosDe(actual);   // conserva los servicios contratados (y su precio) sin cargo
        pasajeros.set(pasajeros.indexOf(actual), nuevo);
        salida.invalidarListaPasajeros();
    }

    // servicios adicionales
    public void contratarServicio(Pasajero pasajero, ServicioAdicional servicio) throws ReglaNegocioException {
        validarModificable();
        validarPertenece(pasajero);
        if (servicio == null) {
            throw new IllegalArgumentException("El servicio no puede ser nulo.");
        }
        if (!salida.getTour().ofreceServicio(servicio)) {
            throw new ReglaNegocioException("El tour no ofrece ese servicio adicional.");
        }
        pasajero.agregarServicio(servicio);
    }

    public void quitarServicio(Pasajero pasajero, ServicioAdicional servicio) throws ReglaNegocioException {
        validarModificable();
        validarSinPagos("quitar servicios");
        validarPertenece(pasajero);
        if (servicio == null) {
            throw new IllegalArgumentException("El servicio no puede ser nulo.");
        }
        if (salida.getTour().getServiciosObligatorios().contains(servicio)) {
            throw new ReglaNegocioException("El servicio es obligatorio en este tour y no se puede quitar.");
        }
        pasajero.quitarServicio(servicio);
    }

    // una reserva admite pagos si todavía está abierta (pendiente o confirmada) y le queda saldo por pagar
    public boolean admitePagos() {
        boolean abierta = estado == EstadoReserva.PENDIENTE || estado == EstadoReserva.CONFIRMADA;
        return abierta && calcularSaldo() > TOLERANCIA;
    }

    // saldos y pagos
    public double calcularTotalAbonado() {
        double abonado = 0;
        for (Pago pago : pagos) {
            abonado += pago.getImporte();
        }
        return abonado;
    }

    public double calcularSaldo() {
        return Math.max(0, total - calcularTotalAbonado());
    }

    // el recargo de tarjeta se cobra aparte: no reduce el saldo, pero sí se devuelve al cancelar
    public double calcularTotalRecargos() {
        double recargos = 0;
        for (Pago pago : pagos) {
            recargos += pago.calcularRecargo();
        }
        return redondear(recargos);
    }

    public Pago crearPago(LocalDate fecha, double importe, MedioPago medio, int cuotas)
            throws ReglaNegocioException {
        validarModificable();
        Pago pago = new Pago(pagos.size() + 1, fecha, importe, medio, cuotas);   // valida los datos

        if (total <= 0) {
            throw new ReglaNegocioException("Calcule el total de la reserva antes de registrar pagos.");
        }
        if (fecha.isBefore(this.fecha)) {
            throw new ReglaNegocioException("El pago no puede ser anterior a la fecha de la reserva.");
        }
        if (fecha.isAfter(fechaLimiteSaldo())) {
            throw new ReglaNegocioException("Venció el plazo para pagar la reserva (hasta " + DIAS_LIMITE_SALDO + " días antes de la salida).");
        }
        if (importe > calcularSaldo() + TOLERANCIA) {
            throw new ReglaNegocioException("El importe supera el saldo pendiente.");
        }
        validarServiciosObligatorios();

        pagos.add(pago);
        if (estado == EstadoReserva.PENDIENTE && seniaCubierta()) {
            confirmar();   // al cubrir la seña del 30% la reserva se confirma sola
        }
        return pago;
    }

    // gestión de estados
    public void confirmar() throws ReglaNegocioException {
        if (estado != EstadoReserva.PENDIENTE) {
            throw new EstadoInvalidoException("Solo se puede confirmar una reserva pendiente.");
        }
        if (pasajeros.isEmpty()) {
            throw new ReglaNegocioException("La reserva debe tener al menos un pasajero.");
        }
        if (total <= 0) {
            throw new ReglaNegocioException("Calcule el total de la reserva antes de confirmarla.");
        }
        validarServiciosObligatorios();
        if (!seniaCubierta()) {
            throw new ReglaNegocioException("Falta abonar la seña del 30% para confirmar la reserva.");
        }
        estado = EstadoReserva.CONFIRMADA;
    }

    public Cancelacion cancelar(LocalDate hoy, String motivo, OrigenCancelacion origen) throws ReglaNegocioException {
        validarModificable();
        if (hoy == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        if (salidaComenzada()) {
            throw new EstadoInvalidoException("No se puede cancelar una reserva de una salida que ya comenzó.");
        }

        double porcentaje = politicaCancelacion.calcularPorcentaje(hoy, salida.getFechaInicio(), origen);

        Cancelacion nueva = new Cancelacion(numero, hoy, motivo, origen);   // valida los datos
        int numeroDevolucion = 1;
        for (Pago pago : pagos) {
            double importe = redondear(pago.calcularTotalPago() * porcentaje);
            if (importe > 0) {
                nueva.generarDevolucion(numeroDevolucion, hoy, importe, pago);
                numeroDevolucion++;
            }
        }

        cancelacion = nueva;
        estado = EstadoReserva.ANULADA;
        salida.actualizarEstado();           // los lugares vuelven al cupo
        salida.invalidarListaPasajeros();
        return cancelacion;
    }

    public void finalizar() throws ReglaNegocioException {
        if (estado != EstadoReserva.CONFIRMADA) {
            throw new EstadoInvalidoException("Solo se puede finalizar una reserva confirmada.");
        }
        estado = EstadoReserva.FINALIZADA;
    }

    // si pasó el plazo del saldo y queda deuda, la reserva se anula. Devuelve true si la anuló.
    public boolean verificarPlazoPago(LocalDate hoy) throws ReglaNegocioException {
        if (hoy == null) {
            throw new IllegalArgumentException("La fecha actual es obligatoria.");
        }
        if ((estado != EstadoReserva.PENDIENTE && estado != EstadoReserva.CONFIRMADA) || salidaComenzada()) {
            return false;
        }
        if (hoy.isAfter(fechaLimiteSaldo()) && calcularSaldo() > TOLERANCIA) {
            cancelar(hoy, "Falta de pago del saldo dentro del plazo.", OrigenCancelacion.FALTA_PAGO);
            return true;
        }
        return false;
    }

    // notas de ajuste
    // se admiten también en reservas finalizadas: son las correcciones posteriores al viaje
    public NotaAjuste crearNotaAjuste(LocalDate fecha, String motivo, double importeAjuste) throws ReglaNegocioException {
        if (estado == EstadoReserva.ANULADA) {
            throw new EstadoInvalidoException("No se pueden registrar ajustes en una reserva anulada.");
        }
        if (pagos.isEmpty()) {
            throw new ReglaNegocioException("Solo se pueden ajustar reservas con pagos registrados.");
        }
        NotaAjuste nota = new NotaAjuste(notasAjuste.size() + 1, fecha, motivo, importeAjuste);   // valida los datos
        double nuevoTotal = total + importeAjuste;
        if (nuevoTotal < 0) {
            throw new ReglaNegocioException("El ajuste dejaría el total de la reserva en negativo.");
        }
        notasAjuste.add(nota);
        total = nuevoTotal;
        return nota;
    }

    // precio (lo calcula CalculadoraPrecioReserva)
    public void actualizarTotal(double nuevoTotal) throws ReglaNegocioException {
        validarModificable();
        if (!Double.isFinite(nuevoTotal) || nuevoTotal < 0) {
            throw new IllegalArgumentException("El total no puede ser negativo ni inválido.");
        }
        this.total = nuevoTotal;
    }

    public void asignarPromocion(Promocion promocion) throws ReglaNegocioException {
        validarModificable();
        this.promocionAplicada = promocion;
    }

    // validaciones privadas
    private LocalDate fechaLimiteSaldo() {
        return salida.getFechaInicio().minusDays(DIAS_LIMITE_SALDO);
    }

    private boolean salidaComenzada() {
        return salida.getEstado() == EstadoSalida.EN_CURSO || salida.getEstado() == EstadoSalida.FINALIZADA;
    }

    private boolean seniaCubierta() {
        return calcularTotalAbonado() + TOLERANCIA >= total * PORCENTAJE_SENIA;
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private void validarModificable() throws EstadoInvalidoException {
        if (estado != EstadoReserva.PENDIENTE && estado != EstadoReserva.CONFIRMADA) {
            throw new EstadoInvalidoException("La reserva está " + estado + " y no admite cambios.");
        }
    }

    // con pagos registrados no se quitan cosas: se corrige con una nota de ajuste
    private void validarSinPagos(String accion) throws ReglaNegocioException {
        if (!pagos.isEmpty()) {
            throw new ReglaNegocioException("No se puede " + accion + " en una reserva con pagos: registre una nota de ajuste.");
        }
    }

    private void validarPertenece(Pasajero pasajero) {
        if (pasajero == null || !pasajeros.contains(pasajero)) {
            throw new IllegalArgumentException("El pasajero no pertenece a esta reserva.");
        }
    }

    // "ignorar" es el pasajero que se está reemplazando (no cuenta como duplicado)
    private void validarPasajero(Pasajero pasajero, Pasajero ignorar) throws ReglaNegocioException {
        for (Pasajero existente : pasajeros) {
            if (existente != ignorar && existente.getDocumento().equals(pasajero.getDocumento())) {
                throw new ReglaNegocioException("El pasajero ya está registrado en la reserva.");
            }
        }
        // una persona no puede viajar dos veces en la misma salida
        for (Reserva otra : salida.getReservas()) {
            boolean activa = otra.estado == EstadoReserva.PENDIENTE || otra.estado == EstadoReserva.CONFIRMADA;
            if (otra != this && activa && otra.tienePasajero(pasajero.getDocumento())) {
                throw new ReglaNegocioException("El pasajero ya está registrado en otra reserva de esta salida.");
            }
        }
        if (!pasajero.tipoCoherenteConEdad(salida.getFechaInicio())) {
            throw new ReglaNegocioException("El tipo de pasajero no corresponde con su edad.");
        }
        if (!salida.getTour().cumpleCondiciones(pasajero, salida.getFechaInicio())) {
            throw new ReglaNegocioException("El pasajero no cumple las condiciones del tour.");
        }
    }

    private boolean tienePasajero(String documento) {
        for (Pasajero pasajero : pasajeros) {
            if (pasajero.getDocumento().equals(documento)) {
                return true;
            }
        }
        return false;
    }

    private void validarServiciosObligatorios() throws ReglaNegocioException {
        for (Pasajero pasajero : pasajeros) {
            for (ServicioAdicional obligatorio : salida.getTour().getServiciosObligatorios()) {
                if (!pasajero.tieneServicio(obligatorio)) {
                    throw new ReglaNegocioException("Falta contratar un servicio obligatorio ("
                            + obligatorio.getNombre() + ") para " + pasajero + ".");
                }
            }
        }
    }

    public int getNumero() {
        return numero;
    }
    public LocalDate getFecha() {
        return fecha;
    }
    public EstadoReserva getEstado() {
        return estado;
    }
    public double getTotal() {
        return total;
    }
    public double getPrecioBase() {return precioBase;}
    public Cliente getCliente() {
        return cliente;
    }
    public Salida getSalida() {
        return salida;
    }
    public Vendedor getVendedor() {
        return vendedor;
    }
    public Promocion getPromocionAplicada() {
        return promocionAplicada;
    }
    public int cantidadPasajeros() {
        return pasajeros.size();
    }
    public List<Pasajero> getPasajeros() {
        return Collections.unmodifiableList(pasajeros);
    }
    public List<Pago> getPagos() {
        return Collections.unmodifiableList(pagos);
    }
    public List<NotaAjuste> getNotasAjuste() {
        return Collections.unmodifiableList(notasAjuste);
    }
    public Cancelacion getCancelacion() {
        return cancelacion;
    }

    private void setNumero(int numero) {
        if (numero <= 0) {
            throw new IllegalArgumentException("El numero de reserva debe ser positivo.");
        }
        this.numero = numero;
    }

    private void setFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de reserva es obligatoria.");
        }
        this.fecha = fecha;
    }

    private void setCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente es obligatorio.");
        }
        this.cliente = cliente;
    }

    private void setSalida(Salida salida) {
        if (salida == null) {
            throw new IllegalArgumentException("La salida es obligatoria.");
        }
        this.salida = salida;
    }

    private void setVendedor(Vendedor vendedor) {
        if (vendedor == null) {
            throw new IllegalArgumentException("El vendedor es obligatorio.");
        }
        this.vendedor = vendedor;
    }

    // dos reservas son la misma si tienen el mismo número
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reserva)) {
            return false;
        }
        Reserva otra = (Reserva) o;
        return numero == otra.numero;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(numero);
    }

    @Override
    public String toString() {
        return "Reserva N° " + numero + " (" + estado + ") - " + cliente;
    }
}
