package controlador;

import excepcion.ReglaNegocioException;
import modelo.Administrador;
import modelo.LiquidacionGuia;
import modelo.Personal;
import modelo.Reserva;
import modelo.Salida;
import modelo.Tour;
import modelo.Vendedor;
import modelo.enums.EstadoSalida;
import servicio.Agencia;
import servicio.ServicioLiquidacion;
import servicio.ServicioReportes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Casos de uso del administrador sobre la operación: CU08 (liquidar honorarios del guía) y CU09 (emitir reportes).
public class ControladorAdministracion {
    private final Agencia agencia;
    private final ServicioLiquidacion servicioLiquidacion;
    private final ServicioReportes servicioReportes;
    private final Pantalla pantalla;

    public ControladorAdministracion(Agencia agencia, ServicioLiquidacion servicioLiquidacion, ServicioReportes servicioReportes, Pantalla pantalla) {
        if (agencia == null || servicioLiquidacion == null || servicioReportes == null || pantalla == null) {
            throw new IllegalArgumentException("La agencia, los servicios y la pantalla son obligatorios.");
        }
        this.agencia = agencia;
        this.servicioLiquidacion = servicioLiquidacion;
        this.servicioReportes = servicioReportes;
        this.pantalla = pantalla;
    }

    public List<Vendedor> getVendedores() {
        List<Vendedor> vendedores = agencia.getVendedores();
        if (vendedores.isEmpty()) {
            pantalla.mostrarError("No hay vendedores cargados.");
            return null;
        }
        return vendedores;
    }

    public List<Tour> getTours() {
        List<Tour> tours = agencia.getTours();
        if (tours.isEmpty()) {
            pantalla.mostrarError("No hay tours cargados.");
            return null;
        }
        return tours;
    }

    // las salidas finalizadas que todavía no tienen su liquidación (las que se pueden liquidar)
    public List<Salida> getSalidasPendientesDeLiquidar() {
        List<Salida> pendientes = new ArrayList<>();
        for (Salida salida : agencia.getSalidas()) {
            if (salida.getEstado() == EstadoSalida.FINALIZADA && salida.getLiquidacionGuia() == null) {
                pendientes.add(salida);
            }
        }
        if (pendientes.isEmpty()) {
            pantalla.mostrarError("No hay salidas finalizadas pendientes de liquidar.");
            return null;
        }
        return pendientes;
    }

    // CU08: liquidar honorarios del guía
    public LiquidacionGuia liquidarHonorarios(Personal usuario, int numeroSalida, LocalDate hoy) {
        try {
            validarAdministrador(usuario);
            Salida salida = agencia.buscarSalida(numeroSalida);
            LiquidacionGuia liquidacion =
                    servicioLiquidacion.generarLiquidacion(salida, agencia.proximoNumeroLiquidacion(), hoy);
            pantalla.mostrarMensaje("Se generó la liquidación N° " + liquidacion.getNumero() + ".");
            return liquidacion;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return null;
        }
    }

    // CU09: reportes. Todos reciben el período (desde y hasta, inclusive).

    // salidas que empiezan en el período; la ocupación de cada una se lee con salida.calcularOcupacion()
    public List<Salida> reporteOcupacion(Personal usuario, LocalDate desde, LocalDate hasta) {
        try {
            validarAdministrador(usuario);
            return servicioReportes.reporteOcupacion(agencia.getSalidas(), desde, hasta);
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return null;
        }
    }

    // ventas de un vendedor (la vista lo llama una vez por cada vendedor)
    public Integer contarVentas(Personal usuario, Vendedor vendedor, LocalDate desde, LocalDate hasta) {
        try {
            validarAdministrador(usuario);
            return servicioReportes.contarVentas(agencia.getSalidas(), vendedor, desde, hasta);
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return null;
        }
    }

    public Double calcularTotalVendido(Personal usuario, Vendedor vendedor, LocalDate desde, LocalDate hasta) {
        try {
            validarAdministrador(usuario);
            return servicioReportes.calcularTotalVendido(agencia.getSalidas(), vendedor, desde, hasta);
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return null;
        }
    }

    // la comisión que le corresponde al vendedor por lo que vendió en el período
    public Double calcularComisionVendedor(Personal usuario, Vendedor vendedor, LocalDate desde, LocalDate hasta) {
        Double totalVendido = calcularTotalVendido(usuario, vendedor, desde, hasta);
        if (totalVendido == null) {
            return null;
        }
        return Math.round(vendedor.calcularComision(totalVendido) * 100.0) / 100.0;
    }

    // ingresos de un tour (la vista lo llama una vez por cada tour)
    public Double calcularIngresosCobrados(Personal usuario, Tour tour, LocalDate desde, LocalDate hasta) {
        try {
            validarAdministrador(usuario);
            return servicioReportes.calcularIngresosCobrados(agencia.getSalidas(), tour, desde, hasta);
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return null;
        }
    }

    public Double calcularDevoluciones(Personal usuario, Tour tour, LocalDate desde, LocalDate hasta) {
        try {
            validarAdministrador(usuario);
            return servicioReportes.calcularDevoluciones(agencia.getSalidas(), tour, desde, hasta);
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return null;
        }
    }

    public Double calcularIngresosNetos(Personal usuario, Tour tour, LocalDate desde, LocalDate hasta) {
        try {
            validarAdministrador(usuario);
            return servicioReportes.calcularIngresosNetos(agencia.getSalidas(), tour, desde, hasta);
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return null;
        }
    }

    // reservas canceladas en el período
    public List<Reserva> reporteCancelaciones(Personal usuario, LocalDate desde, LocalDate hasta) {
        try {
            validarAdministrador(usuario);
            return servicioReportes.reporteCancelaciones(agencia.getSalidas(), desde, hasta);
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return null;
        }
    }

    // total devuelto en una lista de reservas canceladas (el pie del reporte de cancelaciones)
    public double calcularTotalDevuelto(List<Reserva> reservasCanceladas) {
        return servicioReportes.calcularTotalDevuelto(reservasCanceladas);
    }

    private void validarAdministrador(Personal usuario) throws ReglaNegocioException {
        if (!(usuario instanceof Administrador)) {
            throw new ReglaNegocioException("Solo el administrador puede realizar esta operación.");
        }
    }
}