package carreterasmojadas.exception;

public class SimulacionException extends RuntimeException {
    public SimulacionException(String mensaje) { super(mensaje); }
    public SimulacionException(String mensaje, Throwable causa) { super(mensaje, causa); }
}
