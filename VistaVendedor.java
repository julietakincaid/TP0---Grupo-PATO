package vista;

import controlador.ControladorReservas;
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
import modelo.enums.MedioPago;
import modelo.enums.OrigenCancelacion;
import modelo.enums.TipoPasajero;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Menú del vendedor: CU14 (registrar cliente), CU15 (crear reserva), CU16 (registrar pago), CU17 (modificar reserva), CU18 (cancelar reserva) y CU19 (nota de ajuste).
public class VistaVendedor {
    private final Consola consola;
    private final Listados listados;
    private final ControladorReservas controlador;
    private final Personal usuario;
    private final LocalDate hoy;
    private final Runnable guardar;

    public VistaVendedor(Consola consola, ControladorReservas controlador, Personal usuario, LocalDate hoy, Runnable guardar) {
        this.consola = consola;
        this.listados = new Listados(consola);
        this.controlador = controlador;
        this.usuario = usuario;
        this.hoy = hoy;
        this.guardar = guardar;
    }

    public void ejecutar() {
        int opcion;
        do {
            opcion = consola.menu("💼 Menú del vendedor", new String[]{
                    "👤 Clientes",
                    "📅 Salidas",
                    "💳 Pagos",
                    "📋 Reservas",
                    "❌ Cancelar una reserva",
                    "📝 Registrar una nota de ajuste"}, "🚪 Cerrar sesión");
            if (opcion == 1) {
                menuClientes();
            } else if (opcion == 2) {
                menuSalidas();
            } else if (opcion == 3) {
                menuPagos();
            } else if (opcion == 4) {
                menuReservas();
            } else if (opcion == 5) {
                cancelarReserva();
            } else if (opcion == 6) {
                registrarNotaAjuste();
            }
        } while (opcion != 0);
    }

    private void menuClientes() {
        int opcion;
        do {
            opcion = consola.menu("Clientes", new String[]{
                    "Registrar un cliente",
                    "Ver los clientes"}, "Volver");
            if (opcion == 1) {
                registrarCliente();
            } else if (opcion == 2) {
                verClientes();
            }
        } while (opcion != 0);
    }

    private void menuSalidas() {
        int opcion;
        do {
            opcion = consola.menu("Salidas", new String[]{
                    "Ver las salidas",
                    "Ver las reservas de una salida"}, "Volver");
            if (opcion == 1) {
                verSalidas();
            } else if (opcion == 2) {
                verReservasDeSalida();
            }
        } while (opcion != 0);
    }

    private void menuPagos() {
        int opcion;
        do {
            opcion = consola.menu("Pagos", new String[]{
                    "Registrar un pago",
                    "Ver los pagos de una reserva"}, "Volver");
            if (opcion == 1) {
                registrarPago();
            } else if (opcion == 2) {
                verPagosDeReserva();
            }
        } while (opcion != 0);
    }

    private void menuReservas() {
        int opcion;
        do {
            opcion = consola.menu("Reservas", new String[]{
                    "Crear una reserva",
                    "Modificar una reserva",
                    "Consultar una reserva"}, "Volver");
            if (opcion == 1) {
                crearReserva();
            } else if (opcion == 2) {
                modificarReserva();
            } else if (opcion == 3) {
                consultarReserva();
            }
        } while (opcion != 0);
    }

    // ---------- CU14: clientes ----------

    private void registrarCliente() {
        consola.subtitulo("Registrar un cliente");
        String nombre = consola.leerTexto("Nombre");
        String apellido = consola.leerTexto("Apellido");
        String documento = consola.leerTexto("Documento");
        String telefono = consola.leerTexto("Teléfono");
        String correo = consola.leerTexto("Correo electrónico");
        String pais = consola.leerTexto("País de residencia");
        if (controlador.registrarCliente(usuario, nombre, apellido, documento, telefono, correo, pais)) {
            guardar.run();
        }
    }

    private void verClientes() {
        List<Cliente> clientes = controlador.getClientes();
        if (clientes != null) {
            consola.subtitulo("Clientes");
            listados.mostrarClientes(clientes);
        }
    }

    // ---------- consultas ----------

    private void verSalidas() {
        consola.subtitulo("Salidas");
        listados.mostrarSalidas(controlador.getSalidas());
    }

