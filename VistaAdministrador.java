package vista;

import controlador.ControladorAdministracion;
import controlador.ControladorCatalogo;
import modelo.Actividad;
import modelo.Guia;
import modelo.LiquidacionGuia;
import modelo.Personal;
import modelo.Proveedor;
import modelo.Reserva;
import modelo.Salida;
import modelo.ServicioAdicional;
import modelo.ServicioTour;
import modelo.Tarifa;
import modelo.Tour;
import modelo.Vehiculo;
import modelo.Vendedor;
import modelo.enums.CategoriaTour;
import modelo.enums.Temporada;
import modelo.enums.TipoActividad;
import modelo.enums.TipoServicioAdicional;
import modelo.enums.TipoVehiculo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// Menú del administrador: catálogo (CU01 tours, CU02 itinerario, CU03 proveedores, CU04 tarifas, CU05 servicios adicionales, CU06 vehículos, CU07 personal) y administración (CU08 liquidación, CU09 reportes).
public class VistaAdministrador {
    private final Consola consola;
    private final Listados listados;
    private final ControladorCatalogo catalogo;
    private final ControladorAdministracion administracion;
    private final Personal usuario;
    private final LocalDate hoy;
    private final Runnable guardar;

    public VistaAdministrador(Consola consola, ControladorCatalogo catalogo, ControladorAdministracion administracion, Personal usuario, LocalDate hoy, Runnable guardar) {
        this.consola = consola;
        this.listados = new Listados(consola);
        this.catalogo = catalogo;
        this.administracion = administracion;
        this.usuario = usuario;
        this.hoy = hoy;
        this.guardar = guardar;
    }

    public void ejecutar() {
        int opcion;
        do {
            opcion = consola.menu("👑 Menú del administrador", new String[]{
                    "🧳 Tours",
                    "📍 Itinerario de un tour",
                    "🤝 Proveedores",
                    "💲 Tarifas de un tour",
                    "🎒 Servicios adicionales",
                    "🚌 Vehículos",
                    "👥 Personal",
                    "💰 Liquidar los honorarios de un guía",
                    "📊 Reportes"}, "🚪 Cerrar sesión");
            if (opcion == 1) {
                menuTours();
            } else if (opcion == 2) {
                menuItinerario();
            } else if (opcion == 3) {
                menuProveedores();
            } else if (opcion == 4) {
                menuTarifas();
            } else if (opcion == 5) {
                menuServicios();
            } else if (opcion == 6) {
                menuVehiculos();
            } else if (opcion == 7) {
                menuPersonal();
            } else if (opcion == 8) {
                liquidarHonorarios();
            } else if (opcion == 9) {
                menuReportes();
            }
        } while (opcion != 0);
    }

    // =====================================================================
    // CU01: tours
    // =====================================================================

    private void menuTours() {
        int opcion;
        do {
            opcion = consola.menu("Tours", new String[]{
                    "Ver los tours",
                    "Registrar un nuevo tour",
                    "Modificar un tour",
                    "Dar de baja un tour"}, "Volver");
            if (opcion == 1) {
                verTours();
            } else if (opcion == 2) {
                menuRegistrarTour();
            } else if (opcion == 3) {
                menuModificarTour();
            } else if (opcion == 4) {
                darDeBajaTour();
            }
        } while (opcion != 0);
    }

    private void verTours() {
        List<Tour> tours = catalogo.getTours();
        if (tours != null) {
            consola.subtitulo("Tours");
            listados.mostrarTours(tours, catalogo.getToursConSalidasVigentes());
        }
    }

    // se elige el tipo de tour y se lo registra
    private void menuRegistrarTour() {
        int opcion = consola.menu("Registrar un nuevo tour", new String[]{
                "Tour cultural o de naturaleza",
                "Tour de aventura"}, "Volver");
        if (opcion == 1) {
            registrarTour();
        } else if (opcion == 2) {
            registrarTourAventura();
        }
    }

    private void registrarTour() {
        consola.subtitulo("Registrar un tour");
        String codigo = consola.leerTexto("Código");
        String nombre = consola.leerTexto("Nombre");
        String descripcion = consola.leerTexto("Descripción");
        int duracion = consola.leerEntero("Duración en días");
        List<CategoriaTour> categorias = catalogo.getCategoriasDeTourComun();
        CategoriaTour categoria = categorias.get(consola.elegirPosicion("Categoría", categorias));
        int cupoMinimo = consola.leerEntero("Cupo mínimo");
        int cupoMaximo = consola.leerEntero("Cupo máximo");
        if (catalogo.registrarTour(usuario, codigo, nombre, descripcion, duracion, categoria, cupoMinimo, cupoMaximo)) {
            guardar.run();
        }
    }

