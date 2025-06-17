package hse.exception;

public class PaymentsException extends RuntimeException {
    public PaymentsException(String message) {
        super(message);
    }

    public PaymentsException(String message, Throwable cause) {
        super(message, cause);
    }
}
