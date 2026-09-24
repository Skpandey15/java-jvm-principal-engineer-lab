package lab.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Order aggregate root. Named PurchaseOrder because ORDER is an SQL keyword. */
@Entity
@Table(name = "purchase_order")
public class PurchaseOrder {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    /** Optimistic locking (WP-06). */
    @Version
    private long version;

    protected PurchaseOrder() {
        // for JPA
    }

    public PurchaseOrder(String customerId, BigDecimal totalAmount) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.status = OrderStatus.CREATED;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public long getVersion() {
        return version;
    }
}
