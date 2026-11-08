package controlador;

import excepcion.ReglaNegocioException;
import modelo.Actividad;
import modelo.Administrador;
import modelo.CoordinadorOperaciones;
import modelo.Guia;
import modelo.Personal;
import modelo.Proveedor;
import modelo.ServicioAdicional;
import modelo.ServicioTour;
import modelo.Tarifa;
import modelo.Tour;
import modelo.TourAventura;
import modelo.Vehiculo;
import modelo.Vendedor;
import modelo.enums.CategoriaTour;
import modelo.enums.Temporada;
import modelo.enums.TipoActividad;
import modelo.enums.TipoServicioAdicional;
import modelo.enums.TipoVehiculo;
import servicio.Agencia;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

// Casos de uso del administrador sobre el catálogo: CU01 (tours), CU02 (itinerario), CU03 (proveedores), CU04 (tarifas), CU05 (servicios adicionales), CU06 (vehículos) y CU07 (personal).
public class ControladorCatalogo {
    private final Agencia agencia;
    private final Pantalla pantalla;

    public ControladorCatalogo(Agencia agencia, Pantalla pantalla) {
        if (agencia == null || pantalla == null) {
            throw new IllegalArgumentException("La agencia y la pantalla son obligatorias.");
        }
        this.agencia = agencia;
        this.pantalla = pantalla;
    }

    public List<CategoriaTour> getCategoriasDeTour() {
        return List.of(CategoriaTour.values());
    }

    // las categorías de los tours que se registran con registrarTour: los de aventura tienen su propio alta
    public List<CategoriaTour> getCategoriasDeTourComun() {
        return List.of(CategoriaTour.CULTURAL, CategoriaTour.NATURALEZA);
    }

    public List<Temporada> getTemporadas() {
        return List.of(Temporada.values());
    }

    public List<TipoActividad> getTiposDeActividad() {
        return List.of(TipoActividad.values());
    }

    public List<TipoServicioAdicional> getTiposDeServicio() {
        return List.of(TipoServicioAdicional.values());
    }

    public List<TipoVehiculo> getTiposDeVehiculo() {
        return List.of(TipoVehiculo.values());
    }

    public List<Tour> getTours() {
        List<Tour> tours = agencia.getTours();
        if (tours.isEmpty()) {
            pantalla.mostrarError("No hay tours cargados.");
            return null;
        }
        return tours;
    }

    // las actividades del tour, ordenadas por día y orden (se eligen por su posición, desde 1)
    public List<Actividad> getActividades(String codigoTour) {
        try {
            List<Actividad> actividades = agencia.buscarTour(codigoTour).getActividades();
            if (actividades.isEmpty()) {
                throw new ReglaNegocioException("El tour no tiene actividades.");
            }
            return actividades;
        } catch (ReglaNegocioException e) {
            pantalla.mostrarError(e.getMessage());
            return null;
        }
    }

    // las tarifas del tour (se eligen por su posición, desde 1)
    public List<Tarifa> getTarifas(String codigoTour) {
        try {
            List<Tarifa> tarifas = agencia.buscarTour(codigoTour).getTarifas();
            if (tarifas.isEmpty()) {
                throw new ReglaNegocioException("El tour no tiene tarifas.");
            }
            return tarifas;
        } catch (ReglaNegocioException e) {
            pantalla.mostrarError(e.getMessage());
            return null;
        }
    }

    // los servicios adicionales que ofrece el tour
    public List<ServicioTour> getServiciosDelTour(String codigoTour) {
        try {
            List<ServicioTour> servicios = agencia.buscarTour(codigoTour).getServicios();
            if (servicios.isEmpty()) {
                throw new ReglaNegocioException("El tour no ofrece servicios adicionales.");
            }
            return servicios;
        } catch (ReglaNegocioException e) {
            pantalla.mostrarError(e.getMessage());
            return null;
        }
    }

