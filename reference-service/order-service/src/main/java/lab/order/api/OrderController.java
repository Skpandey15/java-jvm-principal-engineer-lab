package lab.order.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lab.order.domain.OrderService;
import lab.order.domain.PurchaseOrder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        PurchaseOrder order = orders.create(request.customerId(), request.totalAmount());
        return ResponseEntity.created(URI.create("/orders/" + order.getId())).body(OrderResponse.from(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> get(@PathVariable UUID id) {
        return ResponseEntity.of(orders.find(id).map(OrderResponse::from));
    }

    public record CreateOrderRequest(
            @NotBlank String customerId,
            @NotNull @DecimalMin("0.01") BigDecimal totalAmount) {
    }

    public record OrderResponse(UUID id, String customerId, BigDecimal totalAmount, String status, Instant createdAt) {
        static OrderResponse from(PurchaseOrder o) {
            return new OrderResponse(o.getId(), o.getCustomerId(), o.getTotalAmount(), o.getStatus().name(), o.getCreatedAt());
        }
    }
}
