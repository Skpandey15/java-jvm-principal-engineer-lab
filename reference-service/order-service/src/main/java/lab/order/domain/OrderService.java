package lab.order.domain;

import lab.order.fault.FaultFlags;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    /** Deliberate leak target for WP-02; only written when the fault flag is on. */
    private static final List<PurchaseOrder> LEAKED_ORDERS = new ArrayList<>();

    private final PurchaseOrderRepository repository;
    private final FaultFlags faults;

    public OrderService(PurchaseOrderRepository repository, FaultFlags faults) {
        this.repository = repository;
        this.faults = faults;
    }

    @Transactional
    public PurchaseOrder create(String customerId, BigDecimal totalAmount) {
        PurchaseOrder order = repository.save(new PurchaseOrder(customerId, totalAmount));
        if (faults.retainCreatedOrders()) {
            synchronized (LEAKED_ORDERS) {
                LEAKED_ORDERS.add(order);
            }
        }
        return order;
    }

    @Transactional(readOnly = true)
    public Optional<PurchaseOrder> find(UUID id) {
        return repository.findById(id);
    }
}
