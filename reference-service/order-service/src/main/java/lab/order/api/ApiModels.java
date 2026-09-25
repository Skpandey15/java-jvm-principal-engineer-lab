package lab.order.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lab.order.catalog.CatalogService.ProductWithStock;
import lab.order.customer.Customer;
import lab.order.domain.OrderLine;
import lab.order.domain.PurchaseOrder;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/** Request and response bodies of the ShopFlow API. Entities never leave the service layer. */
final class ApiModels {

    record CreateOrderRequest(
            @NotNull Long customerId,
            @NotEmpty @Size(max = 50) List<@Valid LineRequest> lines) {
    }

    record LineRequest(@NotNull Long productId, @Min(1) @Max(100) int quantity) {
    }

    record OrderResponse(UUID id, Long customerId, String status, BigDecimal totalAmount, UUID paymentId,
                         Instant createdAt, List<LineResponse> lines) {
        static OrderResponse from(PurchaseOrder o) {
            return new OrderResponse(o.getId(), o.getCustomer().getId(), o.getStatus().name(), o.getTotalAmount(),
                    o.getPaymentId(), o.getCreatedAt(), o.getLines().stream().map(LineResponse::from).toList());
        }
    }

    record LineResponse(Long productId, String sku, String name, int quantity, BigDecimal unitPrice) {
        static LineResponse from(OrderLine line) {
            return new LineResponse(line.getProduct().getId(), line.getProduct().getSku(), line.getProduct().getName(),
                    line.getQuantity(), line.getUnitPrice());
        }
    }

    record ProductResponse(Long id, String sku, String name, String category, BigDecimal price, int available) {
        static ProductResponse from(ProductWithStock p) {
            return new ProductResponse(p.product().getId(), p.product().getSku(), p.product().getName(),
                    p.product().getCategory(), p.product().getPrice(), p.available());
        }
    }

    record CustomerResponse(Long id, String email, String name, String tier, Instant createdAt) {
        static CustomerResponse from(Customer c) {
            return new CustomerResponse(c.getId(), c.getEmail(), c.getName(), c.getTier().name(), c.getCreatedAt());
        }
    }

    /** Stable JSON shape for pages (serializing Spring Data's PageImpl directly is not a stable contract). */
    record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
        static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
            return new PageResponse<>(page.getContent().stream().map(mapper).toList(), page.getNumber(),
                    page.getSize(), page.getTotalElements(), page.getTotalPages());
        }
    }

    private ApiModels() {
    }
}
