package lab.order.domain;

import lab.order.catalog.InventoryRepository;
import lab.order.catalog.Product;
import lab.order.catalog.ProductRepository;
import lab.order.customer.Customer;
import lab.order.customer.CustomerRepository;
import lab.order.fault.FaultFlags;
import lab.order.incident.Incidents;
import lab.order.payment.PaymentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    /** Deliberate leak target for WP-02; only written when the fault flag is on. */
    private static final List<PurchaseOrder> LEAKED_ORDERS = new ArrayList<>();

    private final PurchaseOrderRepository orders;
    private final CustomerRepository customers;
    private final ProductRepository products;
    private final InventoryRepository inventory;
    private final PaymentClient payments;
    private final TransactionTemplate transactions;
    private final FaultFlags faults;
    private final Incidents incidents;

    public OrderService(PurchaseOrderRepository orders, CustomerRepository customers, ProductRepository products,
                        InventoryRepository inventory, PaymentClient payments, TransactionTemplate transactions,
                        FaultFlags faults, Incidents incidents) {
        this.orders = orders;
        this.customers = customers;
        this.products = products;
        this.inventory = inventory;
        this.payments = payments;
        this.transactions = transactions;
        this.faults = faults;
        this.incidents = incidents;
    }

    public record LineRequest(Long productId, int quantity) {
    }

    /**
     * Reserves stock and saves the order in one transaction: if any line lacks stock, every reservation
     * made so far is rolled back. Products are reserved in ascending id order, so two orders touching the
     * same products always lock rows in the same order and cannot deadlock each other.
     */
    @Transactional
    public PurchaseOrder create(Long customerId, List<LineRequest> lines) {
        Customer customer = customers.findById(customerId)
                .orElseThrow(() -> new NotFoundException("customer", customerId));

        Map<Long, Integer> quantityByProduct = lines.stream()
                .collect(Collectors.toMap(LineRequest::productId, LineRequest::quantity, Integer::sum, TreeMap::new));
        Map<Long, Product> productById = products.findAllById(quantityByProduct.keySet()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        quantityByProduct.keySet().stream()
                .filter(id -> !productById.containsKey(id))
                .findFirst()
                .ifPresent(id -> {
                    throw new NotFoundException("product", id);
                });

        quantityByProduct.forEach((productId, quantity) -> {
            if (inventory.reserve(productId, quantity) == 0) {
                throw new InsufficientStockException(productId, quantity);
            }
        });

        PurchaseOrder order = new PurchaseOrder(customer);
        lines.forEach(line -> order.addLine(productById.get(line.productId()), line.quantity()));
        orders.save(order);

        if (faults.retainCreatedOrders()) {
            synchronized (LEAKED_ORDERS) {
                LEAKED_ORDERS.add(order);
            }
        }
        log.info("Order created id={} customer={} lines={} total={}",
                order.getId(), customerId, lines.size(), order.getTotalAmount());
        return order;
    }

    @Transactional(readOnly = true)
    public PurchaseOrder find(UUID id) {
        return orders.findWithDetailsById(id).orElseThrow(() -> new NotFoundException("order", id));
    }

    /** Two statements per page, whatever the page size: order ids, then those orders with lines and products. */
    @Transactional(readOnly = true)
    public Page<PurchaseOrder> history(Long customerId, Pageable pageable) {
        if (!customers.existsById(customerId)) {
            throw new NotFoundException("customer", customerId);
        }
        if (incidents.isActive("inc-002")) {
            // Load the page of orders directly, then touch the lines and products while the
            // transaction is still open, so the API layer can render them.
            Page<PurchaseOrder> page = orders.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
            page.forEach(order -> order.getLines().forEach(line -> line.getProduct().getName()));
            return page;
        }
        Page<UUID> ids = orders.findIdsByCustomer(customerId, pageable);
        Map<UUID, PurchaseOrder> loaded = orders.findAllWithLinesByIdIn(ids.getContent()).stream()
                .collect(Collectors.toMap(PurchaseOrder::getId, Function.identity()));
        List<PurchaseOrder> inPageOrder = ids.getContent().stream().map(loaded::get).toList();
        return new PageImpl<>(inPageOrder, pageable, ids.getTotalElements());
    }

    /**
     * The remote payment call runs outside any database transaction: no connection is held while waiting
     * on the network, so a slow payment service cannot exhaust the connection pool.
     */
    public PurchaseOrder pay(UUID id) {
        record Due(UUID orderId, BigDecimal amount) {
        }
        Due due = transactions.execute(status -> {
            PurchaseOrder order = orders.findById(id).orElseThrow(() -> new NotFoundException("order", id));
            if (order.getStatus() != OrderStatus.CREATED) {
                throw new InvalidOrderStateException(id, order.getStatus(), "pay");
            }
            return new Due(order.getId(), order.getTotalAmount());
        });

        UUID paymentId = payments.pay(due.orderId(), due.amount());

        return transactions.execute(status -> {
            PurchaseOrder order = orders.findWithDetailsById(id).orElseThrow(() -> new NotFoundException("order", id));
            order.markPaid(paymentId);
            return order;
        });
    }

    @Transactional
    public PurchaseOrder cancel(UUID id) {
        PurchaseOrder order = orders.findWithDetailsById(id).orElseThrow(() -> new NotFoundException("order", id));
        order.cancel();
        order.getLines().stream()
                .sorted(Comparator.comparing(line -> line.getProduct().getId()))
                .forEach(line -> inventory.release(line.getProduct().getId(), line.getQuantity()));
        return order;
    }
}