    private void registrarTourAventura() {
        consola.subtitulo("Registrar un tour de aventura");
        String codigo = consola.leerTexto("Código");
        String nombre = consola.leerTexto("Nombre");
        String descripcion = consola.leerTexto("Descripción");
        int duracion = consola.leerEntero("Duración en días");
        int cupoMinimo = consola.leerEntero("Cupo mínimo");
        int cupoMaximo = consola.leerEntero("Cupo máximo");
        int edadMinima = consola.leerEntero("Edad mínima");
        String nivel = consola.leerTexto("Nivel de dificultad");
        if (catalogo.registrarTourAventura(usuario, codigo, nombre, descripcion, duracion, cupoMinimo, cupoMaximo,
                edadMinima, nivel)) {
            guardar.run();
        }
    }

    // se elige el tour una sola vez y después se pueden hacer varios cambios
    private void menuModificarTour() {
        consola.subtitulo("Modificar un tour");
        Tour tour = elegirTour();
        if (tour == null) {
            return;
        }
        int opcion;
        do {
            opcion = consola.menu("Modificar " + tour.getNombre(), new String[]{
                    "El nombre y la descripción",
                    "La duración",
                    "Los cupos",
                    "Las condiciones de un tour de aventura"}, "Volver");
            if (opcion == 1) {
                modificarDatosTour(tour);
            } else if (opcion == 2) {
                modificarDuracionTour(tour);
            } else if (opcion == 3) {
                modificarCuposTour(tour);
            } else if (opcion == 4) {
                modificarCondicionesAventura(tour);
            }
        } while (opcion != 0);
    }

    private void modificarDatosTour(Tour tour) {
        consola.subtitulo("Modificar el nombre y la descripción");
        String nombre = consola.leerTexto("Nombre nuevo");
        String descripcion = consola.leerTexto("Descripción nueva");
        if (catalogo.modificarDatosTour(usuario, tour.getCodigo(), nombre, descripcion)) {
            guardar.run();
        }
    }

    private void modificarDuracionTour(Tour tour) {
        consola.subtitulo("Modificar la duración");
        int duracion = consola.leerEntero("Duración nueva en días");
        if (catalogo.modificarDuracionTour(usuario, tour.getCodigo(), duracion)) {
            guardar.run();
        }
    }

    private void modificarCuposTour(Tour tour) {
        consola.subtitulo("Modificar los cupos");
        int cupoMinimo = consola.leerEntero("Cupo mínimo nuevo");
        int cupoMaximo = consola.leerEntero("Cupo máximo nuevo");
        if (catalogo.modificarCuposTour(usuario, tour.getCodigo(), cupoMinimo, cupoMaximo)) {
            guardar.run();
        }
    }

    private void modificarCondicionesAventura(Tour tour) {
        consola.subtitulo("Modificar las condiciones de un tour de aventura");
        int edadMinima = consola.leerEntero("Edad mínima nueva");
        String nivel = consola.leerTexto("Nivel de dificultad nuevo");
        if (catalogo.modificarCondicionesAventura(usuario, tour.getCodigo(), edadMinima, nivel)) {
            guardar.run();
        }
    }

    private void darDeBajaTour() {
        consola.subtitulo("Dar de baja un tour");
        Tour tour = elegirTour();
        if (tour == null) {
            return;
        }
        if (!consola.confirmar("¿Dar de baja el tour " + tour.getNombre() + "?")) {
            consola.info("Operación cancelada.");
            return;
        }
        if (catalogo.darDeBajaTour(usuario, tour.getCodigo())) {
            guardar.run();
        }
    }

    // =====================================================================
    // CU02: itinerario (se elige el tour y después se trabaja con sus actividades)
    // =====================================================================

    private void menuItinerario() {
        consola.subtitulo("Itinerario de un tour");
        Tour tour = elegirTour();
        if (tour == null) {
            return;
        }
        int opcion;
        do {
            opcion = consola.menu("Itinerario de " + tour.getNombre(), new String[]{
                    "Ver las actividades",
                    "Agregar una actividad",
                    "Modificar el horario de una actividad",
                    "Modificar el orden de una actividad",
                    "Quitar una actividad"}, "Volver");
            if (opcion == 1) {
                verActividades(tour);
            } else if (opcion == 2) {
                agregarActividad(tour);
            } else if (opcion == 3) {
                modificarHorarioActividad(tour);
            } else if (opcion == 4) {
                modificarOrdenActividad(tour);
            } else if (opcion == 5) {
                quitarActividad(tour);
            }
        } while (opcion != 0);
    }

    private void verActividades(Tour tour) {
        List<Actividad> actividades = catalogo.getActividades(tour.getCodigo());
        if (actividades != null) {
            consola.subtitulo("Actividades de " + tour.getNombre());
            listados.mostrarLista(textosDeActividades(actividades));
        }
    }

    private void agregarActividad(Tour tour) {
        consola.subtitulo("Agregar una actividad");
        int dia = consola.leerEntero("Día del tour");
        int orden = consola.leerEntero("Orden dentro del día");
        List<TipoActividad> tipos = catalogo.getTiposDeActividad();
        TipoActividad tipo = tipos.get(consola.elegirPosicion("Tipo de actividad", tipos));
        LocalTime horario = consola.leerHora("Horario");
        String descripcion = consola.leerTexto("Descripción");
        String lugar = consola.leerTexto("Lugar");
        String cuit = consola.leerTextoOpcional("CUIT del proveedor");
        if (catalogo.agregarActividad(usuario, tour.getCodigo(), dia, orden, tipo, horario, descripcion, lugar, cuit)) {
            guardar.run();
        }
    }

