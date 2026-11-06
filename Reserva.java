        validarModificable();

        if (promocion != null) {
            int diasAnticipacion = Math.toIntExact(
                    ChronoUnit.DAYS.between(fecha, salida.getFechaInicio())
            );

            if (!promocion.esAplicable(pasajeros.size(), diasAnticipacion)) {
                throw new IllegalArgumentException(
                        "La promoción no es aplicable a esta reserva.");
            }
        }

        this.promocionAplicada = promocion;
    }

    public Promocion getPromocionAplicada() {
        return promocionAplicada;
    }

    private boolean seniaCubierta() {
        return calcularTotalAbonado() + 0.005
                >= calcularTotalConAjustes() * PORCENTAJE_SENIA;
    }

    private void validarModificable() {
        if (estado != EstadoReserva.PENDIENTE
                && estado != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException(
                    "La reserva está " + estado
                    + " y no admite cambios.");
        }
    }

    private void validarPasajero(
            Pasajero pasajero,
            List<Pasajero> existentes) {

        if (pasajero == null) {
            throw new IllegalArgumentException(
                    "El pasajero es obligatorio.");
        }

        for (Pasajero e : existentes) {
            if (e.getDocumento().equals(pasajero.getDocumento())) {
                throw new IllegalArgumentException(
                        "El pasajero con documento "
                        + pasajero.getDocumento()
                        + " ya está en la reserva.");
            }
        }

        if (!salida.getTour().cumpleCondiciones(
                pasajero, salida.getFechaInicio())) {
            throw new IllegalArgumentException(
                    "El pasajero " + pasajero.getDocumento()
                    + " no cumple las condiciones del tour.");
        }
    }

    public int getNumero() {
        return numero;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public double getTotal() {
        return total;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Salida getSalida() {
        return salida;
    }

    public Vendedor getVendedor() {
        return vendedor;
    }

    public int cantidadPasajeros() {
        return pasajeros.size();
    }

    public List<Pasajero> getPasajeros() {
        return Collections.unmodifiableList(pasajeros);
    }

    public List<Pago> getPagos() {
        return Collections.unmodifiableList(pagos);
    }

    public List<NotaAjuste> getNotasAjuste() {
        return Collections.unmodifiableList(notasAjuste);
    }

    public Cancelacion getCancelacion() {
        return cancelacion;
    }
}
