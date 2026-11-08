package vista;

import controlador.ControladorAcceso;
import controlador.ControladorAdministracion;
import controlador.ControladorCatalogo;
import controlador.ControladorOperaciones;
import controlador.ControladorReservas;
import modelo.Personal;

import java.time.LocalDate;

// Pantalla de entrada: ingreso con legajo y fecha del sistema. Desde acá se abre el menú del rol.
// La fecha de trabajo es la del día en que se abre el programa.
public class VistaPrincipal {
    private final Consola consola;
    private final ControladorAcceso controladorAcceso;
    private final ControladorOperaciones controladorOperaciones;
    private final ControladorReservas controladorReservas;
    private final ControladorCatalogo controladorCatalogo;
    private final ControladorAdministracion controladorAdministracion;
    private final Runnable guardar;
    private final LocalDate fechaActual;

    public VistaPrincipal(Consola consola, ControladorAcceso controladorAcceso,
                          ControladorOperaciones controladorOperaciones, ControladorReservas controladorReservas,
                          ControladorCatalogo controladorCatalogo, ControladorAdministracion controladorAdministracion,
                          Runnable guardar) {
        this.consola = consola;
        this.controladorAcceso = controladorAcceso;
        this.controladorOperaciones = controladorOperaciones;
        this.controladorReservas = controladorReservas;
        this.controladorCatalogo = controladorCatalogo;
        this.controladorAdministracion = controladorAdministracion;
        this.guardar = guardar;
        this.fechaActual = LocalDate.now();
    }

    public void ejecutar() {
        consola.titulo("RUMBO SUR - Agencia de turismo");
        controlarPlazos();
        int opcion;
        do {
            consola.linea();
            consola.info("Fecha del sistema: " + consola.formatear(fechaActual));
            opcion = consola.menu("🌎 Menú principal", new String[]{"🔑 Ingresar con mi legajo"}, "🚪 Salir del programa");
            if (opcion == 1) {
                ingresar();
            }
        } while (opcion != 0);
        consola.texto("¡Hasta luego!");
    }

    // el controlador da la bienvenida o muestra el error; la vista solo abre el menú si el ingreso salió bien
    private void ingresar() {
        int legajo = consola.leerEntero("Legajo");
        Personal usuario = controladorAcceso.ingresar(legajo);
        if (usuario != null) {
            abrirMenu(usuario);
            guardar.run();   // al cerrar la sesión se guardan los datos
        }
    }

    // el controlador dice cuál es el rol y la vista abre el menú que corresponde
    private void abrirMenu(Personal usuario) {
        String rol = controladorAcceso.getRol(usuario);
        if (rol.equals(ControladorAcceso.GUIA)) {
            new VistaGuia(consola, controladorOperaciones, usuario).ejecutar();
        } else if (rol.equals(ControladorAcceso.COORDINADOR)) {
            new VistaCoordinador(consola, controladorOperaciones, usuario, fechaActual, guardar).ejecutar();
        } else if (rol.equals(ControladorAcceso.VENDEDOR)) {
            new VistaVendedor(consola, controladorReservas, usuario, fechaActual, guardar).ejecutar();
        } else if (rol.equals(ControladorAcceso.ADMINISTRADOR)) {
            new VistaAdministrador(consola, controladorCatalogo, controladorAdministracion, usuario, fechaActual,
                    guardar).ejecutar();
        } else {
            consola.aviso("El menú de este rol todavía no está disponible.");
        }
    }

    // el controlador anula las reservas con deuda vencida, cancela las salidas sin cupo mínimo
    // y avisa lo que hizo; si hubo cambios, se guardan los datos
    private void controlarPlazos() {
        if (controladorOperaciones.controlarPlazos(fechaActual)) {
            guardar.run();
        }
    }
}