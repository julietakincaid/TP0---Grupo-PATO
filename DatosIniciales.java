import excepcion.ReglaNegocioException;
import modelo.Actividad;
import modelo.Administrador;
import modelo.Cliente;
import modelo.CoordinadorOperaciones;
import modelo.Guia;
import modelo.Proveedor;
import modelo.Reserva;
import modelo.Salida;
import modelo.ServicioAdicional;
import modelo.Tour;
import modelo.TourAventura;
import modelo.Vehiculo;
import modelo.Vendedor;
import modelo.enums.CategoriaTour;
import modelo.enums.MedioPago;
import modelo.enums.OrigenCancelacion;
import modelo.enums.Temporada;
import modelo.enums.TipoActividad;
import modelo.enums.TipoPasajero;
import modelo.enums.TipoServicioAdicional;
import modelo.enums.TipoVehiculo;
import servicio.Agencia;
import servicio.CalculadoraPrecioReserva;
import servicio.ServicioAsignacion;

import java.time.LocalDate;
import java.time.LocalTime;

// Los datos de prueba con los que arranca la agencia la primera vez que se ejecuta el programa:
// Para volver a cargarlos, se borra el archivo datos/agencia.dat.
public class DatosIniciales {
    public static void cargar(Agencia agencia) throws ReglaNegocioException {
        LocalDate hoy = LocalDate.now();
        cargarPersonal(agencia);
        cargarProveedoresServiciosYVehiculos(agencia);
        cargarTours(agencia, hoy);
        cargarClientes(agencia);
        cargarSalidasYReservas(agencia, hoy);
    }

    // legajos: 1 administrador, 2 coordinador, 3 y 5 vendedores, 4, 6 y 7 guías
    private static void cargarPersonal(Agencia agencia) throws ReglaNegocioException {
        agencia.registrarPersonal(new Administrador(1, "Camila Torres", "10000001", "1100000001"));
        agencia.registrarPersonal(new CoordinadorOperaciones(2, "Santiago Mena", "10000002", "1100000002"));
        agencia.registrarPersonal(new Vendedor(3, "Pablo Herrera", "10000003", "1100000003", 5));
        agencia.registrarPersonal(new Guia(4, "Julia Navarro", "10000004", "1100000004", 1000, "Español", CategoriaTour.CULTURAL));
        agencia.registrarPersonal(new Vendedor(5, "Valeria Suárez", "10000005", "1100000005", 8));

        Guia martin = new Guia(6, "Martín Ortiz", "10000006", "1100000006", 1500, "Español", CategoriaTour.AVENTURA);
        martin.agregarIdioma("Inglés");
        martin.agregarCategoria(CategoriaTour.NATURALEZA);
        agencia.registrarPersonal(martin);

        Guia lucia = new Guia(7, "Lucía Romero", "10000007", "1100000007", 1200, "Español", CategoriaTour.NATURALEZA);
        lucia.agregarIdioma("Portugués");
        agencia.registrarPersonal(lucia);
    }

    private static void cargarProveedoresServiciosYVehiculos(Agencia agencia) throws ReglaNegocioException {
        agencia.registrarProveedor(new Proveedor("Hotel Sur", "30-71234567-8", "Ana Ferrari", "1144445555", "Alojamiento"));
        agencia.registrarProveedor(new Proveedor("Bodega Los Andes", "30-68765432-1", "Pablo Díaz", "1155556666", "Comidas y degustaciones"));
        agencia.registrarProveedor(new Proveedor("Aventura Río Bravo", "30-70123456-5", "Sergio Luna", "1166667777", "Actividades de aventura"));

        agencia.registrarServicioAdicional(new ServicioAdicional(1, "Seguro de viaje", "Cobertura médica durante el tour",
                TipoServicioAdicional.SEGURO, 50));
        agencia.registrarServicioAdicional(new ServicioAdicional(2, "Excursión a la bodega", "Visita y degustación",
                TipoServicioAdicional.EXCURSION, 30));
        agencia.registrarServicioAdicional(new ServicioAdicional(3, "Traslado al aeropuerto", "Traslado de ida o de vuelta",
                TipoServicioAdicional.TRASLADO, 20));

        agencia.registrarVehiculo(new Vehiculo("AB123CD", TipoVehiculo.COMBI, 12));
        agencia.registrarVehiculo(new Vehiculo("AC456EF", TipoVehiculo.MICRO, 30));
        agencia.registrarVehiculo(new Vehiculo("AD789GH", TipoVehiculo.CAMIONETA, 6));
    }

