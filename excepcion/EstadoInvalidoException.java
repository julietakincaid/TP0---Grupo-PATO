package excepcion;

public class EstadoInvalidoException extends ReglaNegocioException {
    public EstadoInvalidoException(String message) {
        super(message);
    }
}
