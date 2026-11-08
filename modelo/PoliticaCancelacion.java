package modelo;

import modelo.enums.OrigenCancelacion;
import java.io.Serializable;

import java.time.LocalDate;

public class PoliticaCancelacion implements Serializable {
    // devuelve el porcentaje (de 0 a 1) del importe pagado que corresponde devolver
    public double calcularPorcentaje(LocalDate hoy, LocalDate fechaSalida, OrigenCancelacion origen) {
        if (hoy == null || fechaSalida == null) {
            throw new IllegalArgumentException("Las fechas son obligatorias.");
        }
        if (origen == null) {
            throw new IllegalArgumentException("El origen de la cancelación es obligatorio.");
        }
        if (origen == OrigenCancelacion.AGENCIA) {
            return 1.0;
        }
        if (origen == OrigenCancelacion.FALTA_PAGO) {
            return 0.0;    // la seña no se devuelve si se anula por falta de pago
        }
        if (!hoy.isAfter(fechaSalida.minusDays(30))) {
            return 0.90;
        }
        if (!hoy.isAfter(fechaSalida.minusDays(15))) {
            return 0.50;
        }
        if (!hoy.isAfter(fechaSalida.minusDays(7))) {
            return 0.20;
        }
        return 0.0;
    }
}