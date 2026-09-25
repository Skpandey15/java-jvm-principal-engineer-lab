package lab.order.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Reserves stock atomically: the check and the decrement are one statement, so two concurrent orders
     * can never oversell, and the row lock is held only for the rest of the (short) transaction.
     *
     * @return 1 if reserved, 0 if not enough stock
     */
    @Modifying
    @Query("update Inventory i set i.available = i.available - :quantity "
            + "where i.productId = :productId and i.available >= :quantity")
    int reserve(Long productId, int quantity);

    @Modifying
    @Query("update Inventory i set i.available = i.available + :quantity where i.productId = :productId")
    int release(Long productId, int quantity);
}
