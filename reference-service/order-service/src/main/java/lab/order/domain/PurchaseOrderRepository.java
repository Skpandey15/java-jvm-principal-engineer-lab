package lab.order.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {

    /** Order, customer, lines and products in one SQL statement. */
    @EntityGraph(attributePaths = {"customer", "lines", "lines.product"})
    Optional<PurchaseOrder> findWithDetailsById(UUID id);

    /**
     * Step 1 of paged history: page over order ids only (uses idx_order_customer_created).
     * Paging a fetch-join of a collection directly would make Hibernate page in memory (HHH90003004).
     */
    @Query(value = "select o.id from PurchaseOrder o where o.customer.id = :customerId order by o.createdAt desc",
            countQuery = "select count(o) from PurchaseOrder o where o.customer.id = :customerId")
    Page<UUID> findIdsByCustomer(Long customerId, Pageable pageable);

    /** Step 2 of paged history: that page's orders with lines and products in one statement. */
    @EntityGraph(attributePaths = {"lines", "lines.product"})
    @Query("select o from PurchaseOrder o where o.id in :ids")
    List<PurchaseOrder> findAllWithLinesByIdIn(Collection<UUID> ids);
}
