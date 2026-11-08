package excepcion;

public class CupoInsuficienteException extends ReglaNegocioException {
    public CupoInsuficienteException(String message) {
        super(message);
    }
}