    // tres tours (uno de cada categoría), cada uno con itinerario y tarifas para las tres temporadas
    private static void cargarTours(Agencia agencia, LocalDate hoy) throws ReglaNegocioException {
        Proveedor hotel = agencia.buscarProveedor("30712345678");
        Proveedor bodega = agencia.buscarProveedor("30687654321");
        Proveedor rioBravo = agencia.buscarProveedor("30701234565");
        ServicioAdicional seguro = agencia.buscarServicioAdicional(1);
        ServicioAdicional excursion = agencia.buscarServicioAdicional(2);
        ServicioAdicional traslado = agencia.buscarServicioAdicional(3);

        // cultural: 3 días
        Tour cultural = new Tour("CUL-01", "Buenos Aires cultural", "Museos, barrios históricos y teatro",
                3, CategoriaTour.CULTURAL, 2, 10);
        cultural.agregarActividad(1, 1, TipoActividad.VISITA, LocalTime.of(9, 0), "Visita guiada a museos", "Centro");
        Actividad almuerzo = cultural.agregarActividad(1, 2, TipoActividad.COMIDA, LocalTime.of(13, 0), "Almuerzo en la bodega", "Palermo");
        almuerzo.setProveedor(bodega);
        Actividad hospedaje = cultural.agregarActividad(1, 3, TipoActividad.ALOJAMIENTO, LocalTime.of(18, 0), "Check-in en el hotel", "Microcentro");
        hospedaje.setProveedor(hotel);
        cultural.agregarActividad(2, 1, TipoActividad.VISITA, LocalTime.of(10, 0), "Recorrido por San Telmo", "San Telmo");
        cultural.agregarActividad(3, 1, TipoActividad.VISITA, LocalTime.of(9, 0), "Visita al Teatro Colón", "Centro");
        cultural.agregarActividad(3, 2, TipoActividad.TRASLADO, LocalTime.of(12, 0), "Traslado final", "Aeropuerto");
        cargarTarifas(cultural, hoy, 100, 150, 200);
        cultural.agregarServicio(excursion, false);
        cultural.agregarServicio(traslado, false);
        agencia.registrarTour(cultural);

        // naturaleza: 4 días
        Tour naturaleza = new Tour("NAT-01", "Sierras y lagos de Córdoba", "Caminatas, lagos y miradores",
                4, CategoriaTour.NATURALEZA, 4, 20);
        naturaleza.agregarActividad(1, 1, TipoActividad.TRASLADO, LocalTime.of(8, 0), "Salida hacia las sierras", "Terminal");
        naturaleza.agregarActividad(1, 2, TipoActividad.COMIDA, LocalTime.of(13, 0), "Almuerzo campestre", "Villa General Belgrano");
        naturaleza.agregarActividad(2, 1, TipoActividad.VISITA, LocalTime.of(9, 0), "Caminata por el valle", "Valle de Calamuchita");
        naturaleza.agregarActividad(3, 1, TipoActividad.VISITA, LocalTime.of(10, 0), "Paseo en lancha por el lago", "Embalse");
        naturaleza.agregarActividad(4, 1, TipoActividad.VISITA, LocalTime.of(10, 0), "Mirador y regreso", "Cerro");
        cargarTarifas(naturaleza, hoy, 180, 220, 280);
        naturaleza.agregarServicio(excursion, false);
        agencia.registrarTour(naturaleza);

        // naturaleza, sin salidas programadas: sirve para probar cambios de duración, de cupos y la baja, y para programar una salida nueva
        Tour cataratas = new Tour("NAT-02", "Cataratas del Iguazú", "Pasarelas, selva y cataratas",
                3, CategoriaTour.NATURALEZA, 3, 15);
        cataratas.agregarActividad(1, 1, TipoActividad.VISITA, LocalTime.of(9, 0), "Pasarelas de las cataratas", "Parque Nacional");
        cataratas.agregarActividad(2, 1, TipoActividad.VISITA, LocalTime.of(9, 0), "Paseo en lancha a la Garganta del Diablo", "Río Iguazú");
        cataratas.agregarActividad(3, 1, TipoActividad.TRASLADO, LocalTime.of(10, 0), "Traslado al aeropuerto", "Aeropuerto");
        cargarTarifas(cataratas, hoy, 250, 300, 350);
        cataratas.agregarServicio(traslado, false);
        agencia.registrarTour(cataratas);

        // aventura: 2 días, con seguro obligatorio (RN15)
        TourAventura aventura = new TourAventura("AVE-01", "Rafting en el Río Bravo", "Descenso en rafting y cabalgata",
                2, 2, 8, 14, "Alta");
        Actividad charla = aventura.agregarActividad(1, 1, TipoActividad.VISITA, LocalTime.of(8, 0), "Charla de seguridad y equipamiento", "Base del río");
        charla.setProveedor(rioBravo);
        Actividad rafting = aventura.agregarActividad(1, 2, TipoActividad.VISITA, LocalTime.of(10, 0), "Descenso en rafting", "Río Bravo");
        rafting.setProveedor(rioBravo);
        aventura.agregarActividad(2, 1, TipoActividad.VISITA, LocalTime.of(9, 0), "Cabalgata por la ribera", "Ribera");
        cargarTarifas(aventura, hoy, 300, 350, 400);
        aventura.agregarServicio(seguro, true);
        aventura.agregarServicio(excursion, false);
        agencia.registrarTour(aventura);
    }