    private void verReservasDeSalida() {
        consola.subtitulo("Reservas de una salida");
        Salida salida = listados.elegirSalida(controlador.getSalidas());
        if (salida != null) {
            listados.mostrarReservas(salida.getReservas());
        }
    }

    private void consultarReserva() {
        consola.subtitulo("Consultar una reserva");
        Reserva reserva = elegirReserva();
        if (reserva != null) {
            listados.mostrarReserva(reserva);
        }
    }

    // ---------- CU15: crear una reserva ----------

    private void crearReserva() {
        consola.subtitulo("Crear una reserva");
        String documento = consola.leerTexto("Documento del cliente");
        Cliente cliente = controlador.buscarCliente(documento);
        if (cliente == null) {
            return;
        }
        consola.info("Cliente: " + cliente);
        Salida salida = listados.elegirSalida(controlador.getSalidas());
        if (salida == null) {
            return;
        }
        Reserva reserva = controlador.crearReserva(usuario, documento, salida.getNumero(), hoy);
        if (reserva == null) {
            return;
        }
        guardar.run();
        do {
            agregarPasajero(reserva);
        } while (consola.confirmar("¿Agregar otro pasajero?"));
        listados.mostrarReserva(reserva);
    }

    // ---------- CU17: modificar una reserva ----------

    private void modificarReserva() {
        consola.subtitulo("Modificar una reserva");
        Reserva reserva = elegirReserva();
        if (reserva == null) {
            return;
        }
        int opcion;
        do {
            consola.info("Reserva N° " + reserva.getNumero() + " (" + reserva.getEstado() + ") - "
                    + reserva.cantidadPasajeros() + " pasajeros - total " + consola.formatearDinero(reserva.getTotal()));
            opcion = consola.menu("Modificar la reserva N° " + reserva.getNumero(), new String[]{
                    "Agregar un pasajero",
                    "Quitar un pasajero",
                    "Reemplazar un pasajero",
                    "Contratar un servicio adicional",
                    "Quitar un servicio adicional"}, "Volver");
            if (opcion == 1) {
                agregarPasajero(reserva);
            } else if (opcion == 2) {
                quitarPasajero(reserva);
            } else if (opcion == 3) {
                reemplazarPasajero(reserva);
            } else if (opcion == 4) {
                contratarServicioAUnPasajero(reserva);
            } else if (opcion == 5) {
                quitarServicio(reserva);
            }
        } while (opcion != 0);
    }

    private void agregarPasajero(Reserva reserva) {
        DatosPasajero datos = pedirDatosPasajero();
        if (controlador.agregarPasajero(usuario, reserva.getNumero(), datos.nombre, datos.apellido, datos.documento,
                datos.fechaNacimiento, datos.tipo, datos.contacto, datos.salud, datos.alimentarias)) {
            consola.info("Total de la reserva: " + consola.formatearDinero(reserva.getTotal()));
            guardar.run();
            ofrecerServicios(reserva, reserva.cantidadPasajeros());
        }
    }

    private void quitarPasajero(Reserva reserva) {
        int numeroPasajero = elegirNumeroPasajero(reserva);
        if (numeroPasajero == 0) {
            return;
        }
        if (!consola.confirmar("¿Quitar a este pasajero de la reserva?")) {
            consola.info("Operación cancelada.");
            return;
        }
        if (controlador.eliminarPasajero(usuario, reserva.getNumero(), numeroPasajero)) {
            consola.info("Total de la reserva: " + consola.formatearDinero(reserva.getTotal()));
            guardar.run();
        }
    }

    private void reemplazarPasajero(Reserva reserva) {
        int numeroPasajero = elegirNumeroPasajero(reserva);
        if (numeroPasajero == 0) {
            return;
        }
        consola.info("Se reemplaza a " + reserva.getPasajeros().get(numeroPasajero - 1) + ". Datos del nuevo pasajero:");
        DatosPasajero datos = pedirDatosPasajero();
        if (controlador.reemplazarPasajero(usuario, reserva.getNumero(), numeroPasajero, datos.nombre, datos.apellido,
                datos.documento, datos.fechaNacimiento, datos.tipo, datos.contacto, datos.salud,
                datos.alimentarias, hoy)) {
            consola.info("Total de la reserva: " + consola.formatearDinero(reserva.getTotal()));
            guardar.run();
        }
    }

