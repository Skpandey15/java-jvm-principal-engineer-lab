package lab.order.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lab.order.catalog.Product;
import lab.order.customer.Customer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Order aggregate root. Named PurchaseOrder because ORDER is an SQL keyword. */
@Entity
@Table(name = "purchase_order")
public class PurchaseOrder {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OrderStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    private UUID paymentId;

    @Column(nullable = false)
    private Instant createdAt;

    /**
     * Wrapper type on purpose: null means "new", so Spring Data calls persist() instead of merge()
     * and avoids a SELECT before every INSERT (the id is assigned in the constructor).
     */
    @Version
    private Long version;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<OrderLine> lines = new ArrayList<>();

    protected PurchaseOrder() {
        // for JPA
    }

    public PurchaseOrder(Customer customer) {
        this.id = UUID.randomUUID();
        this.customer = customer;
        this.status = OrderStatus.CREATED;
        this.totalAmount = BigDecimal.ZERO;
        this.createdAt = Instant.now();
    }

    public void addLine(Product product, int quantity) {
        OrderLine line = new OrderLine(this, product, quantity, product.getPrice());
        lines.add(line);
        totalAmount = totalAmount.add(line.lineTotal());
    }

    public void markPaid(UUID paymentReference) {
        requireStatus(OrderStatus.CREATED, "pay");
        this.status = OrderStatus.PAID;
        this.paymentId = paymentReference;
    }

    public void cancel() {
        requireStatus(OrderStatus.CREATED, "cancel");
        this.status = OrderStatus.CANCELLED;
    }

    private void requireStatus(OrderStatus expected, String action) {
        if (status != expected) {
            throw new InvalidOrderStateException(id, status, action);
        }
    }

    public UUID getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<OrderLine> getLines() {
        return Collections.unmodifiableList(lines);
    }
}
