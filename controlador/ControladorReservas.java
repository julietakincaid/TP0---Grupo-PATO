package controlador;

import excepcion.ReglaNegocioException;
import modelo.Cancelacion;
import modelo.Cliente;
import modelo.Pago;
import modelo.Pasajero;
import modelo.Personal;
import modelo.Reserva;
import modelo.Salida;
import modelo.ServicioAdicional;
import modelo.ServicioContratado;
import modelo.ServicioTour;
import modelo.Vendedor;
import modelo.enums.MedioPago;
import modelo.enums.OrigenCancelacion;
import modelo.enums.TipoPasajero;
import servicio.Agencia;
import servicio.CalculadoraPrecioReserva;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

// Casos de uso del vendedor: CU14 (registrar cliente), CU15 (crear reserva), CU16 (registrar pago),CU17 (modificar reserva), CU18 (cancelar reserva) y CU19 (nota de ajuste).
public class ControladorReservas {
    private final Agencia agencia;
    private final CalculadoraPrecioReserva calculadora;
    private final Pantalla pantalla;

    public ControladorReservas(Agencia agencia, CalculadoraPrecioReserva calculadora, Pantalla pantalla) {
        if (agencia == null || calculadora == null || pantalla == null) {
            throw new IllegalArgumentException("La agencia, la calculadora y la pantalla son obligatorias.");
        }
        this.agencia = agencia;
        this.calculadora = calculadora;
        this.pantalla = pantalla;
    }

    public List<Salida> getSalidas() {
        return agencia.getSalidas();
    }

    public List<TipoPasajero> getTiposDePasajero() {
        return List.of(TipoPasajero.values());
    }

    public List<MedioPago> getMediosDePago() {
        return List.of(MedioPago.values());
    }

    // solo los orígenes que puede elegir el vendedor: la falta de pago la registra el sistema
    public List<OrigenCancelacion> getOrigenesDeCancelacion() {
        return List.of(OrigenCancelacion.CLIENTE, OrigenCancelacion.AGENCIA);
    }

    // todos los clientes registrados, o null (mostrando el error) si todavía no hay ninguno
    public List<Cliente> getClientes() {
        List<Cliente> clientes = agencia.getClientes();
        if (clientes.isEmpty()) {
            pantalla.mostrarError("No hay clientes registrados.");
            return null;
        }
        return clientes;
    }

    // las reservas que todavía admiten pagos, o null (mostrando el error) si no hay ninguna
    public List<Reserva> getReservasConSaldo() {
        List<Reserva> conSaldo = new ArrayList<>();
        for (Salida salida : agencia.getSalidas()) {
            for (Reserva reserva : salida.getReservas()) {
                if (reserva.admitePagos()) {
                    conSaldo.add(reserva);
                }
            }
        }
        if (conSaldo.isEmpty()) {
            pantalla.mostrarError("No hay reservas con saldo pendiente.");
            return null;
        }
        return conSaldo;
    }

    // el cliente con ese documento, o null (mostrando el error) si no existe
    public Cliente buscarCliente(String documento) {
        try {
            return agencia.buscarCliente(documento);
        } catch (ReglaNegocioException e) {
            pantalla.mostrarError(e.getMessage());
            return null;
        }
    }

    // la reserva con ese número, o null (mostrando el error) si no existe
    public Reserva buscarReserva(int numeroReserva) {
        try {
            return agencia.buscarReserva(numeroReserva);
        } catch (ReglaNegocioException e) {
            pantalla.mostrarError(e.getMessage());
            return null;
        }
    }

    // los pasajeros de la reserva, o null (mostrando el error) si todavía no tiene ninguno
    public List<Pasajero> getPasajeros(int numeroReserva) {
        try {
            Reserva reserva = agencia.buscarReserva(numeroReserva);
            if (reserva.cantidadPasajeros() == 0) {
                throw new ReglaNegocioException("La reserva todavía no tiene pasajeros.");
            }
            return reserva.getPasajeros();
        } catch (ReglaNegocioException e) {
            pantalla.mostrarError(e.getMessage());
            return null;
        }
    }

