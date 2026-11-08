package servicio;

import excepcion.ReglaNegocioException;
import modelo.Cliente;
import modelo.Guia;
import modelo.Personal;
import modelo.Proveedor;
import modelo.PromocionAnticipada;
import modelo.PromocionGrupo;
import modelo.Reserva;
import modelo.Salida;
import modelo.ServicioAdicional;
import modelo.Tour;
import modelo.Vehiculo;
import modelo.Vendedor;
import modelo.enums.CategoriaTour;
import modelo.enums.EstadoSalida;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Guarda todos los datos de la agencia y controla las reglas que una entidad sola no puede
public class Agencia implements Serializable {
    private final List<Tour> tours;
    private final List<ServicioAdicional> serviciosAdicionales;
    private final List<Proveedor> proveedores;
    private final List<Vehiculo> vehiculos;
    private final List<Cliente> clientes;
    private final List<Personal> personal;
    private final List<Salida> salidas;
    private final GestorPromociones gestorPromociones;

    public Agencia() {
        this.tours = new ArrayList<>();
        this.serviciosAdicionales = new ArrayList<>();
        this.proveedores = new ArrayList<>();
        this.vehiculos = new ArrayList<>();
        this.clientes = new ArrayList<>();
        this.personal = new ArrayList<>();
        this.salidas = new ArrayList<>();
        this.gestorPromociones = new GestorPromociones();

        // las promociones del enunciado: se cargan solo cuando la agencia se crea por primera vez
        try {
            gestorPromociones.agregarPromocion(new PromocionAnticipada());
            gestorPromociones.agregarPromocion(new PromocionGrupo());
        } catch (ReglaNegocioException e) {
            throw new IllegalStateException("No debería ocurrir: el gestor recién creado está vacío.", e);
        }
    }

    public List<Tour> getTours() {
        return Collections.unmodifiableList(tours);
    }

    public List<ServicioAdicional> getServiciosAdicionales() {
        return Collections.unmodifiableList(serviciosAdicionales);
    }

    public List<Proveedor> getProveedores() {
        return Collections.unmodifiableList(proveedores);
    }

    public List<Vehiculo> getVehiculos() {
        return Collections.unmodifiableList(vehiculos);
    }

    public List<Cliente> getClientes() {
        return Collections.unmodifiableList(clientes);
    }

    public List<Personal> getPersonal() {
        return Collections.unmodifiableList(personal);
    }

    public List<Salida> getSalidas() {
        return Collections.unmodifiableList(salidas);
    }

    public List<Vendedor> getVendedores() {
        List<Vendedor> vendedores = new ArrayList<>();
        for (Personal persona : personal) {
            if (persona instanceof Vendedor) {
                vendedores.add((Vendedor) persona);
            }
        }
        return vendedores;
    }

    public List<Guia> getGuias() {
        List<Guia> guias = new ArrayList<>();
        for (Personal persona : personal) {
            if (persona instanceof Guia) {
                guias.add((Guia) persona);
            }
        }
        return guias;
    }

    public List<Salida> getSalidasDelGuia(Guia guia) {
        validarNoNulo(guia, "El guía es obligatorio.");
        List<Salida> propias = new ArrayList<>();
        for (Salida salida : salidas) {
            if (guia.equals(salida.getGuia()) && salida.getEstado() != EstadoSalida.CANCELADA) {
                propias.add(salida);
            }
        }
        return propias;
    }

    public GestorPromociones getGestorPromociones() {
        return gestorPromociones;
    }

    // altas (cada una controla que la clave no esté repetida)
    public void registrarTour(Tour tour) throws ReglaNegocioException {
        validarNoNulo(tour, "El tour es obligatorio.");
        if (tours.contains(tour)) {
            throw new ReglaNegocioException("Ya existe un tour con el código " + tour.getCodigo() + ".");
        }
        tours.add(tour);
    }

    public void registrarServicioAdicional(ServicioAdicional servicio) throws ReglaNegocioException {
        validarNoNulo(servicio, "El servicio adicional es obligatorio.");
        if (serviciosAdicionales.contains(servicio)) {
            throw new ReglaNegocioException("Ya existe un servicio adicional con el código " + servicio.getCodigo() + ".");
        }
        serviciosAdicionales.add(servicio);
    }