    // temporada baja: de hace 90 días a 100 días; media: los 100 siguientes; alta: hasta 400 días
    private static void cargarTarifas(Tour tour, LocalDate hoy, double baja, double media, double alta)
            throws ReglaNegocioException {
        tour.agregarTarifa(Temporada.BAJA, hoy.minusDays(90), hoy.plusDays(100), baja);
        tour.agregarTarifa(Temporada.MEDIA, hoy.plusDays(101), hoy.plusDays(200), media);
        tour.agregarTarifa(Temporada.ALTA, hoy.plusDays(201), hoy.plusDays(400), alta);
    }

    private static void cargarClientes(Agencia agencia) throws ReglaNegocioException {
        agencia.registrarCliente(new Cliente("Ana", "Pérez", "30111222", "1144440001", "ana.perez@mail.com", "Argentina"));
        agencia.registrarCliente(new Cliente("Beto", "Gómez", "31222333", "1144440002", "beto.gomez@mail.com", "Argentina"));
        agencia.registrarCliente(new Cliente("Carla", "Ruiz", "32333444", "1144440003", "carla.ruiz@mail.com", "Chile"));
        agencia.registrarCliente(new Cliente("Diego", "Fernández", "33444555", "1144440004", "diego.fernandez@mail.com", "Uruguay"));
        agencia.registrarCliente(new Cliente("Elena", "Martínez", "34555666", "1144440005", "elena.martinez@mail.com", "Argentina"));
        agencia.registrarCliente(new Cliente("Facundo", "López", "35666777", "1144440006", "facundo.lopez@mail.com", "Brasil"));
    }