    private void modificarHorarioActividad(Tour tour) {
        consola.subtitulo("Modificar el horario de una actividad");
        int numero = elegirActividad(tour);
        if (numero == 0) {
            return;
        }
        LocalTime horario = consola.leerHora("Horario nuevo");
        if (catalogo.modificarHorarioActividad(usuario, tour.getCodigo(), numero, horario)) {
            guardar.run();
        }
    }

    private void modificarOrdenActividad(Tour tour) {
        consola.subtitulo("Modificar el orden de una actividad");
        int numero = elegirActividad(tour);
        if (numero == 0) {
            return;
        }
        int orden = consola.leerEntero("Orden nuevo dentro del día");
        if (catalogo.modificarOrdenActividad(usuario, tour.getCodigo(), numero, orden)) {
            guardar.run();
        }
    }

    private void quitarActividad(Tour tour) {
        consola.subtitulo("Quitar una actividad");
        int numero = elegirActividad(tour);
        if (numero == 0) {
            return;
        }
        if (catalogo.quitarActividad(usuario, tour.getCodigo(), numero)) {
            guardar.run();
        }
    }

    // =====================================================================
    // CU03: proveedores
    // =====================================================================

    private void menuProveedores() {
        int opcion;
        do {
            opcion = consola.menu("Proveedores", new String[]{
                    "Ver los proveedores",
                    "Registrar un proveedor",
                    "Modificar un proveedor",
                    "Asociar un proveedor a una actividad",
                    "Quitar el proveedor de una actividad"}, "Volver");
            if (opcion == 1) {
                verProveedores();
            } else if (opcion == 2) {
                registrarProveedor();
            } else if (opcion == 3) {
                modificarProveedor();
            } else if (opcion == 4) {
                asociarProveedor();
            } else if (opcion == 5) {
                quitarProveedorDeActividad();
            }
        } while (opcion != 0);
    }

    private void verProveedores() {
        List<Proveedor> proveedores = catalogo.getProveedores();
        if (proveedores != null) {
            consola.subtitulo("Proveedores");
            listados.mostrarProveedores(proveedores);
        }
    }

    private void registrarProveedor() {
        consola.subtitulo("Registrar un proveedor");
        String nombre = consola.leerTexto("Nombre");
        String cuit = consola.leerTexto("CUIT");
        String contacto = consola.leerTexto("Persona de contacto");
        String telefono = consola.leerTexto("Teléfono");
        String servicio = consola.leerTexto("Servicio que presta");
        if (catalogo.registrarProveedor(usuario, nombre, cuit, contacto, telefono, servicio)) {
            guardar.run();
        }
    }

    private void modificarProveedor() {
        consola.subtitulo("Modificar un proveedor");
        Proveedor proveedor = elegirProveedor();
        if (proveedor == null) {
            return;
        }
        String nombre = consola.leerTexto("Nombre nuevo");
        String contacto = consola.leerTexto("Persona de contacto nueva");
        String telefono = consola.leerTexto("Teléfono nuevo");
        String servicio = consola.leerTexto("Servicio que presta");
        if (catalogo.modificarProveedor(usuario, proveedor.getCuit(), nombre, contacto, telefono, servicio)) {
            guardar.run();
        }
    }

    private void asociarProveedor() {
        consola.subtitulo("Asociar un proveedor a una actividad");
        Tour tour = elegirTour();
        if (tour == null) {
            return;
        }
        int numero = elegirActividad(tour);
        if (numero == 0) {
            return;
        }
        Proveedor proveedor = elegirProveedor();
        if (proveedor == null) {
            return;
        }
        if (catalogo.asociarProveedor(usuario, tour.getCodigo(), numero, proveedor.getCuit())) {
            guardar.run();
        }
    }

    private void quitarProveedorDeActividad() {
        consola.subtitulo("Quitar el proveedor de una actividad");
        Tour tour = elegirTour();
        if (tour == null) {
            return;
        }
        int numero = elegirActividad(tour);
        if (numero == 0) {
            return;
        }
        if (catalogo.quitarProveedorDeActividad(usuario, tour.getCodigo(), numero)) {
            guardar.run();
        }
    }

    // =====================================================================
    // CU04: tarifas (se elige el tour y después se trabaja con sus tarifas)
    // =====================================================================

    private void menuTarifas() {
        consola.subtitulo("Tarifas de un tour");
        Tour tour = elegirTour();
        if (tour == null) {
            return;
        }
        int opcion;
        do {
            opcion = consola.menu("Tarifas de " + tour.getNombre(), new String[]{
                    "Ver las tarifas",
                    "Agregar una tarifa",
                    "Modificar el precio de una tarifa",
                    "Modificar las fechas de una tarifa"}, "Volver");
            if (opcion == 1) {
                verTarifas(tour);
            } else if (opcion == 2) {
                agregarTarifa(tour);
            } else if (opcion == 3) {
                modificarPrecioTarifa(tour);
            } else if (opcion == 4) {
                modificarFechasTarifa(tour);
            }
        } while (opcion != 0);
    }

