package lab.order.api;

import jakarta.validation.Valid;
import lab.order.api.ApiModels.CreateOrderRequest;
import lab.order.api.ApiModels.OrderResponse;
import lab.order.domain.OrderService;
import lab.order.domain.OrderService.LineRequest;
import lab.order.domain.PurchaseOrder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
class OrderController {

    private final OrderService orders;

    OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping
    ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        PurchaseOrder order = orders.create(request.customerId(),
                request.lines().stream().map(l -> new LineRequest(l.productId(), l.quantity())).toList());
        return ResponseEntity.created(URI.create("/orders/" + order.getId())).body(OrderResponse.from(order));
    }

    @GetMapping("/{id}")
    OrderResponse get(@PathVariable UUID id) {
        return OrderResponse.from(orders.find(id));
    }

    @PostMapping("/{id}/pay")
    OrderResponse pay(@PathVariable UUID id) {
        return OrderResponse.from(orders.pay(id));
    }

    @PostMapping("/{id}/cancel")
    OrderResponse cancel(@PathVariable UUID id) {
        return OrderResponse.from(orders.cancel(id));
    }
}
