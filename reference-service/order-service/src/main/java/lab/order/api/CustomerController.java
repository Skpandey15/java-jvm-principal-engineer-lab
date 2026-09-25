package lab.order.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lab.order.api.ApiModels.CustomerResponse;
import lab.order.api.ApiModels.OrderResponse;
import lab.order.api.ApiModels.PageResponse;
import lab.order.customer.CustomerRepository;
import lab.order.domain.NotFoundException;
import lab.order.domain.OrderService;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
class CustomerController {

    private final CustomerRepository customers;
    private final OrderService orders;

    CustomerController(CustomerRepository customers, OrderService orders) {
        this.customers = customers;
        this.orders = orders;
    }

    @GetMapping("/{id}")
    CustomerResponse get(@PathVariable Long id) {
        return customers.findById(id).map(CustomerResponse::from)
                .orElseThrow(() -> new NotFoundException("customer", id));
    }

    /** Order history, newest first. Example: GET /customers/42/orders?page=0&size=10 */
    @GetMapping("/{id}/orders")
    PageResponse<OrderResponse> orders(@PathVariable Long id,
                                       @RequestParam(defaultValue = "0") @Min(0) int page,
                                       @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return PageResponse.from(orders.history(id, PageRequest.of(page, size)), OrderResponse::from);
    }
}