    private void verTarifas(Tour tour) {
        List<Tarifa> tarifas = catalogo.getTarifas(tour.getCodigo());
        if (tarifas != null) {
            consola.subtitulo("Tarifas de " + tour.getNombre());
            listados.mostrarLista(textosDeTarifas(tarifas));
        }
    }

    private void agregarTarifa(Tour tour) {
        consola.subtitulo("Agregar una tarifa");
        List<Temporada> temporadas = catalogo.getTemporadas();
        Temporada temporada = temporadas.get(consola.elegirPosicion("Temporada", temporadas));
        LocalDate desde = consola.leerFecha("Vigente desde");
        LocalDate hasta = consola.leerFecha("Vigente hasta");
        double precio = consola.leerDecimal("Precio base por pasajero");
        if (catalogo.agregarTarifa(usuario, tour.getCodigo(), temporada, desde, hasta, precio)) {
            guardar.run();
        }
    }

    private void modificarPrecioTarifa(Tour tour) {
        consola.subtitulo("Modificar el precio de una tarifa");
        int numero = elegirTarifa(tour);
        if (numero == 0) {
            return;
        }
        double precio = consola.leerDecimal("Precio base nuevo");
        if (catalogo.modificarPrecioTarifa(usuario, tour.getCodigo(), numero, precio)) {
            guardar.run();
        }
    }

    private void modificarFechasTarifa(Tour tour) {
        consola.subtitulo("Modificar las fechas de una tarifa");
        int numero = elegirTarifa(tour);
        if (numero == 0) {
            return;
        }
        LocalDate desde = consola.leerFecha("Vigente desde");
        LocalDate hasta = consola.leerFecha("Vigente hasta");
        if (catalogo.modificarFechasTarifa(usuario, tour.getCodigo(), numero, desde, hasta)) {
            guardar.run();
        }
    }

    // =====================================================================
    // CU05: servicios adicionales
    // =====================================================================

    private void menuServicios() {
        int opcion;
        do {
            opcion = consola.menu("Servicios adicionales", new String[]{
                    "Ver los servicios adicionales",
                    "Registrar un servicio adicional",
                    "Modificar un servicio adicional",
                    "Ofrecer un servicio en un tour",
                    "Cambiar si un servicio es obligatorio en un tour",
                    "Quitar un servicio de un tour"}, "Volver");
            if (opcion == 1) {
                verServicios();
            } else if (opcion == 2) {
                registrarServicio();
            } else if (opcion == 3) {
                modificarServicio();
            } else if (opcion == 4) {
                ofrecerServicioEnTour();
            } else if (opcion == 5) {
                cambiarObligatoriedad();
            } else if (opcion == 6) {
                quitarServicioDeTour();
            }
        } while (opcion != 0);
    }

    private void verServicios() {
        List<ServicioAdicional> servicios = catalogo.getServiciosAdicionales();
        if (servicios != null) {
            consola.subtitulo("Servicios adicionales");
            listados.mostrarServicios(servicios);
        }
    }

    private void registrarServicio() {
        consola.subtitulo("Registrar un servicio adicional");
        int codigo = consola.leerEntero("Código");
        String nombre = consola.leerTexto("Nombre");
        String descripcion = consola.leerTexto("Descripción");
        List<TipoServicioAdicional> tipos = catalogo.getTiposDeServicio();
        TipoServicioAdicional tipo = tipos.get(consola.elegirPosicion("Tipo de servicio", tipos));
        double precio = consola.leerDecimal("Precio");
        if (catalogo.registrarServicioAdicional(usuario, codigo, nombre, descripcion, tipo, precio)) {
            guardar.run();
        }
    }

    private void modificarServicio() {
        consola.subtitulo("Modificar un servicio adicional");
        ServicioAdicional servicio = elegirServicio();
        if (servicio == null) {
            return;
        }
        String nombre = consola.leerTexto("Nombre nuevo");
        String descripcion = consola.leerTexto("Descripción nueva");
        double precio = consola.leerDecimal("Precio nuevo");
        if (catalogo.modificarServicioAdicional(usuario, servicio.getCodigo(), nombre, descripcion, precio)) {
            guardar.run();
        }
    }

    private void ofrecerServicioEnTour() {
        consola.subtitulo("Ofrecer un servicio en un tour");
        Tour tour = elegirTour();
        if (tour == null) {
            return;
        }
        ServicioAdicional servicio = elegirServicio();
        if (servicio == null) {
            return;
        }
        boolean obligatorio = consola.confirmar("¿Es obligatorio en este tour?");
        if (catalogo.ofrecerServicioEnTour(usuario, tour.getCodigo(), servicio.getCodigo(), obligatorio)) {
            guardar.run();
        }
    }

