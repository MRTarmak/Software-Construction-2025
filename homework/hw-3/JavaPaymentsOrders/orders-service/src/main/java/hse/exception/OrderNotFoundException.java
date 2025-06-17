package hse.exception;

public class OrderNotFoundException extends OrdersException {
    public OrderNotFoundException(String message) {
        super(message);
    }

    OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