    public void registrarProveedor(Proveedor proveedor) throws ReglaNegocioException {
        validarNoNulo(proveedor, "El proveedor es obligatorio.");
        if (proveedores.contains(proveedor)) {
            throw new ReglaNegocioException("Ya existe un proveedor con el CUIT " + proveedor.getCuit() + ".");
        }
        proveedores.add(proveedor);
    }

    public void registrarVehiculo(Vehiculo vehiculo) throws ReglaNegocioException {
        validarNoNulo(vehiculo, "El vehículo es obligatorio.");
        if (vehiculos.contains(vehiculo)) {
            throw new ReglaNegocioException("Ya existe un vehículo con la patente " + vehiculo.getPatente() + ".");
        }
        vehiculos.add(vehiculo);
    }

    public void registrarCliente(Cliente cliente) throws ReglaNegocioException {
        validarNoNulo(cliente, "El cliente es obligatorio.");
        if (clientes.contains(cliente)) {
            throw new ReglaNegocioException("Ya existe un cliente con el documento " + cliente.getDocumento() + ".");
        }
        clientes.add(cliente);
    }

    public void registrarPersonal(Personal persona) throws ReglaNegocioException {
        validarNoNulo(persona, "El empleado es obligatorio.");
        if (personal.contains(persona)) {
            throw new ReglaNegocioException("Ya existe un empleado con el legajo " + persona.getLegajo() + ".");
        }
        personal.add(persona);
    }

    // la agencia crea sus salidas y les asigna el número siguiente
    public Salida programarSalida(LocalDate fechaInicio, LocalDate fechaFin, int cupoMinimo, int cupoMaximo, Tour tour) throws ReglaNegocioException {
        validarNoNulo(tour, "El tour es obligatorio.");
        if (!tours.contains(tour)) {
            throw new ReglaNegocioException("El tour no está registrado en la agencia.");
        }
        Salida salida = new Salida(proximoNumeroSalida(), fechaInicio, fechaFin, cupoMinimo, cupoMaximo, tour);
        salidas.add(salida);
        return salida;
    }

    // la agencia asigna el número y controla que existan el cliente y el vendedor;
    // la creación en sí la hace la salida, que es la que contiene a sus reservas
    public Reserva crearReserva(Salida salida, Cliente cliente, Vendedor vendedor, LocalDate fecha) throws ReglaNegocioException {
        validarNoNulo(salida, "La salida es obligatoria.");
        validarNoNulo(cliente, "El cliente es obligatorio.");
        validarNoNulo(vendedor, "El vendedor es obligatorio.");
        if (!salidas.contains(salida)) {
            throw new ReglaNegocioException("La salida no está registrada en la agencia.");
        }
        if (!clientes.contains(cliente)) {
            throw new ReglaNegocioException("El cliente no está registrado en la agencia.");
        }
        if (!personal.contains(vendedor)) {
            throw new ReglaNegocioException("El vendedor no está registrado en la agencia.");
        }
        return salida.crearReserva(proximoNumeroReserva(), fecha, cliente, vendedor);
    }

    // búsquedas por clave (si no existe, lanzan la excepción)
    public Tour buscarTour(String codigo) throws ReglaNegocioException {
        validarNoNulo(codigo, "El código es obligatorio.");
        for (Tour tour : tours) {
            if (tour.getCodigo().equals(codigo.strip())) {
                return tour;
            }
        }
        throw new ReglaNegocioException("No existe un tour con el código " + codigo.strip() + ".");
    }

    public ServicioAdicional buscarServicioAdicional(int codigo) throws ReglaNegocioException {
        for (ServicioAdicional servicio : serviciosAdicionales) {
            if (servicio.getCodigo() == codigo) {
                return servicio;
            }
        }
        throw new ReglaNegocioException("No existe un servicio adicional con el código " + codigo + ".");
    }