    // después de agregar un pasajero se le pregunta al vendedor si quiere sumarle servicios;
    // se deja de preguntar si dice que no o si ya no se puede contratar
    private void ofrecerServicios(Reserva reserva, int numeroPasajero) {
        while (consola.confirmar("¿Contratar un servicio adicional para este pasajero?")) {
            if (!contratarServicio(reserva, numeroPasajero)) {
                return;
            }
        }
    }

    private void contratarServicioAUnPasajero(Reserva reserva) {
        int numeroPasajero = elegirNumeroPasajero(reserva);
        if (numeroPasajero != 0) {
            contratarServicio(reserva, numeroPasajero);
        }
    }

    // devuelve true si se contrató el servicio
    private boolean contratarServicio(Reserva reserva, int numeroPasajero) {
        List<ServicioTour> servicios = controlador.getServiciosDelTour(reserva.getNumero());
        if (servicios == null) {
            return false;
        }
        List<String> textos = new ArrayList<>();
        for (ServicioTour servicio : servicios) {
            String texto = servicio.getServicioAdicional().getNombre() + " - "
                    + consola.formatearDinero(servicio.getServicioAdicional().getPrecio());
            if (servicio.esObligatorio()) {
                texto += " (obligatorio: ya está incluido)";
            }
            textos.add(texto);
        }
        ServicioAdicional servicio = servicios.get(consola.elegirPosicion("Servicio", textos)).getServicioAdicional();
        if (controlador.contratarServicio(usuario, reserva.getNumero(), numeroPasajero, servicio.getCodigo())) {
            consola.info("Total de la reserva: " + consola.formatearDinero(reserva.getTotal()));
            guardar.run();
            return true;
        }
        return false;
    }

    private void quitarServicio(Reserva reserva) {
        int numeroPasajero = elegirNumeroPasajero(reserva);
        if (numeroPasajero == 0) {
            return;
        }
        List<ServicioContratado> contratados = controlador.getServiciosContratados(reserva.getNumero(), numeroPasajero);
        if (contratados == null) {
            return;
        }
        List<String> textos = new ArrayList<>();
        for (ServicioContratado contratado : contratados) {
            textos.add(contratado.getServicioAdicional().getNombre() + " - " + consola.formatearDinero(contratado.getPrecioAplicado()));
        }
        ServicioContratado elegido = contratados.get(consola.elegirPosicion("Servicio a quitar", textos));
        if (controlador.quitarServicio(usuario, reserva.getNumero(), numeroPasajero,
                elegido.getServicioAdicional().getCodigo())) {
            consola.info("Total de la reserva: " + consola.formatearDinero(reserva.getTotal()));
            guardar.run();
        }
    }

    // ---------- CU16: registrar un pago ----------

    // se elige entre las reservas que todavía tienen saldo; antes de cobrar se ve el estado de cuenta
    private void registrarPago() {
        consola.subtitulo("Registrar un pago");
        Reserva reserva = elegirReservaConSaldo();
        if (reserva == null) {
            return;
        }
        listados.mostrarPagos(reserva);
        double importe = consola.leerDecimal("Importe a pagar");
        List<MedioPago> medios = controlador.getMediosDePago();
        MedioPago medio = medios.get(consola.elegirPosicion("Medio de pago", medios));
        int cuotas = consola.leerEntero("Cantidad de cuotas (1 si es un solo pago)");
        Pago pago = controlador.registrarPago(usuario, reserva.getNumero(), importe, medio, cuotas, hoy);
        if (pago != null) {
            if (pago.calcularRecargo() > 0) {
                consola.aviso("Recargo por tarjeta en cuotas: " + consola.formatearDinero(pago.calcularRecargo())
                        + ". Se le cobra al cliente " + consola.formatearDinero(pago.calcularTotalPago()) + ".");
            }
            consola.info("Saldo pendiente: " + consola.formatearDinero(reserva.calcularSaldo())
                    + " | Estado de la reserva: " + reserva.getEstado());
            guardar.run();
        }
    }

    private void verPagosDeReserva() {
        consola.subtitulo("Pagos de una reserva");
        Reserva reserva = elegirReserva();
        if (reserva != null) {
            listados.mostrarPagos(reserva);
        }
    }

    // ---------- CU18: cancelar una reserva ----------