    private void cambiarObligatoriedad() {
        consola.subtitulo("Cambiar si un servicio es obligatorio en un tour");
        Tour tour = elegirTour();
        if (tour == null) {
            return;
        }
        ServicioTour servicioTour = elegirServicioDelTour(tour);
        if (servicioTour == null) {
            return;
        }
        boolean obligatorio = consola.confirmar("¿Debe ser obligatorio?");
        if (catalogo.cambiarObligatoriedadServicio(usuario, tour.getCodigo(),
                servicioTour.getServicioAdicional().getCodigo(), obligatorio)) {
            guardar.run();
        }
    }

    private void quitarServicioDeTour() {
        consola.subtitulo("Quitar un servicio de un tour");
        Tour tour = elegirTour();
        if (tour == null) {
            return;
        }
        ServicioTour servicioTour = elegirServicioDelTour(tour);
        if (servicioTour == null) {
            return;
        }
        if (catalogo.quitarServicioDeTour(usuario, tour.getCodigo(), servicioTour.getServicioAdicional().getCodigo())) {
            guardar.run();
        }
    }

    // =====================================================================
    // CU06: vehículos
    // =====================================================================

    private void menuVehiculos() {
        int opcion;
        do {
            opcion = consola.menu("Vehículos", new String[]{
                    "Ver los vehículos",
                    "Registrar un vehículo",
                    "Modificar un vehículo"}, "Volver");
            if (opcion == 1) {
                verVehiculos();
            } else if (opcion == 2) {
                registrarVehiculo();
            } else if (opcion == 3) {
                modificarVehiculo();
            }
        } while (opcion != 0);
    }

    private void verVehiculos() {
        List<Vehiculo> vehiculos = catalogo.getVehiculos();
        if (vehiculos != null) {
            consola.subtitulo("Vehículos");
            listados.mostrarVehiculos(vehiculos);
        }
    }

    private void registrarVehiculo() {
        consola.subtitulo("Registrar un vehículo");
        String patente = consola.leerTexto("Patente");
        List<TipoVehiculo> tipos = catalogo.getTiposDeVehiculo();
        TipoVehiculo tipo = tipos.get(consola.elegirPosicion("Tipo de vehículo", tipos));
        int capacidad = consola.leerEntero("Capacidad (cantidad de pasajeros)");
        if (catalogo.registrarVehiculo(usuario, patente, tipo, capacidad)) {
            guardar.run();
        }
    }

    private void modificarVehiculo() {
        consola.subtitulo("Modificar un vehículo");
        List<Vehiculo> vehiculos = catalogo.getVehiculos();
        if (vehiculos == null) {
            return;
        }
        Vehiculo vehiculo = vehiculos.get(consola.elegirPosicion("Vehículo", vehiculos));
        List<TipoVehiculo> tipos = catalogo.getTiposDeVehiculo();
        TipoVehiculo tipo = tipos.get(consola.elegirPosicion("Tipo de vehículo nuevo", tipos));
        int capacidad = consola.leerEntero("Capacidad nueva");
        if (catalogo.modificarVehiculo(usuario, vehiculo.getPatente(), tipo, capacidad)) {
            guardar.run();
        }
    }

    // =====================================================================
    // CU07: personal
    // =====================================================================

    private void menuPersonal() {
        int opcion;
        do {
            opcion = consola.menu("Personal", new String[]{
                    "Ver el personal",
                    "Registrar nuevo personal",
                    "Gestionar guías",
                    "Modificar los datos de un empleado"}, "Volver");
            if (opcion == 1) {
                menuVerPersonal();
            } else if (opcion == 2) {
                menuRegistrarPersonal();
            } else if (opcion == 3) {
                menuGestionarGuias();
            } else if (opcion == 4) {
                modificarDatosPersonal();
            }
        } while (opcion != 0);
    }

    private void menuVerPersonal() {
        int opcion;
        do {
            opcion = consola.menu("Ver el personal", new String[]{
                    "Todo el personal",
                    "Los guías",
                    "Los vendedores",
                    "Los coordinadores de operaciones",
                    "Los administradores"}, "Volver");
            if (opcion == 1) {
                verPersonal();
            } else if (opcion == 2) {
                verGuias();
            } else if (opcion == 3) {
                verVendedores();
            } else if (opcion == 4) {
                verCoordinadores();
            } else if (opcion == 5) {
                verAdministradores();
            }
        } while (opcion != 0);
    }

    private void verPersonal() {
        List<Personal> personal = catalogo.getPersonal();
        if (personal != null) {
            consola.subtitulo("Personal");
            listados.mostrarPersonal(personal);
        }
    }

    private void verGuias() {
        List<Guia> guias = catalogo.getGuias();
        if (guias != null) {
            consola.subtitulo("Guías");
            listados.mostrarGuias(guias);
        }
    }

    private void verVendedores() {
        List<Vendedor> vendedores = catalogo.getVendedores();
        if (vendedores != null) {
            consola.subtitulo("Vendedores");
            listados.mostrarVendedores(vendedores);
        }
    }

