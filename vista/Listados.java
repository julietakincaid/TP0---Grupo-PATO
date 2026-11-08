package vista;

import modelo.Cancelacion;
import modelo.Devolucion;
import modelo.Guia;
import modelo.NotaAjuste;
import modelo.Pago;
import modelo.Pasajero;
import modelo.Personal;
import modelo.Proveedor;
import modelo.Reserva;
import modelo.Salida;
import modelo.ServicioAdicional;
import modelo.ServicioContratado;
import modelo.Tour;
import modelo.Vehiculo;
import modelo.Vendedor;
import modelo.enums.EstadoReserva;
import modelo.Cliente;

import java.util.ArrayList;
import java.util.List;

// Los listados que se repiten en varias vistas (salidas, pasajeros...), para escribirlos una sola vez.
public class Listados {
    private final Consola consola;

    public Listados(Consola consola) {
        this.consola = consola;
    }

    public void mostrarSalidas(List<Salida> salidas) {
        if (salidas.isEmpty()) {
            consola.aviso("No hay salidas para mostrar.");
            return;
        }
        List<String[]> filas = new ArrayList<>();
        for (Salida salida : salidas) {
            String guia = "-";
            if (salida.getGuia() != null) {
                guia = salida.getGuia().getNombre();
            }
            String vehiculo = "-";
            if (salida.getVehiculo() != null) {
                vehiculo = salida.getVehiculo().getPatente();
            }
            filas.add(new String[]{
                    String.valueOf(salida.getNumero()),
                    salida.getTour().getNombre(),
                    consola.formatear(salida.getFechaInicio()),
                    salida.getEstado().toString(),
                    salida.calcularOcupacion() + "/" + salida.getCupoMaximo(),
                    guia,
                    vehiculo});
        }
        consola.tabla(new String[]{"N°", "Tour", "Inicio", "Estado", "Ocupación", "Guía", "Vehículo"}, filas);
    }

    // muestra las salidas numeradas para elegir una
    public Salida elegirSalida(List<Salida> salidas) {
        if (salidas.isEmpty()) {
            consola.aviso("No hay salidas para elegir.");
            return null;
        }
        List<String> textos = new ArrayList<>();
        for (Salida salida : salidas) {
            textos.add("N° " + salida.getNumero() + " - " + salida.getTour().getNombre() + " - "
                    + consola.formatear(salida.getFechaInicio()) + " - " + salida.getEstado());
        }
        return salidas.get(consola.elegirPosicion("Elegí la salida", textos));
    }

    // la lista de pasajeros de una salida, con los datos que necesita el guía
    public void mostrarPasajeros(Salida salida, List<Pasajero> pasajeros) {
        consola.subtitulo("Pasajeros de la salida N° " + salida.getNumero() + " - " + salida.getTour().getNombre());
        List<String[]> filas = new ArrayList<>();
        int numero = 1;
        for (Pasajero pasajero : pasajeros) {
            filas.add(new String[]{
                    String.valueOf(numero),
                    pasajero.getApellido() + ", " + pasajero.getNombre(),
                    pasajero.getDocumento(),
                    String.valueOf(pasajero.calcularEdad(salida.getFechaInicio())),
                    pasajero.getContactoEmergencia(),
                    pasajero.getRestriccionesSalud(),
                    pasajero.getRestriccionesAlimentarias()});
            numero++;
        }
        consola.tabla(new String[]{"N°", "Pasajero", "Documento", "Edad", "Contacto de emergencia", "Salud", "Alimentación"}, filas);
        consola.info("Total: " + pasajeros.size() + " pasajeros.");
    }

