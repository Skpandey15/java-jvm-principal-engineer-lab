package lab.order.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/** Stock level per product. Changed only through the atomic updates in {@link InventoryRepository}. */
@Entity
public class Inventory {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(nullable = false)
    private int available;

    protected Inventory() {
        // for JPA
    }

    public Long getProductId() {
        return productId;
    }

    public int getAvailable() {
        return available;
    }
}