    private void verCoordinadores() {
        List<Personal> coordinadores = catalogo.getCoordinadores();
        if (coordinadores != null) {
            consola.subtitulo("Coordinadores de operaciones");
            listados.mostrarPersonal(coordinadores);
        }
    }

    private void verAdministradores() {
        List<Personal> administradores = catalogo.getAdministradores();
        if (administradores != null) {
            consola.subtitulo("Administradores");
            listados.mostrarPersonal(administradores);
        }
    }

    // se elige el tipo de empleado y se lo registra
    private void menuRegistrarPersonal() {
        int opcion = consola.menu("Registrar nuevo personal", new String[]{
                "Un vendedor",
                "Un guía",
                "Un coordinador de operaciones",
                "Un administrador"}, "Volver");
        if (opcion == 1) {
            registrarVendedor();
        } else if (opcion == 2) {
            registrarGuia();
        } else if (opcion == 3) {
            registrarCoordinador();
        } else if (opcion == 4) {
            registrarAdministrador();
        }
    }

    private void registrarVendedor() {
        consola.subtitulo("Registrar un vendedor");
        int legajo = consola.leerEntero("Legajo");
        String nombre = consola.leerTexto("Nombre");
        String documento = consola.leerTexto("Documento");
        String telefono = consola.leerTexto("Teléfono");
        double comision = consola.leerDecimal("Comisión (porcentaje)");
        if (catalogo.registrarVendedor(usuario, legajo, nombre, documento, telefono, comision)) {
            guardar.run();
        }
    }

    private void registrarGuia() {
        consola.subtitulo("Registrar un guía");
        int legajo = consola.leerEntero("Legajo");
        String nombre = consola.leerTexto("Nombre");
        String documento = consola.leerTexto("Documento");
        String telefono = consola.leerTexto("Teléfono");
        double honorario = consola.leerDecimal("Honorario por día");
        String idioma = consola.leerTexto("Idioma");
        List<CategoriaTour> categorias = catalogo.getCategoriasDeTour();
        CategoriaTour categoria = categorias.get(consola.elegirPosicion("Categoría de tours para la que está habilitado", categorias));
        if (catalogo.registrarGuia(usuario, legajo, nombre, documento, telefono, honorario, idioma, categoria)) {
            guardar.run();
        }
    }

    private void registrarCoordinador() {
        consola.subtitulo("Registrar un coordinador de operaciones");
        int legajo = consola.leerEntero("Legajo");
        String nombre = consola.leerTexto("Nombre");
        String documento = consola.leerTexto("Documento");
        String telefono = consola.leerTexto("Teléfono");
        if (catalogo.registrarCoordinador(usuario, legajo, nombre, documento, telefono)) {
            guardar.run();
        }
    }

    private void registrarAdministrador() {
        consola.subtitulo("Registrar un administrador");
        int legajo = consola.leerEntero("Legajo");
        String nombre = consola.leerTexto("Nombre");
        String documento = consola.leerTexto("Documento");
        String telefono = consola.leerTexto("Teléfono");
        if (catalogo.registrarAdministrador(usuario, legajo, nombre, documento, telefono)) {
            guardar.run();
        }
    }

    private void modificarDatosPersonal() {
        consola.subtitulo("Modificar los datos de un empleado");
        List<Personal> personal = catalogo.getPersonal();
        if (personal == null) {
            return;
        }
        Personal empleado = personal.get(consola.elegirPosicion("Empleado", personal));
        String nombre = consola.leerTexto("Nombre nuevo");
        String documento = consola.leerTexto("Documento nuevo");
        String telefono = consola.leerTexto("Teléfono nuevo");
        if (catalogo.modificarDatosPersonal(usuario, empleado.getLegajo(), nombre, documento, telefono)) {
            guardar.run();
        }
    }

    // se elige el guía una sola vez y después se pueden hacer varios cambios en sus idiomas y categorías
    private void menuGestionarGuias() {
        consola.subtitulo("Gestionar guías");
        Guia guia = elegirGuia();
        if (guia == null) {
            return;
        }
        int opcion;
        do {
            consola.info("Idiomas: " + guia.getIdiomas() + " | Categorías: " + guia.getCategoriasHabilitadas());
            opcion = consola.menu("Gestionar a " + guia.getNombre(), new String[]{
                    "Agregar un idioma",
                    "Quitar un idioma",
                    "Agregar una categoría",
                    "Quitar una categoría"}, "Volver");
            if (opcion == 1) {
                agregarIdiomaGuia(guia);
            } else if (opcion == 2) {
                quitarIdiomaGuia(guia);
            } else if (opcion == 3) {
                agregarCategoriaGuia(guia);
            } else if (opcion == 4) {
                quitarCategoriaGuia(guia);
            }
        } while (opcion != 0);
    }

    private void agregarIdiomaGuia(Guia guia) {
        consola.subtitulo("Agregar un idioma");
        String idioma = consola.leerTexto("Idioma");
        if (catalogo.agregarIdiomaGuia(usuario, guia.getLegajo(), idioma)) {
            guardar.run();
        }
    }

