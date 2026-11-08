package modelo;

import java.time.LocalDate;
import modelo.enums.EstadoSalida;

public class Administrador extends Personal {
    private static final double PORCENTAJE_ADICIONALES = 0.05;

    public Administrador(int legajo, String nombre, String documento, String telefono) {
        super(legajo, nombre, documento, telefono);
    }

    @Override
    public String getRol() {
        return "Administrador";
    }


    public LiquidacionGuia liquidarHonorarios(Salida salida, int numero, LocalDate fecha) {
        if (salida == null) {
            throw new IllegalArgumentException("La salida es obligatoria.");
        }
        if (salida.getEstado() != EstadoSalida.FINALIZADA) {
            throw new IllegalStateException("Solo se liquidan salidas finalizadas.");
        }
        Guia guia = salida.getGuia();
        if (guia == null) {
            throw new IllegalStateException("La salida no tiene guía asignado.");
        }
        double honorarios = guia.calcularHonorarios(salida);
        double comision = salida.calcularVentasAdicionales() * PORCENTAJE_ADICIONALES;
        LiquidacionGuia liquidacion = new LiquidacionGuia(numero, fecha, guia, honorarios, comision);
        salida.registrarLiquidacion(liquidacion);
        return liquidacion;
    }
}
