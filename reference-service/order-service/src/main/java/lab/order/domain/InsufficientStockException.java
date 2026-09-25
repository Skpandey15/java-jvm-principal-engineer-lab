package lab.order.domain;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(Long productId, int requested) {
        super("not enough stock for product " + productId + " (requested " + requested + ")");
    }
}
