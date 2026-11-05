package modelo;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import modelo.enums.TipoPasajero;

public class Pasajero {

    // Porcentajes de la tarifa base (RN10 / RF12)
    private static final double PORC_MENOR_3 = 0.0;
    private static final double PORC_MENOR_12 = 0.6;
    private static final double PORC_JUBILADO = 0.9;
    private static final double PORC_COMPLETA = 1.0;

    private String nombre;
    private String apellido;
    private String documento;
    private LocalDate fechaNacimiento;
    private TipoPasajero tipo;
    private String contactoEmergencia;
    private String restriccionesSalud;
    private String restriccionesAlimentarias;
    private final List<ServicioContratado> servicios = new ArrayList<>();

    public Pasajero(String nombre, String apellido, String documento,
                    LocalDate fechaNacimiento, TipoPasajero tipo,
                    String contactoEmergencia, String restriccionesSalud,
                    String restriccionesAlimentarias) {
        if (nombre == null || nombre.isBlank() || apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("Nombre y apellido son obligatorios.");
        }
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio.");
        }
        if (fechaNacimiento == null || fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento es inválida.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de pasajero es obligatorio.");
        }
        if (contactoEmergencia == null || contactoEmergencia.isBlank()) {
            throw new IllegalArgumentException("El contacto de emergencia es obligatorio.");
        }
        this.nombre = nombre;
        this.apellido = apellido;
        this.documento = documento;
        this.fechaNacimiento = fechaNacimiento;
        this.tipo = tipo;
        this.contactoEmergencia = contactoEmergencia;
        this.restriccionesSalud = restriccionesSalud == null ? "" : restriccionesSalud;
        this.restriccionesAlimentarias = restriccionesAlimentarias == null ? "" : restriccionesAlimentarias;
    }


    public int calcularEdad(LocalDate fechaSalida) {
        if (fechaSalida == null || fechaSalida.isBefore(fechaNacimiento)) {
            throw new IllegalArgumentException("La fecha de salida es inválida.");
        }
        return Period.between(fechaNacimiento, fechaSalida).getYears();
    }


    public double calcularPrecio(double tarifa, LocalDate fechaSalida) {
        if (tarifa < 0) {
            throw new IllegalArgumentException("La tarifa no puede ser negativa.");
        }
        int edad = calcularEdad(fechaSalida);
        if (edad < 3) {
            return tarifa * PORC_MENOR_3;
        }
        if (edad < 12) {
            return tarifa * PORC_MENOR_12;
        }
        if (tipo == TipoPasajero.JUBILADO) {
            return tarifa * PORC_JUBILADO;
        }
        return tarifa * PORC_COMPLETA;
    }

    public void agregarServicio(ServicioAdicional servicio) {
        servicios.add(new ServicioContratado(servicio));
    }

    public double calcularTotalServicios() {
        double total = 0;
        for (ServicioContratado s : servicios) {
            total += s.calcularSubtotal();
        }
        return total;
    }

    public boolean tieneServicio(modelo.enums.TipoServicioAdicional tipoServicio) {
        for (ServicioContratado s : servicios) {
            if (s.getServicio().getTipo() == tipoServicio) {
                return true;
            }
        }
        return false;
    }

    public List<ServicioContratado> getServicios() {
        return Collections.unmodifiableList(servicios);
    }

    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getDocumento() { return documento; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public TipoPasajero getTipo() { return tipo; }
    public String getContactoEmergencia() { return contactoEmergencia; }
    public String getRestriccionesSalud() { return restriccionesSalud; }
    public String getRestriccionesAlimentarias() { return restriccionesAlimentarias; }

    @Override
    public String toString() {
        return apellido + ", " + nombre + " (" + documento + ")";
    }
}