    private void quitarIdiomaGuia(Guia guia) {
        consola.subtitulo("Quitar un idioma");
        List<String> idiomas = guia.getIdiomas();
        String idioma = idiomas.get(consola.elegirPosicion("Idioma a quitar", idiomas));
        if (catalogo.quitarIdiomaGuia(usuario, guia.getLegajo(), idioma)) {
            guardar.run();
        }
    }

    private void agregarCategoriaGuia(Guia guia) {
        consola.subtitulo("Agregar una categoría");
        List<CategoriaTour> categorias = catalogo.getCategoriasDeTour();
        CategoriaTour categoria = categorias.get(consola.elegirPosicion("Categoría", categorias));
        if (catalogo.agregarCategoriaGuia(usuario, guia.getLegajo(), categoria)) {
            guardar.run();
        }
    }

    private void quitarCategoriaGuia(Guia guia) {
        consola.subtitulo("Quitar una categoría");
        List<CategoriaTour> categorias = guia.getCategoriasHabilitadas();
        CategoriaTour categoria = categorias.get(consola.elegirPosicion("Categoría a quitar", categorias));
        if (catalogo.quitarCategoriaGuia(usuario, guia.getLegajo(), categoria)) {
            guardar.run();
        }
    }

    // =====================================================================
    // CU08: liquidación de honorarios
    // =====================================================================

    private void liquidarHonorarios() {
        consola.subtitulo("Liquidar los honorarios de un guía");
        List<Salida> pendientes = administracion.getSalidasPendientesDeLiquidar();
        if (pendientes == null) {
            return;
        }
        Salida salida = listados.elegirSalida(pendientes);
        if (salida == null) {
            return;
        }
        LiquidacionGuia liquidacion = administracion.liquidarHonorarios(usuario, salida.getNumero(), hoy);
        if (liquidacion != null) {
            consola.texto("  Guía:                      " + liquidacion.getGuia().getNombre());
            consola.texto("  Fecha:                     " + consola.formatear(liquidacion.getFecha()));
            consola.texto("  Honorarios:                " + consola.formatearDinero(liquidacion.getHonorarios()));
            consola.texto("  Comisión por adicionales:  " + consola.formatearDinero(liquidacion.getComisionAdicionales()));
            consola.texto("  Total a pagar:             " + consola.formatearDinero(liquidacion.calcularTotal()));
            guardar.run();
        }
    }

    // =====================================================================
    // CU09: reportes
    // =====================================================================

    private void menuReportes() {
        int opcion;
        do {
            opcion = consola.menu("Reportes", new String[]{
                    "Ocupación de las salidas",
                    "Ventas por vendedor",
                    "Ingresos por tour",
                    "Cancelaciones"}, "Volver");
            if (opcion == 1) {
                reporteOcupacion();
            } else if (opcion == 2) {
                reporteVentas();
            } else if (opcion == 3) {
                reporteIngresos();
            } else if (opcion == 4) {
                reporteCancelaciones();
            }
        } while (opcion != 0);
    }

    private void reporteOcupacion() {
        consola.subtitulo("Ocupación de las salidas");
        LocalDate desde = consola.leerFecha("Desde");
        LocalDate hasta = consola.leerFecha("Hasta");
        List<Salida> salidas = administracion.reporteOcupacion(usuario, desde, hasta);
        if (salidas == null) {
            return;
        }
        if (salidas.isEmpty()) {
            consola.aviso("No hay salidas en ese período.");
            return;
        }
        listados.mostrarOcupacion(salidas);
    }

    private void reporteVentas() {
        consola.subtitulo("Ventas por vendedor");
        LocalDate desde = consola.leerFecha("Desde");
        LocalDate hasta = consola.leerFecha("Hasta");
        List<Vendedor> vendedores = administracion.getVendedores();
        if (vendedores == null) {
            return;
        }
        List<String[]> filas = new ArrayList<>();
        for (Vendedor vendedor : vendedores) {
            Integer cantidad = administracion.contarVentas(usuario, vendedor, desde, hasta);
            Double total = administracion.calcularTotalVendido(usuario, vendedor, desde, hasta);
            Double comision = administracion.calcularComisionVendedor(usuario, vendedor, desde, hasta);
            if (cantidad == null || total == null || comision == null) {
                return;   // el controlador ya mostró el error (por ejemplo, un período inválido)
            }
            filas.add(new String[]{vendedor.getNombre(), String.valueOf(cantidad),
                    consola.formatearDinero(total), consola.formatearDinero(comision)});
        }
        consola.tabla(new String[]{"Vendedor", "Ventas", "Total vendido", "Comisión"}, filas);
    }