    // cinco salidas en distintos estados, con reservas pendientes, confirmadas, anuladas y finalizadas
    private static void cargarSalidasYReservas(Agencia agencia, LocalDate hoy) throws ReglaNegocioException {
        CalculadoraPrecioReserva calculadora = new CalculadoraPrecioReserva(agencia.getGestorPromociones());
        ServicioAsignacion asignacion = new ServicioAsignacion();
        Vendedor vendedor3 = (Vendedor) agencia.buscarPersonal(3);
        Vendedor vendedor5 = (Vendedor) agencia.buscarPersonal(5);
        Guia guiaCultural = (Guia) agencia.buscarPersonal(4);
        Guia guiaAventura = (Guia) agencia.buscarPersonal(6);
        Vehiculo combi = agencia.buscarVehiculo("AB123CD");
        Vehiculo micro = agencia.buscarVehiculo("AC456EF");
        ServicioAdicional excursion = agencia.buscarServicioAdicional(2);
        Tour cultural = agencia.buscarTour("CUL-01");
        Tour naturaleza = agencia.buscarTour("NAT-01");
        Tour aventura = agencia.buscarTour("AVE-01");

        // ---- salida 1: cultural, a 65 días (con promoción por anticipación), con guía y vehículo, confirmada ----
        LocalDate inicio1 = hoy.plusDays(65);
        Salida salida1 = agencia.programarSalida(inicio1, inicio1.plusDays(2), 2, 10, cultural);
        asignacion.asignarGuia(salida1, guiaCultural, agencia.getSalidas());
        asignacion.asignarVehiculo(salida1, combi, agencia.getSalidas());

        // un grupo de 6 (promoción por grupo), pagado completo: parte en tarjeta en 3 cuotas (con recargo)
        Reserva grupo = agencia.crearReserva(salida1, agencia.buscarCliente("31222333"), vendedor3, hoy.minusDays(5));
        grupo.agregarPasajero("Beto", "Gómez", "40000001", LocalDate.of(1985, 3, 10), TipoPasajero.ADULTO, "Rita 11-4000-0001", null, null);
        grupo.agregarPasajero("Rita", "Gómez", "40000002", LocalDate.of(1987, 6, 22), TipoPasajero.ADULTO, "Beto 11-4000-0002", null, null);
        grupo.agregarPasajero("Tomás", "Gómez", "40000003", inicio1.minusYears(9), TipoPasajero.MENOR, "Beto 11-4000-0001", null, null);
        grupo.agregarPasajero("Sofía", "Gómez", "40000004", inicio1.minusYears(7), TipoPasajero.MENOR, "Beto 11-4000-0001", null, null);
        grupo.agregarPasajero("Nilda", "Rossi", "40000005", LocalDate.of(1956, 1, 15), TipoPasajero.JUBILADO, "Beto 11-4000-0001", "Hipertensión", null);
        grupo.agregarPasajero("Hugo", "Rossi", "40000006", LocalDate.of(1954, 9, 3), TipoPasajero.JUBILADO, "Beto 11-4000-0001", null, "Diabético");
        calculadora.recalcularTotal(grupo);
        grupo.crearPago(hoy.minusDays(4), redondear(grupo.getTotal() / 2), MedioPago.TARJETA, 3);
        grupo.crearPago(hoy.minusDays(3), grupo.calcularSaldo(), MedioPago.TRANSFERENCIA, 1);

        // una pareja con una excursión, con la mitad pagada (queda saldo pendiente)
        Reserva pareja = agencia.crearReserva(salida1, agencia.buscarCliente("30111222"), vendedor3, hoy.minusDays(2));
        pareja.agregarPasajero("Ana", "Pérez", "40000007", LocalDate.of(1990, 1, 1), TipoPasajero.ADULTO, "Luis 11-4000-0007", "Asma", null);
        pareja.agregarPasajero("Luis", "Pérez", "40000008", LocalDate.of(1988, 4, 4), TipoPasajero.ADULTO, "Ana 11-4000-0008", null, null);
        pareja.contratarServicio(pareja.getPasajeros().get(0), excursion);
        calculadora.recalcularTotal(pareja);
        pareja.crearPago(hoy.minusDays(1), redondear(pareja.getTotal() / 2), MedioPago.EFECTIVO, 1);

        // una persona sola con apenas el 10% pagado: sigue pendiente
        Reserva sola = agencia.crearReserva(salida1, agencia.buscarCliente("32333444"), vendedor5, hoy);
        sola.agregarPasajero("Carla", "Ruiz", "40000009", LocalDate.of(1993, 8, 8), TipoPasajero.ADULTO, "Mario 56-9-4000-0009", null, "Vegetariana");
        calculadora.recalcularTotal(sola);
        sola.crearPago(hoy, redondear(sola.getTotal() / 10), MedioPago.EFECTIVO, 1);

        // ---- salida 2: naturaleza, a 90 días, sin guía ni vehículo todavía (para que el coordinador los asigne) ----
        LocalDate inicio2 = hoy.plusDays(90);
        Salida salida2 = agencia.programarSalida(inicio2, inicio2.plusDays(3), 4, 20, naturaleza);
        Reserva diego = agencia.crearReserva(salida2, agencia.buscarCliente("33444555"), vendedor5, hoy.minusDays(1));
        diego.agregarPasajero("Diego", "Fernández", "40000010", LocalDate.of(1980, 2, 20), TipoPasajero.ADULTO, "Lucía 598-4000-0010", null, null);
        diego.agregarPasajero("Lucía", "Fernández", "40000011", LocalDate.of(1982, 5, 30), TipoPasajero.ADULTO, "Diego 598-4000-0011", null, null);
        calculadora.recalcularTotal(diego);
        diego.crearPago(hoy, redondear(diego.getTotal() * 0.35), MedioPago.TRANSFERENCIA, 1);

        // ---- salida 3: aventura, a 45 días, con guía y micro, confirmada (el seguro es obligatorio) ----
        LocalDate inicio3 = hoy.plusDays(45);
        Salida salida3 = agencia.programarSalida(inicio3, inicio3.plusDays(1), 2, 8, aventura);
        asignacion.asignarGuia(salida3, guiaAventura, agencia.getSalidas());
        asignacion.asignarVehiculo(salida3, micro, agencia.getSalidas());
        Reserva elena = agencia.crearReserva(salida3, agencia.buscarCliente("34555666"), vendedor3, hoy.minusDays(3));
        elena.agregarPasajero("Elena", "Martínez", "40000012", LocalDate.of(1995, 11, 11), TipoPasajero.ADULTO, "Pedro 11-4000-0012", null, null);
        elena.agregarPasajero("Julián", "Martínez", "40000013", inicio3.minusYears(16), TipoPasajero.MENOR, "Elena 11-4000-0012", null, null);
        calculadora.recalcularTotal(elena);   // el seguro obligatorio ya se agregó solo a cada pasajero
        elena.crearPago(hoy.minusDays(2), elena.getTotal(), MedioPago.EFECTIVO, 1);

        // ---- salida 4: cultural, ya realizada hace unos días: una reserva finalizada y una cancelada por el cliente ----
        LocalDate inicio4 = hoy.minusDays(25);
        Salida salida4 = agencia.programarSalida(inicio4, inicio4.plusDays(2), 2, 10, cultural);
        asignacion.asignarGuia(salida4, guiaCultural, agencia.getSalidas());
        asignacion.asignarVehiculo(salida4, combi, agencia.getSalidas());

        Reserva facundo = agencia.crearReserva(salida4, agencia.buscarCliente("35666777"), vendedor3, hoy.minusDays(50));
        facundo.agregarPasajero("Facundo", "López", "40000014", LocalDate.of(1991, 7, 7), TipoPasajero.ADULTO, "Marina 55-4000-0014", null, null);
        facundo.agregarPasajero("Marina", "Souza", "40000015", LocalDate.of(1992, 9, 9), TipoPasajero.ADULTO, "Facundo 55-4000-0015", null, null);
        facundo.contratarServicio(facundo.getPasajeros().get(0), excursion);
        calculadora.recalcularTotal(facundo);
        facundo.crearPago(hoy.minusDays(48), facundo.getTotal(), MedioPago.TRANSFERENCIA, 1);

        Reserva anulada = agencia.crearReserva(salida4, agencia.buscarCliente("30111222"), vendedor5, hoy.minusDays(55));
        anulada.agregarPasajero("Marcos", "Pérez", "40000016", LocalDate.of(1989, 12, 1), TipoPasajero.ADULTO, "Ana 11-4000-0016", null, null);
        calculadora.recalcularTotal(anulada);
        anulada.crearPago(hoy.minusDays(54), anulada.getTotal(), MedioPago.EFECTIVO, 1);
        anulada.cancelar(hoy.minusDays(40), "El cliente no pudo viajar", OrigenCancelacion.CLIENTE);

        // ---- salida 5: cultural, a 40 días (sin promoción por anticipación), sin guía ni vehículo todavía:
        // un grupo de 6 que recibe la promoción por grupo ----
        LocalDate inicio5 = hoy.plusDays(40);
        Salida salida5 = agencia.programarSalida(inicio5, inicio5.plusDays(2), 2, 10, cultural);
        Reserva familia = agencia.crearReserva(salida5, agencia.buscarCliente("35666777"), vendedor5, hoy);
        familia.agregarPasajero("Facundo", "López", "40000017", LocalDate.of(1984, 2, 2), TipoPasajero.ADULTO, "Marina 55-4000-0017", null, null);
        familia.agregarPasajero("Marina", "Souza", "40000018", LocalDate.of(1986, 4, 4), TipoPasajero.ADULTO, "Facundo 55-4000-0018", null, null);
        familia.agregarPasajero("Bruno", "López", "40000019", inicio5.minusYears(14), TipoPasajero.MENOR, "Facundo 55-4000-0017", null, null);
        familia.agregarPasajero("Alma", "López", "40000020", inicio5.minusYears(10), TipoPasajero.MENOR, "Facundo 55-4000-0017", null, null);
        familia.agregarPasajero("Rosa", "Souza", "40000021", LocalDate.of(1958, 6, 6), TipoPasajero.JUBILADO, "Marina 55-4000-0018", null, null);
        familia.agregarPasajero("Raúl", "Souza", "40000022", LocalDate.of(1957, 8, 8), TipoPasajero.JUBILADO, "Marina 55-4000-0018", null, null);
        calculadora.recalcularTotal(familia);
        familia.crearPago(hoy, redondear(familia.getTotal() * 0.4), MedioPago.TRANSFERENCIA, 1);

        // ---- salida 6: cultural, empieza hoy, con guía y vehículo, confirmada y con la lista sin emitir:
        // el coordinador puede emitir la lista, iniciarla y finalizarla para ver el ciclo completo ----
        Salida salida6 = agencia.programarSalida(hoy, hoy.plusDays(2), 2, 10, cultural);
        asignacion.asignarGuia(salida6, guiaCultural, agencia.getSalidas());
        asignacion.asignarVehiculo(salida6, combi, agencia.getSalidas());
        Reserva hoyEmpieza = agencia.crearReserva(salida6, agencia.buscarCliente("33444555"), vendedor3, hoy.minusDays(20));
        hoyEmpieza.agregarPasajero("Valentina", "Sosa", "40000023", LocalDate.of(1994, 3, 3), TipoPasajero.ADULTO, "Diego 598-4000-0023", null, null);
        hoyEmpieza.agregarPasajero("Mateo", "Sosa", "40000024", LocalDate.of(1992, 5, 5), TipoPasajero.ADULTO, "Valentina 598-4000-0024", null, null);
        calculadora.recalcularTotal(hoyEmpieza);
        hoyEmpieza.crearPago(hoy.minusDays(18), hoyEmpieza.getTotal(), MedioPago.TRANSFERENCIA, 1);

        salida4.emitirListaPasajeros();
        salida4.iniciarSalida(hoy);
        salida4.finalizarSalida();   // queda pendiente de liquidar el guía
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
