package modelo;

public class CoordinadorOperaciones extends Personal {

    public CoordinadorOperaciones(int legajo, String nombre, String documento, String telefono) {
        super(legajo, nombre, documento, telefono);
    }

    @Override
    public String getRol() {
        return "Coordinador de operaciones";
    }

    public void asignarGuia(Salida salida, Guia guia) {
        exigir(salida).asignarGuia(guia);
    }

    public void asignarVehiculo(Salida salida, Vehiculo vehiculo) {
        exigir(salida).asignarVehiculo(vehiculo);
    }

    
    public String emitirListaPasajeros(Salida salida) {
        StringBuilder sb = new StringBuilder();
        sb.append("Lista de pasajeros - ").append(exigir(salida)).append("\n");
        int n = 1;
        for (Pasajero p : salida.emitirListaPasajeros()) {
            sb.append(String.format("%d. %s, %s | Doc: %s | Emergencia: %s | Salud: %s | Alimentarias: %s%n",
                    n++, p.getApellido(), p.getNombre(), p.getDocumento(), p.getContactoEmergencia(),
                    p.getRestriccionesSalud().isBlank() ? "-" : p.getRestriccionesSalud(),
                    p.getRestriccionesAlimentarias().isBlank() ? "-" : p.getRestriccionesAlimentarias()));
        }
        return sb.toString();
    }

    public void iniciarSalida(Salida salida) {
        exigir(salida).iniciar();
    }

    public void finalizarSalida(Salida salida) {
        exigir(salida).finalizar();
    }

    private Salida exigir(Salida salida) {
        if (salida == null) {
            throw new IllegalArgumentException("La salida es obligatoria.");
        }
        return salida;
    }
}
