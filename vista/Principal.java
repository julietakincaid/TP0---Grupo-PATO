import controlador.ControladorAcceso;
import controlador.ControladorOperaciones;
import excepcion.ReglaNegocioException;
import repositorio.RepositorioAgenciaArchivo;
import servicio.Agencia;
import servicio.ServicioAsignacion;
import vista.Consola;
import vista.VistaPrincipal;
import controlador.ControladorReservas;
import servicio.CalculadoraPrecioReserva;
import controlador.ControladorAdministracion;
import controlador.ControladorCatalogo;
import servicio.ServicioLiquidacion;
import servicio.ServicioReportes;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

// Arranque de la aplicación: carga los datos, crea y conecta los objetos, y guarda al salir.
public class Principal {
    private static final String RUTA_DATOS = "datos/agencia.dat";

    public static void main(String[] args) {
        boolean colores = !(args.length > 0 && args[0].equals("--sin-color"));
        Consola consola = new Consola(new Scanner(System.in), colores);
        RepositorioAgenciaArchivo repositorio = new RepositorioAgenciaArchivo(RUTA_DATOS);

        // 1. cargar los datos: si falla, el programa no sigue (así no pisa el archivo dañado)
        Agencia agencia;
        try {
            agencia = repositorio.cargar();
        } catch (IOException e) {
            consola.error("No se pudieron leer los datos de " + repositorio.getRuta() + ": " + e.getMessage());
            consola.error("El programa se cierra para no pisar el archivo.");
            return;
        }
        consola.info("Datos: " + repositorio.getRuta());

        // 2. primera ejecución: se cargan los datos de prueba (personal, catálogo, clientes, salidas y reservas)
        if (agencia.getPersonal().isEmpty()) {
            try {
                DatosIniciales.cargar(agencia);
                guardar(repositorio, agencia, consola);   // se guarda enseguida, así el archivo existe desde el primer arranque
                consola.aviso("Primera ejecución: se cargaron los datos de prueba. Legajos: 1 administrador, 2 coordinador, 3 y 5 vendedores, 4, 6 y 7 guías.");
            } catch (ReglaNegocioException e) {
                consola.error("No se pudieron cargar los datos iniciales: " + e.getMessage());
                return;
            }
        }

        // 3. crear y conectar los objetos
        ControladorAcceso controladorAcceso = new ControladorAcceso(agencia, consola);
        ControladorOperaciones controladorOperaciones = new ControladorOperaciones(agencia, new ServicioAsignacion(), consola);
        CalculadoraPrecioReserva calculadora = new CalculadoraPrecioReserva(agencia.getGestorPromociones());
        ControladorReservas controladorReservas = new ControladorReservas(agencia, calculadora, consola);
        ControladorCatalogo controladorCatalogo = new ControladorCatalogo(agencia, consola);
        ControladorAdministracion controladorAdministracion = new ControladorAdministracion(agencia, new ServicioLiquidacion(), new ServicioReportes(), consola);
        Runnable guardar = () -> guardar(repositorio, agencia, consola);
        VistaPrincipal vista = new VistaPrincipal(consola, controladorAcceso, controladorOperaciones, controladorReservas, controladorCatalogo, controladorAdministracion, guardar);

        // 4. usar el programa y, pase lo que pase, guardar al salir
        try {
            vista.ejecutar();
        } catch (NoSuchElementException e) {
            consola.aviso("Se cerró la entrada de datos.");
        } finally {
            guardar(repositorio, agencia, consola);
        }
    }

    private static void guardar(RepositorioAgenciaArchivo repositorio, Agencia agencia, Consola consola) {
        try {
            repositorio.guardar(agencia);
        } catch (IOException e) {
            consola.error("ATENCIÓN: no se pudieron guardar los datos: " + e.getMessage());
        }
    }
}