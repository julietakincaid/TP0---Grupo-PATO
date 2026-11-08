package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.Period;
import java.io.Serializable;

import excepcion.ReglaNegocioException;
import modelo.enums.TipoPasajero;

public class Pasajero implements Serializable{
    private static final int EDAD_MAYORIA = 18;
    private static final int EDAD_JUBILADO = 60;    // supuesto: jubilado desde los 60

    private String nombre;
    private String apellido;
    private String documento;
    private LocalDate fechaNacimiento;
    private TipoPasajero tipo;
    private String contactoEmergencia;
    private String restriccionesSalud;
    private String restriccionesAlimentarias;
    private List<ServicioContratado> serviciosContratados;

    Pasajero(String nombre, String apellido, String documento, LocalDate fechaNacimiento, TipoPasajero tipo, String contactoEmergencia, String restriccionesSalud, String restriccionesAlimentarias) {
        setNombre(nombre);
        setApellido(apellido);
        setDocumento(documento);
        setFechaNacimiento(fechaNacimiento);
        setTipo(tipo);
        setContactoEmergencia(contactoEmergencia);
        setRestriccionesSalud(restriccionesSalud);
        setRestriccionesAlimentarias(restriccionesAlimentarias);
        this.serviciosContratados = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }
    public String getApellido() {
        return apellido;
    }
    public String getDocumento() {
        return documento;
    }
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }
    public TipoPasajero getTipo() {
        return tipo;
    }
    public String getContactoEmergencia() {
        return contactoEmergencia;
    }
    public String getRestriccionesSalud() {
        return restriccionesSalud;
    }
    public String getRestriccionesAlimentarias() {
        return restriccionesAlimentarias;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        this.nombre = nombre.strip();
    }

    public void setApellido(String apellido) {
        if (apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio.");
        }
        this.apellido = apellido.strip();
    }

    void setDocumento(String documento) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio.");
        }

        // saco puntos, guiones y espacios, y paso a mayúsculas
        String limpio = documento.replace(".", "").replace("-", "").replace(" ", "").toUpperCase();

        if (limpio.length() < 6 || limpio.length() > 15) {
            throw new IllegalArgumentException("El documento debe tener entre 6 y 15 caracteres.");
        }

        // reviso que cada caracter sea una letra o un número
        for (int i = 0; i < limpio.length(); i++) {
            if (!Character.isLetterOrDigit(limpio.charAt(i))) {
                throw new IllegalArgumentException("El documento solo puede contener letras y números.");
            }
        }

        this.documento = limpio;
    }

    void setFechaNacimiento(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null || fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser nula ni futura.");
        }
        this.fechaNacimiento = fechaNacimiento;
    }

    void setTipo(TipoPasajero tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de pasajero es obligatorio.");
        }
        this.tipo = tipo;
    }

    public void setContactoEmergencia(String contactoEmergencia) {
        if (contactoEmergencia == null || contactoEmergencia.isBlank()) {
            throw new IllegalArgumentException("El contacto de emergencia es obligatorio.");
        }
        this.contactoEmergencia = contactoEmergencia.strip();
    }

    public void setRestriccionesSalud(String restriccionesSalud) {
        if (restriccionesSalud == null || restriccionesSalud.isBlank()) {
            this.restriccionesSalud = "Ninguna";
        } else {
            this.restriccionesSalud = restriccionesSalud.strip();
        }
    }

    public void setRestriccionesAlimentarias(String restriccionesAlimentarias) {
        if (restriccionesAlimentarias == null || restriccionesAlimentarias.isBlank()) {
            this.restriccionesAlimentarias = "Ninguna";
        } else {
            this.restriccionesAlimentarias = restriccionesAlimentarias.strip();
        }
    }

    public int calcularEdad(LocalDate fechaReferencia) {
        if (fechaReferencia == null || fechaReferencia.isBefore(fechaNacimiento)) {
            throw new IllegalArgumentException("La fecha de referencia no es válida.");
        }
        return Period.between(fechaNacimiento, fechaReferencia).getYears();
    }

    boolean tipoCoherenteConEdad(LocalDate fechaSalida) {
        int edad = calcularEdad(fechaSalida);

        if (tipo == TipoPasajero.MENOR) {
            return edad < EDAD_MAYORIA;
        } else if (tipo == TipoPasajero.JUBILADO) {
            return edad >= EDAD_JUBILADO;
        } else {
            return edad >= EDAD_MAYORIA;   // ADULTO
        }
    }

    // servicios contratados (se manejan desde reservas)
    void agregarServicio(ServicioAdicional servicioAdicional) throws ReglaNegocioException {
        if (servicioAdicional == null) {
            throw new IllegalArgumentException("El servicio no puede ser nulo.");
        }
        if (tieneServicio(servicioAdicional)) {
            throw new ReglaNegocioException("El pasajero ya tiene contratado este servicio.");
        }
        serviciosContratados.add(new ServicioContratado(servicioAdicional));
    }

    void quitarServicio(ServicioAdicional servicioAdicional) throws ReglaNegocioException {
        if (servicioAdicional == null) {
            throw new IllegalArgumentException("El servicio no puede ser nulo.");
        }
        for (ServicioContratado servicio : serviciosContratados) {
            if (servicio.getServicioAdicional().equals(servicioAdicional)) {
                serviciosContratados.remove(servicio);
                return;
            }
        }
        throw new ReglaNegocioException("El pasajero no tiene contratado ese servicio.");
    }

    public boolean tieneServicio(ServicioAdicional servicioAdicional) {
        for (ServicioContratado servicio : serviciosContratados) {
            if (servicio.getServicioAdicional().equals(servicioAdicional)) {
                return true;
            }
        }
        return false;
    }

    public boolean tieneSeguro() {
        for (ServicioContratado servicio : serviciosContratados) {
            if (servicio.getServicioAdicional().esSeguro()) {
                return true;
            }
        }
        return false;
    }

    public List<ServicioContratado> getServiciosContratados() {
        return new ArrayList<>(serviciosContratados);
    }

    public double calcularTotalServicios() {
        double total = 0;
        for (ServicioContratado servicio : serviciosContratados) {
            total += servicio.calcularSubtotal();
        }
        return total;
    }

    void heredarServiciosDe(Pasajero otro) {
        serviciosContratados.addAll(otro.serviciosContratados);
    }

    @Override
    public String toString() {
        return apellido + ", " + nombre + " (DNI " + documento + ")";
    }
}
