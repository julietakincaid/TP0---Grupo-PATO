package prueba;

import excepcion.ReglaNegocioException;
import modelo.Administrador;
import modelo.Cliente;
import modelo.CoordinadorOperaciones;
import modelo.Guia;
import modelo.Reserva;
import modelo.Salida;
import modelo.ServicioAdicional;
import modelo.Tour;
import modelo.TourAventura;
import modelo.Vehiculo;
import modelo.Vendedor;
import modelo.enums.CategoriaTour;
import modelo.enums.MedioPago;
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

// El escenario común de las pruebas: una agencia con un tour cultural (tarifa baja de $100), un vendedor,
// un administrador, un coordinador, un guía, una combi, una cliente y una salida que empieza en 30 días
// (sin promoción por anticipación). Cada prueba crea el suyo, así no dependen unas de otras.
class Escenario {
    final LocalDate hoy = LocalDate.now();
    final Agencia agencia = new Agencia();
    final CalculadoraPrecioReserva calculadora = new CalculadoraPrecioReserva(agencia.getGestorPromociones());
    final ServicioAdicional excursion;
    final ServicioAdicional seguro;
    final Tour tour;
    final Vendedor vendedor;
    final Administrador administrador;
    final CoordinadorOperaciones coordinador;
    final Guia guia;
    final Vehiculo combi;
    final Cliente cliente;
    final Salida salida;
    private int proximoDocumento = 40000000;

    Escenario() throws ReglaNegocioException {
        excursion = new ServicioAdicional(1, "Excursión", "Opcional", TipoServicioAdicional.EXCURSION, 30);
        seguro = new ServicioAdicional(2, "Seguro de viaje", "Cobertura médica", TipoServicioAdicional.SEGURO, 50);
        agencia.registrarServicioAdicional(excursion);
        agencia.registrarServicioAdicional(seguro);

        tour = new Tour("T-01", "Museos", "Circuito cultural", 3, CategoriaTour.CULTURAL, 1, 10);
        tour.agregarActividad(1, 1, TipoActividad.VISITA, LocalTime.of(9, 0), "Visita", "Centro");
        agregarTarifas(tour);
        tour.agregarServicio(excursion, false);
        agencia.registrarTour(tour);

        vendedor = new Vendedor(1, "Vera", "27111222", "1144445555", 5);
        administrador = new Administrador(2, "Adri", "29111222", "1144445555");
        coordinador = new CoordinadorOperaciones(3, "Coco", "24111222", "1144445555");
        guia = new Guia(4, "Gus", "28111222", "1144445555", 1000, "Español", CategoriaTour.CULTURAL);
        agencia.registrarPersonal(vendedor);
        agencia.registrarPersonal(administrador);
        agencia.registrarPersonal(coordinador);
        agencia.registrarPersonal(guia);

        combi = new Vehiculo("AB123CD", TipoVehiculo.COMBI, 10);
        agencia.registrarVehiculo(combi);
        cliente = new Cliente("Ana", "Perez", "30111222", "1144445555", "ana@mail.com", "Argentina");
        agencia.registrarCliente(cliente);

        salida = nuevaSalida(30, 1, 10);
    }

    // temporada baja: de hace 10 días a 100 días ($100); media hasta 200 días ($150); alta hasta 400 días ($200)
    void agregarTarifas(Tour unTour) throws ReglaNegocioException {
        unTour.agregarTarifa(Temporada.BAJA, hoy.minusDays(10), hoy.plusDays(100), 100);
        unTour.agregarTarifa(Temporada.MEDIA, hoy.plusDays(101), hoy.plusDays(200), 150);
        unTour.agregarTarifa(Temporada.ALTA, hoy.plusDays(201), hoy.plusDays(400), 200);
    }

    // un tour de aventura de 2 días, para mayores de 14, con el seguro obligatorio
    TourAventura crearTourAventura(String codigo) throws ReglaNegocioException {
        TourAventura aventura = new TourAventura(codigo, "Rafting", "Descenso en el río", 2, 1, 8, 14, "Alta");
        aventura.agregarActividad(1, 1, TipoActividad.VISITA, LocalTime.of(9, 0), "Descenso", "Río");
        agregarTarifas(aventura);
        aventura.agregarServicio(seguro, true);
        agencia.registrarTour(aventura);
        return aventura;
    }

    // una salida del tour cultural que empieza dentro de la cantidad de días indicada (dura 3 días)
    Salida nuevaSalida(int diasHastaElInicio, int cupoMinimo, int cupoMaximo) throws ReglaNegocioException {
        LocalDate inicio = hoy.plusDays(diasHastaElInicio);
        return agencia.programarSalida(inicio, inicio.plusDays(2), cupoMinimo, cupoMaximo, tour);
    }

    void agregarAdulto(Reserva reserva) throws ReglaNegocioException {
        agregarPasajero(reserva, LocalDate.of(1990, 1, 1), TipoPasajero.ADULTO);
    }

    void agregarPasajero(Reserva reserva, LocalDate nacimiento, TipoPasajero tipo) throws ReglaNegocioException {
        reserva.agregarPasajero("Ana", "Perez", String.valueOf(proximoDocumento++), nacimiento, tipo,
                "Contacto 11-1111", null, null);
    }

    // una reserva de la cliente con la cantidad de adultos indicada y el total ya calculado
    Reserva reservaConAdultos(Salida unaSalida, int cantidad) throws ReglaNegocioException {
        Reserva reserva = agencia.crearReserva(unaSalida, cliente, vendedor, hoy);
        for (int i = 0; i < cantidad; i++) {
            agregarAdulto(reserva);
        }
        calculadora.recalcularTotal(reserva);
        return reserva;
    }

    // lo mismo, pero pagada por completo (queda confirmada)
    Reserva reservaPagada(Salida unaSalida, int cantidad) throws ReglaNegocioException {
        Reserva reserva = reservaConAdultos(unaSalida, cantidad);
        reserva.crearPago(hoy, reserva.getTotal(), MedioPago.EFECTIVO, 1);
        return reserva;
    }

    // una salida confirmada, con guía, vehículo y la lista de pasajeros emitida: lista para iniciarse
    // (empieza en 30 días, así que se la inicia pasando su fecha de inicio como "hoy")
    Salida salidaLista() throws ReglaNegocioException {
        Salida lista = nuevaSalida(30, 1, 10);
        reservaPagada(lista, 1);
        asignarGuiaYVehiculo(lista);
        lista.emitirListaPasajeros();
        return lista;
    }

    void asignarGuiaYVehiculo(Salida unaSalida) throws ReglaNegocioException {
        new ServicioAsignacion().asignarGuia(unaSalida, guia, agencia.getSalidas());
        new ServicioAsignacion().asignarVehiculo(unaSalida, combi, agencia.getSalidas());
    }

    // una salida que empezó hoy y ya finalizó, con una reserva que contrató una excursión de $30
    Salida salidaFinalizada() throws ReglaNegocioException {
        Salida terminada = agencia.programarSalida(hoy, hoy.plusDays(2), 1, 10, tour);
        Reserva reserva = agencia.crearReserva(terminada, cliente, vendedor, hoy.minusDays(20));
        agregarAdulto(reserva);
        reserva.contratarServicio(reserva.getPasajeros().get(0), excursion);
        calculadora.recalcularTotal(reserva);
        reserva.crearPago(hoy.minusDays(18), reserva.getTotal(), MedioPago.EFECTIVO, 1);
        asignarGuiaYVehiculo(terminada);
        terminada.emitirListaPasajeros();
        terminada.iniciarSalida(hoy);
        terminada.finalizarSalida();
        return terminada;
    }
}