    private void reporteIngresos() {
        consola.subtitulo("Ingresos por tour");
        LocalDate desde = consola.leerFecha("Desde");
        LocalDate hasta = consola.leerFecha("Hasta");
        List<Tour> tours = administracion.getTours();
        if (tours == null) {
            return;
        }
        List<String[]> filas = new ArrayList<>();
        for (Tour tour : tours) {
            Double cobrado = administracion.calcularIngresosCobrados(usuario, tour, desde, hasta);
            Double devuelto = administracion.calcularDevoluciones(usuario, tour, desde, hasta);
            Double neto = administracion.calcularIngresosNetos(usuario, tour, desde, hasta);
            if (cobrado == null || devuelto == null || neto == null) {
                return;   // el controlador ya mostró el error
            }
            filas.add(new String[]{tour.getNombre(), consola.formatearDinero(cobrado),
                    consola.formatearDinero(devuelto), consola.formatearDinero(neto)});
        }
        consola.tabla(new String[]{"Tour", "Cobrado", "Devuelto", "Neto"}, filas);
    }

    private void reporteCancelaciones() {
        consola.subtitulo("Cancelaciones");
        LocalDate desde = consola.leerFecha("Desde");
        LocalDate hasta = consola.leerFecha("Hasta");
        List<Reserva> reservas = administracion.reporteCancelaciones(usuario, desde, hasta);
        if (reservas == null) {
            return;
        }
        if (reservas.isEmpty()) {
            consola.aviso("No hubo cancelaciones en ese período.");
            return;
        }
        listados.mostrarCancelaciones(reservas);
        consola.info("Total devuelto: " + consola.formatearDinero(administracion.calcularTotalDevuelto(reservas)));
    }

    // =====================================================================
    // apoyo: elegir de una lista
    // =====================================================================

    // todas devuelven null (o 0) si no se pudo elegir; el controlador ya mostró el motivo

    private Tour elegirTour() {
        List<Tour> tours = catalogo.getTours();
        if (tours == null) {
            return null;
        }
        return tours.get(consola.elegirPosicion("Tour", tours));
    }

    private Proveedor elegirProveedor() {
        List<Proveedor> proveedores = catalogo.getProveedores();
        if (proveedores == null) {
            return null;
        }
        return proveedores.get(consola.elegirPosicion("Proveedor", proveedores));
    }

    private ServicioAdicional elegirServicio() {
        List<ServicioAdicional> servicios = catalogo.getServiciosAdicionales();
        if (servicios == null) {
            return null;
        }
        return servicios.get(consola.elegirPosicion("Servicio", servicios));
    }

    private Guia elegirGuia() {
        List<Guia> guias = catalogo.getGuias();
        if (guias == null) {
            return null;
        }
        return guias.get(consola.elegirPosicion("Guía", guias));
    }

    // devuelve el número de la actividad elegida (desde 1), o 0 si no hay
    private int elegirActividad(Tour tour) {
        List<Actividad> actividades = catalogo.getActividades(tour.getCodigo());
        if (actividades == null) {
            return 0;
        }
        return consola.elegirPosicion("Actividad", textosDeActividades(actividades)) + 1;
    }

    // devuelve el número de la tarifa elegida (desde 1), o 0 si no hay
    private int elegirTarifa(Tour tour) {
        List<Tarifa> tarifas = catalogo.getTarifas(tour.getCodigo());
        if (tarifas == null) {
            return 0;
        }
        return consola.elegirPosicion("Tarifa", textosDeTarifas(tarifas)) + 1;
    }

    private ServicioTour elegirServicioDelTour(Tour tour) {
        List<ServicioTour> servicios = catalogo.getServiciosDelTour(tour.getCodigo());
        if (servicios == null) {
            return null;
        }
        return servicios.get(consola.elegirPosicion("Servicio del tour", servicios));
    }

    // ---- textos para mostrar ----

    // el texto de cada tour; los que tienen salidas vigentes lo avisan (no se pueden dar de baja ni cambiar de duración o cupos)
    private List<String> textosDeTours(List<Tour> tours) {
        List<Tour> conSalidasVigentes = catalogo.getToursConSalidasVigentes();
        List<String> textos = new ArrayList<>();
        for (Tour tour : tours) {
            String texto = tour.toString();
            if (conSalidasVigentes.contains(tour)) {
                texto += "  [con salidas vigentes]";
            }
            textos.add(texto);
        }
        return textos;
    }

    private List<String> textosDeActividades(List<Actividad> actividades) {
        List<String> textos = new ArrayList<>();
        for (Actividad actividad : actividades) {
            String proveedor = "";
            if (actividad.getProveedor() != null) {
                proveedor = " - " + actividad.getProveedor().getNombre();
            }
            textos.add("Día " + actividad.getDia() + ", orden " + actividad.getOrden() + " - "
                    + consola.formatear(actividad.getHorario()) + " - " + actividad.getDescripcion()
                    + " (" + actividad.getLugar() + ") - " + actividad.getTipo() + proveedor);
        }
        return textos;
    }

    private List<String> textosDeTarifas(List<Tarifa> tarifas) {
        List<String> textos = new ArrayList<>();
        for (Tarifa tarifa : tarifas) {
            textos.add(tarifa.getTemporada() + ": " + consola.formatear(tarifa.getFechaDesde()) + " a "
                    + consola.formatear(tarifa.getFechaHasta()) + " - " + consola.formatearDinero(tarifa.getPrecioBase()));
        }
        return textos;
    }
}