    // los tours que tienen salidas programadas o en curso: esos no se pueden dar de baja ni cambiar de duración o de cupos
    public List<Tour> getToursConSalidasVigentes() {
        List<Tour> conSalidas = new ArrayList<>();
        for (Tour tour : agencia.getTours()) {
            if (agencia.tieneSalidasVigentes(tour)) {
                conSalidas.add(tour);
            }
        }
        return conSalidas;
    }

    public List<Proveedor> getProveedores() {
        List<Proveedor> proveedores = agencia.getProveedores();
        if (proveedores.isEmpty()) {
            pantalla.mostrarError("No hay proveedores cargados.");
            return null;
        }
        return proveedores;
    }

    public List<ServicioAdicional> getServiciosAdicionales() {
        List<ServicioAdicional> servicios = agencia.getServiciosAdicionales();
        if (servicios.isEmpty()) {
            pantalla.mostrarError("No hay servicios adicionales cargados.");
            return null;
        }
        return servicios;
    }

    public List<Vehiculo> getVehiculos() {
        List<Vehiculo> vehiculos = agencia.getVehiculos();
        if (vehiculos.isEmpty()) {
            pantalla.mostrarError("No hay vehículos cargados.");
            return null;
        }
        return vehiculos;
    }

    public List<Personal> getPersonal() {
        List<Personal> personal = agencia.getPersonal();
        if (personal.isEmpty()) {
            pantalla.mostrarError("No hay personal cargado.");
            return null;
        }
        return personal;
    }

    public List<Guia> getGuias() {
        List<Guia> guias = agencia.getGuias();
        if (guias.isEmpty()) {
            pantalla.mostrarError("No hay guías cargados.");
            return null;
        }
        return guias;
    }

    public List<Vendedor> getVendedores() {
        List<Vendedor> vendedores = agencia.getVendedores();
        if (vendedores.isEmpty()) {
            pantalla.mostrarError("No hay vendedores cargados.");
            return null;
        }
        return vendedores;
    }

    public List<Personal> getCoordinadores() {
        List<Personal> coordinadores = new ArrayList<>();
        for (Personal persona : agencia.getPersonal()) {
            if (persona instanceof CoordinadorOperaciones) {
                coordinadores.add(persona);
            }
        }
        if (coordinadores.isEmpty()) {
            pantalla.mostrarError("No hay coordinadores cargados.");
            return null;
        }
        return coordinadores;
    }

    public List<Personal> getAdministradores() {
        List<Personal> administradores = new ArrayList<>();
        for (Personal persona : agencia.getPersonal()) {
            if (persona instanceof Administrador) {
                administradores.add(persona);
            }
        }
        if (administradores.isEmpty()) {
            pantalla.mostrarError("No hay administradores cargados.");
            return null;
        }
        return administradores;
    }

