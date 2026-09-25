package lab.order.domain;

import java.util.UUID;

public class InvalidOrderStateException extends RuntimeException {

    public InvalidOrderStateException(UUID orderId, OrderStatus status, String action) {
        super("cannot " + action + " order " + orderId + " in status " + status);
    }
}