    // los servicios adicionales que ofrece el tour de la reserva, o null (mostrando el error) si no ofrece ninguno
    public List<ServicioTour> getServiciosDelTour(int numeroReserva) {
        try {
            Reserva reserva = agencia.buscarReserva(numeroReserva);
            List<ServicioTour> servicios = reserva.getSalida().getTour().getServicios();
            if (servicios.isEmpty()) {
                throw new ReglaNegocioException("El tour no ofrece servicios adicionales.");
            }
            return servicios;
        } catch (ReglaNegocioException e) {
            pantalla.mostrarError(e.getMessage());
            return null;
        }
    }

    // los servicios que contrató un pasajero (numeroPasajero: 1 es el primero), o null (mostrando el error) si no tiene
    public List<ServicioContratado> getServiciosContratados(int numeroReserva, int numeroPasajero) {
        try {
            Pasajero pasajero = pasajeroDe(agencia.buscarReserva(numeroReserva), numeroPasajero);
            if (pasajero.getServiciosContratados().isEmpty()) {
                throw new ReglaNegocioException("Este pasajero no tiene servicios adicionales.");
            }
            return pasajero.getServiciosContratados();
        } catch (ReglaNegocioException e) {
            pantalla.mostrarError(e.getMessage());
            return null;
        }
    }

    // ---- CU14: registrar cliente ----