    // ---- CU01: tours ----
    // para tours culturales y de naturaleza
    public boolean registrarTour(Personal usuario, String codigo, String nombre, String descripcion, int duracionDias, CategoriaTour categoria, int cupoMinimo, int cupoMaximo) {
        try {
            validarAdministrador(usuario);
            agencia.registrarTour(new Tour(codigo, nombre, descripcion, duracionDias, categoria, cupoMinimo, cupoMaximo));
            pantalla.mostrarMensaje("Tour registrado.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    // los tours de aventura necesitan además edad mínima y nivel de dificultad (RN02)
    public boolean registrarTourAventura(Personal usuario, String codigo, String nombre, String descripcion, int duracionDias, int cupoMinimo, int cupoMaximo, int edadMinima, String nivelDificultad) {
        try {
            validarAdministrador(usuario);
            agencia.registrarTour(new TourAventura(codigo, nombre, descripcion, duracionDias, cupoMinimo,
                    cupoMaximo, edadMinima, nivelDificultad));
            pantalla.mostrarMensaje("Tour de aventura registrado.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean modificarDatosTour(Personal usuario, String codigoTour, String nombre, String descripcion) {
        try {
            Tour tour = tourDe(usuario, codigoTour);
            // objeto descartable solo para validar el nombre y la descripción (la categoría no importa acá)
            new Tour(tour.getCodigo(), nombre, descripcion, tour.getDuracionDias(), CategoriaTour.CULTURAL,
                    tour.getCupoMinimo(), tour.getCupoMaximo());
            tour.setNombre(nombre);
            tour.setDescripcion(descripcion);
            pantalla.mostrarMensaje("Datos del tour modificados.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean modificarDuracionTour(Personal usuario, String codigoTour, int duracionDias) {
        try {
            agencia.modificarDuracionTour(tourDe(usuario, codigoTour), duracionDias);
            pantalla.mostrarMensaje("Duración del tour modificada.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean modificarCuposTour(Personal usuario, String codigoTour, int cupoMinimo, int cupoMaximo) {
        try {
            agencia.modificarCuposTour(tourDe(usuario, codigoTour), cupoMinimo, cupoMaximo);
            pantalla.mostrarMensaje("Cupos del tour modificados.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean modificarCondicionesAventura(Personal usuario, String codigoTour, int edadMinima, String nivelDificultad) {
        try {
            Tour tour = tourDe(usuario, codigoTour);
            if (!(tour instanceof TourAventura)) {
                throw new ReglaNegocioException("El tour " + codigoTour + " no es de aventura.");
            }
            validarSinSalidasVigentes(tour, "cambiar las condiciones de");
            // objeto descartable solo para validar la edad y el nivel antes de modificar
            new TourAventura(tour.getCodigo(), tour.getNombre(), tour.getDescripcion(), tour.getDuracionDias(),
                    tour.getCupoMinimo(), tour.getCupoMaximo(), edadMinima, nivelDificultad);
            TourAventura aventura = (TourAventura) tour;
            aventura.setEdadMinima(edadMinima);
            aventura.setNivelDificultad(nivelDificultad);
            pantalla.mostrarMensaje("Condiciones del tour modificadas.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean darDeBajaTour(Personal usuario, String codigoTour) {
        try {
            agencia.darDeBajaTour(tourDe(usuario, codigoTour));
            pantalla.mostrarMensaje("El tour fue dado de baja.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    // ---- CU02: itinerario (las actividades se eligen por su posición en la lista ordenada, desde 1) ----
    // cuitProveedor puede venir vacío si la actividad no incluye servicios de terceros
    public boolean agregarActividad(Personal usuario, String codigoTour, int dia, int orden, TipoActividad tipo, LocalTime horario, String descripcion, String lugar, String cuitProveedor) {
        try {
            Tour tour = tourDe(usuario, codigoTour);
            Proveedor proveedor = null;
            if (cuitProveedor != null && !cuitProveedor.isBlank()) {
                proveedor = agencia.buscarProveedor(cuitProveedor);   // primero: si no existe, no se agrega la actividad
            }
            Actividad actividad = tour.agregarActividad(dia, orden, tipo, horario, descripcion, lugar);
            actividad.setProveedor(proveedor);
            pantalla.mostrarMensaje("Actividad agregada.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean modificarHorarioActividad(Personal usuario, String codigoTour, int numeroActividad, LocalTime horario) {
        try {
            Tour tour = tourDe(usuario, codigoTour);
            tour.modificarHorario(actividadDe(tour, numeroActividad), horario);
            pantalla.mostrarMensaje("Horario de la actividad modificado.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean modificarOrdenActividad(Personal usuario, String codigoTour, int numeroActividad, int orden) {
        try {
            Tour tour = tourDe(usuario, codigoTour);
            tour.modificarOrden(actividadDe(tour, numeroActividad), orden);
            pantalla.mostrarMensaje("Orden de la actividad modificado.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean quitarActividad(Personal usuario, String codigoTour, int numeroActividad) {
        try {
            Tour tour = tourDe(usuario, codigoTour);
            tour.quitarActividad(actividadDe(tour, numeroActividad));
            pantalla.mostrarMensaje("Actividad quitada.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    // ---- CU03: proveedores (y su asociación a las actividades) ----
    public boolean registrarProveedor(Personal usuario, String nombre, String cuit, String contacto, String telefono, String servicioPrestado) {
        try {
            validarAdministrador(usuario);
            agencia.registrarProveedor(new Proveedor(nombre, cuit, contacto, telefono, servicioPrestado));
            pantalla.mostrarMensaje("Proveedor registrado.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean modificarProveedor(Personal usuario, String cuit, String nombre, String contacto, String telefono, String servicioPrestado) {
        try {
            validarAdministrador(usuario);
            Proveedor proveedor = agencia.buscarProveedor(cuit);
            new Proveedor(nombre, proveedor.getCuit(), contacto, telefono, servicioPrestado);   // valida todo antes
            proveedor.setNombre(nombre);
            proveedor.setContacto(contacto);
            proveedor.setTelefono(telefono);
            proveedor.setServicioPrestado(servicioPrestado);
            pantalla.mostrarMensaje("Proveedor modificado.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean asociarProveedor(Personal usuario, String codigoTour, int numeroActividad, String cuitProveedor) {
        try {
            Tour tour = tourDe(usuario, codigoTour);
            Proveedor proveedor = agencia.buscarProveedor(cuitProveedor);
            actividadDe(tour, numeroActividad).setProveedor(proveedor);
            pantalla.mostrarMensaje("Proveedor asociado a la actividad.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean quitarProveedorDeActividad(Personal usuario, String codigoTour, int numeroActividad) {
        try {
            Tour tour = tourDe(usuario, codigoTour);
            actividadDe(tour, numeroActividad).setProveedor(null);
            pantalla.mostrarMensaje("Se quitó el proveedor de la actividad.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    // ---- CU04: tarifas (se eligen por su posición en la lista del tour, desde 1) ----
    public boolean agregarTarifa(Personal usuario, String codigoTour, Temporada temporada, LocalDate fechaDesde, LocalDate fechaHasta, double precioBase) {
        try {
            tourDe(usuario, codigoTour).agregarTarifa(temporada, fechaDesde, fechaHasta, precioBase);
            pantalla.mostrarMensaje("Tarifa agregada.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    // las reservas ya hechas conservan el precio que tenían (RF04)
    public boolean modificarPrecioTarifa(Personal usuario, String codigoTour, int numeroTarifa, double precioBase) {
        try {
            Tour tour = tourDe(usuario, codigoTour);
            tarifaDe(tour, numeroTarifa).setPrecioBase(precioBase);
            pantalla.mostrarMensaje("Precio de la tarifa modificado.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean modificarFechasTarifa(Personal usuario, String codigoTour, int numeroTarifa, LocalDate fechaDesde, LocalDate fechaHasta) {
        try {
            Tour tour = tourDe(usuario, codigoTour);
            tour.modificarFechasTarifa(tarifaDe(tour, numeroTarifa), fechaDesde, fechaHasta);
            pantalla.mostrarMensaje("Fechas de la tarifa modificadas.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    // ---- CU05: servicios adicionales (y su oferta en cada tour) ----
    public boolean registrarServicioAdicional(Personal usuario, int codigo, String nombre, String descripcion, TipoServicioAdicional tipo, double precio) {
        try {
            validarAdministrador(usuario);
            agencia.registrarServicioAdicional(new ServicioAdicional(codigo, nombre, descripcion, tipo, precio));
            pantalla.mostrarMensaje("Servicio adicional registrado.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean modificarServicioAdicional(Personal usuario, int codigo, String nombre, String descripcion, double precio) {
        try {
            validarAdministrador(usuario);
            ServicioAdicional servicio = agencia.buscarServicioAdicional(codigo);
            new ServicioAdicional(servicio.getCodigo(), nombre, descripcion, servicio.getTipo(), precio);   // valida todo antes
            servicio.setNombre(nombre);
            servicio.setDescripcion(descripcion);
            servicio.setPrecio(precio);   // los servicios ya contratados conservan su precio
            pantalla.mostrarMensaje("Servicio adicional modificado.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean ofrecerServicioEnTour(Personal usuario, String codigoTour, int codigoServicio, boolean obligatorio) {
        try {
            Tour tour = tourDe(usuario, codigoTour);
            ServicioAdicional servicio = agencia.buscarServicioAdicional(codigoServicio);
            if (obligatorio) {
                validarSinSalidasVigentes(tour, "agregar un servicio obligatorio a");
            }
            tour.agregarServicio(servicio, obligatorio);
            pantalla.mostrarMensaje("Servicio agregado al tour.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean cambiarObligatoriedadServicio(Personal usuario, String codigoTour, int codigoServicio, boolean obligatorio) {
        try {
            Tour tour = tourDe(usuario, codigoTour);
            ServicioAdicional servicio = agencia.buscarServicioAdicional(codigoServicio);
            if (obligatorio) {
                validarSinSalidasVigentes(tour, "volver obligatorio un servicio de");
            }
            tour.modificarObligatorio(servicio, obligatorio);
            pantalla.mostrarMensaje("Se cambió si el servicio es obligatorio.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean quitarServicioDeTour(Personal usuario, String codigoTour, int codigoServicio) {
        try {
            Tour tour = tourDe(usuario, codigoTour);
            tour.quitarServicio(agencia.buscarServicioAdicional(codigoServicio));
            pantalla.mostrarMensaje("Servicio quitado del tour.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    // ---- CU06: vehículos ----
    public boolean registrarVehiculo(Personal usuario, String patente, TipoVehiculo tipo, int capacidad) {
        try {
            validarAdministrador(usuario);
            agencia.registrarVehiculo(new Vehiculo(patente, tipo, capacidad));
            pantalla.mostrarMensaje("Vehículo registrado.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean modificarVehiculo(Personal usuario, String patente, TipoVehiculo tipo, int capacidad) {
        try {
            validarAdministrador(usuario);
            Vehiculo vehiculo = agencia.buscarVehiculo(patente);
            new Vehiculo(vehiculo.getPatente(), tipo, capacidad);          // valida todo antes de modificar
            agencia.modificarCapacidadVehiculo(vehiculo, capacidad);       // controla las salidas que lo usan (RN26)
            vehiculo.setTipo(tipo);
            pantalla.mostrarMensaje("Vehículo modificado.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    // ---- CU07: personal ----
    public boolean registrarVendedor(Personal usuario, int legajo, String nombre, String documento, String telefono, double comision) {
        try {
            validarAdministrador(usuario);
            agencia.registrarPersonal(new Vendedor(legajo, nombre, documento, telefono, comision));
            pantalla.mostrarMensaje("Vendedor registrado.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean registrarGuia(Personal usuario, int legajo, String nombre, String documento, String telefono, double honorarioPorDia, String idioma, CategoriaTour categoria) {
        try {
            validarAdministrador(usuario);
            agencia.registrarPersonal(new Guia(legajo, nombre, documento, telefono, honorarioPorDia, idioma, categoria));
            pantalla.mostrarMensaje("Guía registrado.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean registrarCoordinador(Personal usuario, int legajo, String nombre, String documento, String telefono) {
        try {
            validarAdministrador(usuario);
            agencia.registrarPersonal(new CoordinadorOperaciones(legajo, nombre, documento, telefono));
            pantalla.mostrarMensaje("Coordinador registrado.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean registrarAdministrador(Personal usuario, int legajo, String nombre, String documento, String telefono) {
        try {
            validarAdministrador(usuario);
            agencia.registrarPersonal(new Administrador(legajo, nombre, documento, telefono));
            pantalla.mostrarMensaje("Administrador registrado.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean modificarDatosPersonal(Personal usuario, int legajo, String nombre, String documento, String telefono) {
        try {
            validarAdministrador(usuario);
            Personal persona = agencia.buscarPersonal(legajo);
            new Administrador(legajo, nombre, documento, telefono);   // objeto descartable: valida los tres datos antes
            persona.setNombre(nombre);
            persona.setDocumento(documento);
            persona.setTelefono(telefono);
            pantalla.mostrarMensaje("Datos del empleado modificados.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean modificarComision(Personal usuario, int legajoVendedor, double comision) {
        try {
            vendedorDe(usuario, legajoVendedor).setComision(comision);
            pantalla.mostrarMensaje("Comisión modificada.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean modificarHonorarioGuia(Personal usuario, int legajoGuia, double honorarioPorDia) {
        try {
            guiaDe(usuario, legajoGuia).setHonorarioPorDia(honorarioPorDia);
            pantalla.mostrarMensaje("Honorario del guía modificado.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean agregarIdiomaGuia(Personal usuario, int legajoGuia, String idioma) {
        try {
            guiaDe(usuario, legajoGuia).agregarIdioma(idioma);
            pantalla.mostrarMensaje("Idioma agregado.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean quitarIdiomaGuia(Personal usuario, int legajoGuia, String idioma) {
        try {
            guiaDe(usuario, legajoGuia).quitarIdioma(idioma);
            pantalla.mostrarMensaje("Idioma quitado.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean agregarCategoriaGuia(Personal usuario, int legajoGuia, CategoriaTour categoria) {
        try {
            guiaDe(usuario, legajoGuia).agregarCategoria(categoria);
            pantalla.mostrarMensaje("Categoría agregada.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    public boolean quitarCategoriaGuia(Personal usuario, int legajoGuia, CategoriaTour categoria) {
        try {
            agencia.quitarCategoriaGuia(guiaDe(usuario, legajoGuia), categoria);
            pantalla.mostrarMensaje("Categoría quitada.");
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException e) {
            pantalla.mostrarError(e.getMessage());
            return false;
        }
    }

    private void validarAdministrador(Personal usuario) throws ReglaNegocioException {
        if (!(usuario instanceof Administrador)) {
            throw new ReglaNegocioException("Solo el administrador puede realizar esta operación.");
        }
    }

    // controla el rol y busca el tour
    private Tour tourDe(Personal usuario, String codigoTour) throws ReglaNegocioException {
        validarAdministrador(usuario);
        return agencia.buscarTour(codigoTour);
    }

    private Guia guiaDe(Personal usuario, int legajo) throws ReglaNegocioException {
        validarAdministrador(usuario);
        Personal persona = agencia.buscarPersonal(legajo);
        if (!(persona instanceof Guia)) {
            throw new ReglaNegocioException("El legajo " + legajo + " no corresponde a un guía.");
        }
        return (Guia) persona;
    }

    private Vendedor vendedorDe(Personal usuario, int legajo) throws ReglaNegocioException {
        validarAdministrador(usuario);
        Personal persona = agencia.buscarPersonal(legajo);
        if (!(persona instanceof Vendedor)) {
            throw new ReglaNegocioException("El legajo " + legajo + " no corresponde a un vendedor.");
        }
        return (Vendedor) persona;
    }

    private Actividad actividadDe(Tour tour, int numeroActividad) throws ReglaNegocioException {
        List<Actividad> actividades = tour.getActividades();   // ordenadas por día y orden
        if (numeroActividad < 1 || numeroActividad > actividades.size()) {
            throw new ReglaNegocioException("El tour no tiene una actividad N° " + numeroActividad + ".");
        }
        return actividades.get(numeroActividad - 1);
    }

    private Tarifa tarifaDe(Tour tour, int numeroTarifa) throws ReglaNegocioException {
        List<Tarifa> tarifas = tour.getTarifas();
        if (numeroTarifa < 1 || numeroTarifa > tarifas.size()) {
            throw new ReglaNegocioException("El tour no tiene una tarifa N° " + numeroTarifa + ".");
        }
        return tarifas.get(numeroTarifa - 1);
    }

    private void validarSinSalidasVigentes(Tour tour, String accion) throws ReglaNegocioException {
        if (agencia.tieneSalidasVigentes(tour)) {
            throw new ReglaNegocioException("No se puede " + accion + " un tour con salidas programadas o en curso.");
        }
    }
}