    public void mostrarReservas(List<Reserva> reservas) {
        if (reservas.isEmpty()) {
            consola.aviso("No hay reservas para mostrar.");
            return;
        }
        List<String[]> filas = new ArrayList<>();
        for (Reserva reserva : reservas) {
            filas.add(new String[]{
                    String.valueOf(reserva.getNumero()),
                    reserva.getCliente().toString(),
                    reserva.getEstado().toString(),
                    String.valueOf(reserva.cantidadPasajeros()),
                    consola.formatearDinero(reserva.getTotal()),
                    saldoParaMostrar(reserva)});
        }
        consola.tabla(new String[]{"N°", "Cliente", "Estado", "Pasajeros", "Total", "Saldo"}, filas);
    }

    // el detalle completo de una reserva
    public void mostrarReserva(Reserva reserva) {
        Salida salida = reserva.getSalida();
        consola.subtitulo("Reserva N° " + reserva.getNumero());
        consola.texto("  Estado:    " + reserva.getEstado());
        consola.texto("  Cliente:   " + reserva.getCliente());
        consola.texto("  Salida:    N° " + salida.getNumero() + " - " + salida.getTour().getNombre()
                + " (" + consola.formatear(salida.getFechaInicio()) + " a " + consola.formatear(salida.getFechaFin()) + ")");
        consola.texto("  Vendedor:  " + reserva.getVendedor().getNombre());
        if (reserva.getPromocionAplicada() != null) {
            consola.texto("  Promoción: " + reserva.getPromocionAplicada());
        }
        consola.texto("  Total:     " + consola.formatearDinero(reserva.getTotal()));
        consola.texto("  Abonado:   " + consola.formatearDinero(reserva.calcularTotalAbonado()));
        consola.texto("  Saldo:     " + saldoParaMostrar(reserva));
        if (reserva.calcularTotalRecargos() > 0) {
            consola.texto("  Recargo de tarjeta (se cobra aparte): " + consola.formatearDinero(reserva.calcularTotalRecargos()));
        }

        if (!reserva.getPasajeros().isEmpty()) {
            consola.linea();
            List<String[]> filas = new ArrayList<>();
            int numero = 1;
            for (Pasajero pasajero : reserva.getPasajeros()) {
                String servicios = "";
                for (ServicioContratado servicio : pasajero.getServiciosContratados()) {
                    if (!servicios.isEmpty()) {
                        servicios += ", ";
                    }
                    servicios += servicio.getServicioAdicional().getNombre();
                }
                if (servicios.isEmpty()) {
                    servicios = "-";
                }
                filas.add(new String[]{String.valueOf(numero), pasajero.getApellido() + ", " + pasajero.getNombre(),
                        pasajero.getTipo().toString(), servicios});
                numero++;
            }
            consola.tabla(new String[]{"N°", "Pasajero", "Tipo", "Servicios adicionales"}, filas);
        }

        if (!reserva.getPagos().isEmpty()) {
            consola.linea();
            mostrarTablaDePagos(reserva);
        }

        if (!reserva.getNotasAjuste().isEmpty()) {
            consola.linea();
            List<String[]> filas = new ArrayList<>();
            for (NotaAjuste nota : reserva.getNotasAjuste()) {
                filas.add(new String[]{String.valueOf(nota.getNumero()), consola.formatear(nota.getFecha()),
                        consola.formatearDinero(nota.getImporte()), nota.getMotivo()});
            }
            consola.tabla(new String[]{"Ajuste", "Fecha", "Importe", "Motivo"}, filas);
        }

        if (reserva.getCancelacion() != null) {
            consola.linea();
            mostrarCancelacion(reserva.getCancelacion());
        }
    }

    // la cancelación de una reserva y lo que se devolvió, pago por pago
    public void mostrarCancelacion(Cancelacion cancelacion) {
        consola.texto("  Cancelación: " + cancelacion.getOrigen() + " el " + consola.formatear(cancelacion.getFecha())
                + " - " + cancelacion.getMotivo());
        if (cancelacion.getDevoluciones().isEmpty()) {
            consola.info("No corresponde devolución.");
            return;
        }
        List<String[]> filas = new ArrayList<>();
        for (Devolucion devolucion : cancelacion.getDevoluciones()) {
            filas.add(new String[]{String.valueOf(devolucion.getNumero()),
                    consola.formatearDinero(devolucion.getImporte()), devolucion.getMedioPago().toString()});
        }
        consola.tabla(new String[]{"Devolución", "Importe", "Medio"}, filas);
        consola.info("Total a devolver: " + consola.formatearDinero(cancelacion.calcularTotalDevuelto()));
    }

