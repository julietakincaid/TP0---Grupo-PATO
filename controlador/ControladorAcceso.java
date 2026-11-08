package controlador;

import excepcion.ReglaNegocioException;
import modelo.Administrador;
import modelo.CoordinadorOperaciones;
import modelo.Guia;
import modelo.Personal;
import modelo.Vendedor;
import servicio.Agencia;

// Ingreso al sistema: el empleado se identifica con su legajo y el programa descubre su rol.
public class ControladorAcceso {
    public static final String ADMINISTRADOR = "Administrador";
    public static final String COORDINADOR = "Coordinador de operaciones";
    public static final String VENDEDOR = "Vendedor";
    public static final String GUIA = "Guía";

    private final Agencia agencia;
    private final Pantalla pantalla;

    public ControladorAcceso(Agencia agencia, Pantalla pantalla) {
        if (agencia == null || pantalla == null) {
            throw new IllegalArgumentException("La agencia y la pantalla son obligatorias.");
        }
        this.agencia = agencia;
        this.pantalla = pantalla;
    }

    // si el legajo existe, da la bienvenida y devuelve al empleado; si no, muestra el error y devuelve null
    public Personal ingresar(int legajo) {
        try {
            Personal usuario = agencia.buscarPersonal(legajo);
            pantalla.mostrarMensaje("Bienvenido/a, " + usuario.getNombre() + " (" + getRol(usuario) + ").");
            return usuario;
        } catch (ReglaNegocioException e) {
            pantalla.mostrarError(e.getMessage());
            return null;
        }
    }

    // el rol del empleado: una de las constantes de arriba
    public String getRol(Personal usuario) {
        if (usuario instanceof Administrador) {
            return ADMINISTRADOR;
        }
        if (usuario instanceof CoordinadorOperaciones) {
            return COORDINADOR;
        }
        if (usuario instanceof Vendedor) {
            return VENDEDOR;
        }
        if (usuario instanceof Guia) {
            return GUIA;
        }
        return "Empleado";
    }
}