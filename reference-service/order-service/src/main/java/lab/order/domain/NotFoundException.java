package lab.order.domain;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String type, Object id) {
        super(type + " " + id + " not found");
    }
}