    // una reserva anulada ya no debe nada: se muestra una raya en lugar del saldo
    private String saldoParaMostrar(Reserva reserva) {
        if (reserva.getEstado() == EstadoReserva.ANULADA) {
            return "-";
        }
        return consola.formatearDinero(reserva.calcularSaldo());
    }

    // una lista numerada de textos (sirve para actividades, tarifas, servicios de un tour...)
    public void mostrarLista(List<String> textos) {
        int numero = 1;
        for (String texto : textos) {
            consola.texto("  " + numero + ") " + texto);
            numero++;
        }
    }

    public void mostrarTours(List<Tour> tours, List<Tour> conSalidasVigentes) {
        List<String[]> filas = new ArrayList<>();
        for (Tour tour : tours) {
            String estado = "Activo";
            if (!tour.estaActivo()) {
                estado = "De baja";
            }
            String salidasVigentes = "No";
            if (conSalidasVigentes.contains(tour)) {
                salidasVigentes = "Sí";
            }
            filas.add(new String[]{tour.getCodigo(), tour.getNombre(), tour.getCategoria().toString(),
                    String.valueOf(tour.getDuracionDias()), tour.getCupoMinimo() + " a " + tour.getCupoMaximo(),
                    estado, salidasVigentes});
        }
        consola.tabla(new String[]{"Código", "Nombre", "Categoría", "Días", "Cupos", "Estado", "Salidas vigentes"}, filas);
    }

    public void mostrarProveedores(List<Proveedor> proveedores) {
        List<String[]> filas = new ArrayList<>();
        for (Proveedor proveedor : proveedores) {
            filas.add(new String[]{proveedor.getNombre(), proveedor.getCuit(), proveedor.getContacto(),
                    proveedor.getTelefono(), proveedor.getServicioPrestado()});
        }
        consola.tabla(new String[]{"Nombre", "CUIT", "Contacto", "Teléfono", "Servicio"}, filas);
    }

    public void mostrarServicios(List<ServicioAdicional> servicios) {
        List<String[]> filas = new ArrayList<>();
        for (ServicioAdicional servicio : servicios) {
            filas.add(new String[]{String.valueOf(servicio.getCodigo()), servicio.getNombre(),
                    servicio.getTipo().toString(), consola.formatearDinero(servicio.getPrecio())});
        }
        consola.tabla(new String[]{"Código", "Nombre", "Tipo", "Precio"}, filas);
    }

    public void mostrarVehiculos(List<Vehiculo> vehiculos) {
        List<String[]> filas = new ArrayList<>();
        for (Vehiculo vehiculo : vehiculos) {
            filas.add(new String[]{vehiculo.getPatente(), vehiculo.getTipo().toString(),
                    String.valueOf(vehiculo.getCapacidad())});
        }
        consola.tabla(new String[]{"Patente", "Tipo", "Capacidad"}, filas);
    }

    public void mostrarPersonal(List<Personal> personal) {
        List<String[]> filas = new ArrayList<>();
        for (Personal persona : personal) {
            filas.add(new String[]{String.valueOf(persona.getLegajo()), persona.getNombre(),
                    persona.getDocumento(), persona.getTelefono()});
        }
        consola.tabla(new String[]{"Legajo", "Nombre", "Documento", "Teléfono"}, filas);
    }

    public void mostrarGuias(List<Guia> guias) {
        List<String[]> filas = new ArrayList<>();
        for (Guia guia : guias) {
            filas.add(new String[]{String.valueOf(guia.getLegajo()), guia.getNombre(),
                    consola.formatearDinero(guia.getHonorarioPorDia()), guia.getIdiomas().toString(),
                    guia.getCategoriasHabilitadas().toString()});
        }
        consola.tabla(new String[]{"Legajo", "Nombre", "Honorario por día", "Idiomas", "Categorías"}, filas);
    }