    public boolean registrarCliente(Personal usuario, String nombre, String apellido, String documento, String telefono, String correoElectronico, String paisResidencia) {
        try {
            validarVendedor(usuario);
            Cliente cliente = new Cliente(nombre, apellido, documento, telefono, correoElectronico, paisResidencia);
            agencia.registrarCliente(cliente);
            pantalla.mostrarMensaje("Cliente registrado: " + cliente);
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    // CU15: crear reserva (después se le agregan los pasajeros)
    public Reserva crearReserva(Personal usuario, String documentoCliente, int numeroSalida, LocalDate hoy) {
        try {
            validarVendedor(usuario);
            Cliente cliente = agencia.buscarCliente(documentoCliente);
            Salida salida = agencia.buscarSalida(numeroSalida);
            Reserva reserva = agencia.crearReserva(salida, cliente, (Vendedor) usuario, hoy);
            pantalla.mostrarMensaje("Se creó la reserva N° " + reserva.getNumero() + ".");
            return reserva;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return null;
        }
    }

    // CU15 y CU17: pasajeros y servicios. Después de cada cambio se recalcula el total.
    public boolean agregarPasajero(Personal usuario, int numeroReserva, String nombre, String apellido, String documento, LocalDate fechaNacimiento, TipoPasajero tipo, String contactoEmergencia, String restriccionesSalud, String restriccionesAlimentarias) {
        try {
            Reserva reserva = reservaDe(usuario, numeroReserva);
            reserva.agregarPasajero(nombre, apellido, documento, fechaNacimiento, tipo, contactoEmergencia,
                    restriccionesSalud, restriccionesAlimentarias);
            calculadora.recalcularTotal(reserva);
            pantalla.mostrarMensaje("Pasajero agregado.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    // numeroPasajero es la posición que muestra la vista (1 es el primero)
    public boolean eliminarPasajero(Personal usuario, int numeroReserva, int numeroPasajero) {
        try {
            Reserva reserva = reservaDe(usuario, numeroReserva);
            reserva.eliminarPasajero(pasajeroDe(reserva, numeroPasajero));
            calculadora.recalcularTotal(reserva);
            pantalla.mostrarMensaje("Pasajero quitado.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean reemplazarPasajero(Personal usuario, int numeroReserva, int numeroPasajero, String nombre, String apellido, String documento, LocalDate fechaNacimiento, TipoPasajero tipo, String contactoEmergencia, String restriccionesSalud, String restriccionesAlimentarias, LocalDate hoy) {
        try {
            Reserva reserva = reservaDe(usuario, numeroReserva);
            reserva.reemplazarPasajero(pasajeroDe(reserva, numeroPasajero), nombre, apellido, documento,
                    fechaNacimiento, tipo, contactoEmergencia, restriccionesSalud, restriccionesAlimentarias, hoy);
            calculadora.recalcularTotal(reserva);
            pantalla.mostrarMensaje("Pasajero reemplazado.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean contratarServicio(Personal usuario, int numeroReserva, int numeroPasajero, int codigoServicio) {
        try {
            Reserva reserva = reservaDe(usuario, numeroReserva);
            ServicioAdicional servicio = agencia.buscarServicioAdicional(codigoServicio);
            reserva.contratarServicio(pasajeroDe(reserva, numeroPasajero), servicio);
            calculadora.recalcularTotal(reserva);
            pantalla.mostrarMensaje("Se contrató el servicio " + servicio.getNombre() + ".");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean quitarServicio(Personal usuario, int numeroReserva, int numeroPasajero, int codigoServicio) {
        try {
            Reserva reserva = reservaDe(usuario, numeroReserva);
            ServicioAdicional servicio = agencia.buscarServicioAdicional(codigoServicio);
            reserva.quitarServicio(pasajeroDe(reserva, numeroPasajero), servicio);
            calculadora.recalcularTotal(reserva);
            pantalla.mostrarMensaje("Se quitó el servicio " + servicio.getNombre() + ".");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    // CU16: registrar pago
    // devuelve el pago registrado (la vista muestra su recargo y el saldo), o null si falló
    public Pago registrarPago(Personal usuario, int numeroReserva, double importe, MedioPago medioPago,
                              int cuotas, LocalDate hoy) {
        try {
            Reserva reserva = reservaDe(usuario, numeroReserva);
            Pago pago = reserva.crearPago(hoy, importe, medioPago, cuotas);
            pantalla.mostrarMensaje("Pago registrado.");
            return pago;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return null;
        }
    }

    // CU18: cancelar reserva (el cliente la pide o la decide la agencia)
    // devuelve la cancelación (la vista muestra lo que se devuelve), o null si falló
    public Cancelacion cancelarReserva(Personal usuario, int numeroReserva, String motivo,
                                       OrigenCancelacion origen, LocalDate hoy) {
        try {
            Reserva reserva = reservaDe(usuario, numeroReserva);
            if (origen != OrigenCancelacion.CLIENTE && origen != OrigenCancelacion.AGENCIA) {
                throw new ReglaNegocioException("El origen debe ser CLIENTE o AGENCIA: la falta de pago la registra el sistema.");
            }
            Cancelacion cancelacion = reserva.cancelar(hoy, motivo, origen);
            pantalla.mostrarMensaje("La reserva N° " + numeroReserva + " quedó anulada.");
            return cancelacion;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return null;
        }
    }

    // CU19: nota de ajuste
    public boolean registrarNotaAjuste(Personal usuario, int numeroReserva, String motivo, double importe, LocalDate hoy) {
        try {
            Reserva reserva = reservaDe(usuario, numeroReserva);
            reserva.crearNotaAjuste(hoy, motivo, importe);
            pantalla.mostrarMensaje("Ajuste registrado.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    private void validarVendedor(Personal usuario) throws ReglaNegocioException {
        if (!(usuario instanceof Vendedor)) {
            throw new ReglaNegocioException("Solo un vendedor puede realizar esta operación.");
        }
    }

    // controla el rol y busca la reserva
    private Reserva reservaDe(Personal usuario, int numeroReserva) throws ReglaNegocioException {
        validarVendedor(usuario);
        return agencia.buscarReserva(numeroReserva);
    }

    private Pasajero pasajeroDe(Reserva reserva, int numeroPasajero) throws ReglaNegocioException {
        if (numeroPasajero < 1 || numeroPasajero > reserva.cantidadPasajeros()) {
            throw new ReglaNegocioException("La reserva no tiene un pasajero N° " + numeroPasajero + ".");
        }
        return reserva.getPasajeros().get(numeroPasajero - 1);
    }
}