    public Proveedor buscarProveedor(String cuit) throws ReglaNegocioException {
        validarNoNulo(cuit, "El CUIT es obligatorio.");
        String buscado = limpiar(cuit);
        for (Proveedor proveedor : proveedores) {
            if (proveedor.getCuit().equals(buscado)) {
                return proveedor;
            }
        }
        throw new ReglaNegocioException("No existe un proveedor con el CUIT " + cuit.strip() + ".");
    }

    public Vehiculo buscarVehiculo(String patente) throws ReglaNegocioException {
        validarNoNulo(patente, "La patente es obligatoria.");
        String buscada = limpiar(patente);
        for (Vehiculo vehiculo : vehiculos) {
            if (vehiculo.getPatente().equals(buscada)) {
                return vehiculo;
            }
        }
        throw new ReglaNegocioException("No existe un vehículo con la patente " + patente.strip() + ".");
    }

    public Cliente buscarCliente(String documento) throws ReglaNegocioException {
        validarNoNulo(documento, "El documento es obligatorio.");
        String buscado = limpiar(documento);
        for (Cliente cliente : clientes) {
            if (cliente.getDocumento().equals(buscado)) {
                return cliente;
            }
        }
        throw new ReglaNegocioException("No existe un cliente con el documento " + documento.strip() + ".");
    }

    public Personal buscarPersonal(int legajo) throws ReglaNegocioException {
        for (Personal persona : personal) {
            if (persona.getLegajo() == legajo) {
                return persona;
            }
        }
        throw new ReglaNegocioException("No existe un empleado con el legajo " + legajo + ".");
    }

    public Salida buscarSalida(int numero) throws ReglaNegocioException {
        for (Salida salida : salidas) {
            if (salida.getNumero() == numero) {
                return salida;
            }
        }
        throw new ReglaNegocioException("No existe una salida con el número " + numero + ".");
    }

    public Reserva buscarReserva(int numero) throws ReglaNegocioException {
        for (Salida salida : salidas) {
            for (Reserva reserva : salida.getReservas()) {
                if (reserva.getNumero() == numero) {
                    return reserva;
                }
            }
        }
        throw new ReglaNegocioException("No existe una reserva con el número " + numero + ".");
    }

    // numeración: el número siguiente al más alto que ya existe
    public int proximoNumeroSalida() {
        int maximo = 0;
        for (Salida salida : salidas) {
            if (salida.getNumero() > maximo) {
                maximo = salida.getNumero();
            }
        }
        return maximo + 1;
    }

    public int proximoNumeroReserva() {
        int maximo = 0;
        for (Salida salida : salidas) {
            for (Reserva reserva : salida.getReservas()) {
                if (reserva.getNumero() > maximo) {
                    maximo = reserva.getNumero();
                }
            }
        }
        return maximo + 1;
    }

    public int proximoNumeroLiquidacion() {
        int maximo = 0;
        for (Salida salida : salidas) {
            if (salida.getLiquidacionGuia() != null && salida.getLiquidacionGuia().getNumero() > maximo) {
                maximo = salida.getLiquidacionGuia().getNumero();
            }
        }
        return maximo + 1;
    }

    // cambios en un tour (la agencia conoce las salidas, el tour no)
    // un tour con salidas programadas o en curso no se puede dar de baja ni cambiar de duración o cupos
    public boolean tieneSalidasVigentes(Tour tour) {
        for (Salida salida : salidas) {
            boolean vigente = salida.getEstado() != EstadoSalida.FINALIZADA
                    && salida.getEstado() != EstadoSalida.CANCELADA;
            if (vigente && salida.getTour().equals(tour)) {
                return true;
            }
        }
        return false;
    }

    public void darDeBajaTour(Tour tour) throws ReglaNegocioException {
        validarTourRegistrado(tour);
        validarSinSalidasVigentes(tour, "dar de baja");
        tour.darDeBaja();
    }

    public void modificarDuracionTour(Tour tour, int duracionDias) throws ReglaNegocioException {
        validarTourRegistrado(tour);
        validarSinSalidasVigentes(tour, "cambiar la duración de");
        tour.setDuracionDias(duracionDias);
    }