    public void mostrarVendedores(List<Vendedor> vendedores) {
        List<String[]> filas = new ArrayList<>();
        for (Vendedor vendedor : vendedores) {
            filas.add(new String[]{String.valueOf(vendedor.getLegajo()), vendedor.getNombre(),
                    vendedor.getComision() + "%"});
        }
        consola.tabla(new String[]{"Legajo", "Nombre", "Comisión"}, filas);
    }

    // reporte de ocupación: una fila por salida
    public void mostrarOcupacion(List<Salida> salidas) {
        List<String[]> filas = new ArrayList<>();
        for (Salida salida : salidas) {
            filas.add(new String[]{String.valueOf(salida.getNumero()), salida.getTour().getNombre(),
                    consola.formatear(salida.getFechaInicio()), salida.getEstado().toString(),
                    salida.calcularOcupacion() + "/" + salida.getCupoMaximo(),
                    String.format("%.1f%%", salida.calcularPorcentajeOcupacion())});
        }
        consola.tabla(new String[]{"N°", "Tour", "Inicio", "Estado", "Ocupación", "%"}, filas);
    }

    // reporte de cancelaciones: una fila por reserva cancelada
    public void mostrarCancelaciones(List<Reserva> reservas) {
        List<String[]> filas = new ArrayList<>();
        for (Reserva reserva : reservas) {
            Cancelacion cancelacion = reserva.getCancelacion();
            filas.add(new String[]{String.valueOf(reserva.getNumero()), reserva.getCliente().toString(),
                    consola.formatear(cancelacion.getFecha()), cancelacion.getOrigen().toString(),
                    cancelacion.getMotivo(), consola.formatearDinero(cancelacion.calcularTotalDevuelto())});
        }
        consola.tabla(new String[]{"Reserva", "Cliente", "Fecha", "Origen", "Motivo", "Devuelto"}, filas);
    }


    // el estado de cuenta de una reserva: total, abonado, saldo y el detalle de cada pago
    public void mostrarPagos(Reserva reserva) {
        consola.info("Reserva N° " + reserva.getNumero() + " - " + reserva.getCliente()
                + " | Total: " + consola.formatearDinero(reserva.getTotal())
                + " | Abonado: " + consola.formatearDinero(reserva.calcularTotalAbonado())
                + " | Saldo: " + saldoParaMostrar(reserva));
        if (reserva.getPagos().isEmpty()) {
            consola.aviso("La reserva todavía no tiene pagos.");
            return;
        }
        mostrarTablaDePagos(reserva);
    }

    private void mostrarTablaDePagos(Reserva reserva) {
        List<String[]> filas = new ArrayList<>();
        for (Pago pago : reserva.getPagos()) {
            filas.add(new String[]{String.valueOf(pago.getNumero()), consola.formatear(pago.getFecha()),
                    consola.formatearDinero(pago.getImporte()), pago.getMedioPago().toString(),
                    String.valueOf(pago.getCantidadCuotas()), consola.formatearDinero(pago.calcularRecargo())});
        }
        consola.tabla(new String[]{"Pago", "Fecha", "Importe", "Medio", "Cuotas", "Recargo"}, filas);
    }

    public void mostrarClientes(List<Cliente> clientes) {
        List<String[]> filas = new ArrayList<>();
        for (Cliente cliente : clientes) {
            filas.add(new String[]{cliente.getDocumento(), cliente.getApellido() + ", " + cliente.getNombre(),
                    cliente.getTelefono(), cliente.getCorreoElectronico(), cliente.getPaisResidencia()});
        }
        consola.tabla(new String[]{"Documento", "Cliente", "Teléfono", "Correo", "País"}, filas);
    }
}
