package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.Period;

import modelo.enums.TipoPasajero;

public class Pasajero {
    private String nombre;
    private String apellido;
    private String documento;
    private LocalDate fechaNacimiento;
    private TipoPasajero tipo;
    private String contactoEmergencia;
    private String restriccionesSalud;
    private String restriccionesAlimentarias;
    private List<ServicioContratado> serviciosContratados;

    public Pasajero(String nombre, String apellido, String documento, LocalDate fechaNacimiento, TipoPasajero tipo, String contactoEmergencia, String restriccionesSalud, String restriccionesAlimentarias) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.documento = documento;
        setFechaNacimiento(fechaNacimiento);
        this.tipo = tipo;
        this.contactoEmergencia = contactoEmergencia;
        this.restriccionesSalud = restriccionesSalud;
        this.restriccionesAlimentarias = restriccionesAlimentarias;
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
        this.nombre = nombre;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null || fechaNacimiento.isAfter(LocalDate.now())) {

            throw new IllegalArgumentException("La fecha de nacimiento no puede ser nula ni futura.");
        }
        this.fechaNacimiento = fechaNacimiento;
    }

    public void setTipo(TipoPasajero tipo) {
        this.tipo = tipo;
    }

    public void setContactoEmergencia(String contactoEmergencia) {
        this.contactoEmergencia = contactoEmergencia;
    }

    public void setRestriccionesSalud(String restriccionesSalud) {
        this.restriccionesSalud = restriccionesSalud;
    }

    public void setRestriccionesAlimentarias(
            String restriccionesAlimentarias) {
        this.restriccionesAlimentarias = restriccionesAlimentarias;
    }

    public void agregarServicio(ServicioAdicional servicioAdicional) {
        for (ServicioContratado servicio : serviciosContratados) {
            if (servicio.getServicioAdicional() == servicioAdicional) {
                throw new IllegalArgumentException("El pasajero ya tiene contratado este servicio.");
            }
        }
        ServicioContratado nuevo = new ServicioContratado(servicioAdicional, servicioAdicional.getPrecio());
        serviciosContratados.add(nuevo);
    }

    public List<ServicioContratado> getServiciosContratados() {
        return new ArrayList<>(serviciosContratados);
    }

    public int calcularEdad(LocalDate fechaReferencia) {
        return Period.between(fechaNacimiento, fechaReferencia).getYears();
    }
}