    public void modificarCuposTour(Tour tour, int cupoMinimo, int cupoMaximo) throws ReglaNegocioException {
        validarTourRegistrado(tour);
        validarSinSalidasVigentes(tour, "cambiar los cupos de");
        tour.setCupos(cupoMinimo, cupoMaximo);
    }

    // un vehículo asignado a salidas programadas o en curso no puede quedar con capacidad menor a su cupo
    public void modificarCapacidadVehiculo(Vehiculo vehiculo, int capacidad) throws ReglaNegocioException {
        validarNoNulo(vehiculo, "El vehículo es obligatorio.");
        if (!vehiculos.contains(vehiculo)) {
            throw new ReglaNegocioException("El vehículo no está registrado en la agencia.");
        }
        for (Salida salida : salidas) {
            boolean vigente = salida.getEstado() != EstadoSalida.FINALIZADA && salida.getEstado() != EstadoSalida.CANCELADA;
            if (vigente && vehiculo.equals(salida.getVehiculo()) && salida.getCupoMaximo() > capacidad) {
                throw new ReglaNegocioException("El vehículo está asignado a la salida N° " + salida.getNumero() + ", que necesita " + salida.getCupoMaximo() + " lugares.");
            }
        }
        vehiculo.setCapacidad(capacidad);
    }

    // un guía no puede perder una categoría si tiene salidas programadas o en curso de esa categoría
    public void quitarCategoriaGuia(Guia guia, CategoriaTour categoria) throws ReglaNegocioException {
        validarNoNulo(guia, "El guía es obligatorio.");
        if (!personal.contains(guia)) {
            throw new ReglaNegocioException("El guía no está registrado en la agencia.");
        }
        for (Salida salida : salidas) {
            boolean vigente = salida.getEstado() != EstadoSalida.FINALIZADA && salida.getEstado() != EstadoSalida.CANCELADA;
            if (vigente && guia.equals(salida.getGuia()) && salida.getTour().getCategoria() == categoria) {
                throw new ReglaNegocioException("El guía tiene asignada la salida N° " + salida.getNumero() + " de la categoría " + categoria + ".");
            }
        }
        guia.quitarCategoria(categoria);
    }

    // controles periódicos: el programa los dispara al arrancar y antes de operar
    // anula las reservas que no pagaron el saldo a tiempo y devuelve las que anuló
    public List<Reserva> verificarPlazosPago(LocalDate hoy) throws ReglaNegocioException {
        List<Reserva> anuladas = new ArrayList<>();
        for (Salida salida : salidas) {
            for (Reserva reserva : salida.getReservas()) {
                if (reserva.verificarPlazoPago(hoy)) {
                    anuladas.add(reserva);
                }
            }
        }
        return anuladas;
    }

    // cancela las salidas que a 7 días no alcanzaron el cupo mínimo y devuelve las que canceló
    public List<Salida> verificarCuposMinimos(LocalDate hoy) throws ReglaNegocioException {
        List<Salida> canceladas = new ArrayList<>();
        for (Salida salida : salidas) {
            if (salida.cancelarSiNoAlcanzaMinimo(hoy)) {
                canceladas.add(salida);
            }
        }
        return canceladas;
    }

    // saca puntos, guiones y espacios y pasa a mayúsculas, igual que las entidades al guardar sus claves
    private String limpiar(String clave) {
        return clave.replace(".", "").replace("-", "").replace(" ", "").toUpperCase();
    }

    private void validarTourRegistrado(Tour tour) throws ReglaNegocioException {
        validarNoNulo(tour, "El tour es obligatorio.");
        if (!tours.contains(tour)) {
            throw new ReglaNegocioException("El tour no está registrado en la agencia.");
        }
    }

    private void validarSinSalidasVigentes(Tour tour, String accion) throws ReglaNegocioException {
        if (tieneSalidasVigentes(tour)) {
            throw new ReglaNegocioException("No se puede " + accion + " un tour con salidas programadas o en curso.");
        }
    }

    private void validarNoNulo(Object objeto, String mensaje) {
        if (objeto == null) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}
