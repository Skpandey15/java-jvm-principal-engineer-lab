package lab.order.payment;

import java.util.UUID;

public class PaymentFailedException extends RuntimeException {

    public PaymentFailedException(UUID orderId, String reason) {
        super("payment for order " + orderId + " failed: " + reason);
    }
}