    private void cancelarReserva() {
        consola.subtitulo("Cancelar una reserva");
        Reserva reserva = elegirReserva();
        if (reserva == null) {
            return;
        }
        consola.info("Cobrado hasta ahora: "
                + consola.formatearDinero(reserva.calcularTotalAbonado() + reserva.calcularTotalRecargos()));
        String motivo = consola.leerTexto("Motivo de la cancelación");
        consola.texto("¿Quién cancela?");
        List<OrigenCancelacion> origenes = controlador.getOrigenesDeCancelacion();
        OrigenCancelacion origen = origenes.get(consola.elegirPosicion("Origen", origenes));
        if (!consola.confirmar("¿Cancelar la reserva N° " + reserva.getNumero() + "?")) {
            consola.info("Operación cancelada.");
            return;
        }
        Cancelacion cancelacion = controlador.cancelarReserva(usuario, reserva.getNumero(), motivo, origen, hoy);
        if (cancelacion != null) {
            listados.mostrarCancelacion(cancelacion);
            guardar.run();
        }
    }

    // ---------- CU19: nota de ajuste ----------

    private void registrarNotaAjuste() {
        consola.subtitulo("Registrar una nota de ajuste");
        Reserva reserva = elegirReserva();
        if (reserva == null) {
            return;
        }
        consola.info("Total actual: " + consola.formatearDinero(reserva.getTotal()));
        String motivo = consola.leerTexto("Motivo del ajuste");
        double importe = consola.leerDecimal("Importe del ajuste (positivo suma al total, negativo resta)");
        if (controlador.registrarNotaAjuste(usuario, reserva.getNumero(), motivo, importe, hoy)) {
            consola.info("Nuevo total: " + consola.formatearDinero(reserva.getTotal()));
            guardar.run();
        }
    }

    // ---------- apoyo ----------

    // pide el número de una reserva y se la pide al controlador; devuelve null si no existe
    private Reserva elegirReserva() {
        int numero = consola.leerEntero("Número de reserva");
        return controlador.buscarReserva(numero);
    }

    // muestra las reservas que todavía admiten pagos y devuelve la elegida (o null si no hay)
    private Reserva elegirReservaConSaldo() {
        List<Reserva> reservas = controlador.getReservasConSaldo();
        if (reservas == null) {
            return null;
        }
        List<String> textos = new ArrayList<>();
        for (Reserva reserva : reservas) {
            textos.add("N° " + reserva.getNumero() + " - " + reserva.getCliente() + " - "
                    + reserva.getSalida().getTour().getNombre() + " - saldo "
                    + consola.formatearDinero(reserva.calcularSaldo()));
        }
        return reservas.get(consola.elegirPosicion("Reserva", textos));
    }

    // muestra los pasajeros de la reserva numerados y devuelve el número elegido (0 si no hay pasajeros)
    private int elegirNumeroPasajero(Reserva reserva) {
        List<Pasajero> pasajeros = controlador.getPasajeros(reserva.getNumero());
        if (pasajeros == null) {
            return 0;
        }
        return consola.elegirPosicion("Pasajero", pasajeros) + 1;
    }

    // los datos de un pasajero se piden en dos lugares (agregar y reemplazar), así que se juntan acá
    private DatosPasajero pedirDatosPasajero() {
        DatosPasajero datos = new DatosPasajero();
        datos.nombre = consola.leerTexto("Nombre del pasajero");
        datos.apellido = consola.leerTexto("Apellido");
        datos.documento = consola.leerTexto("Documento");
        datos.fechaNacimiento = consola.leerFecha("Fecha de nacimiento");
        List<TipoPasajero> tipos = controlador.getTiposDePasajero();
        datos.tipo = tipos.get(consola.elegirPosicion("Tipo de pasajero", tipos));
        datos.contacto = consola.leerTexto("Contacto de emergencia");
        datos.salud = consola.leerTextoOpcional("Restricciones de salud");
        datos.alimentarias = consola.leerTextoOpcional("Restricciones alimentarias");
        return datos;
    }

    // una clase chica, solo para esta vista: junta los datos que se piden de un pasajero
    private static class DatosPasajero {
        String nombre;
        String apellido;
        String documento;
        LocalDate fechaNacimiento;
        TipoPasajero tipo;
        String contacto;
        String salud;
        String alimentarias;
    }
}