package lab.order;

import lab.order.catalog.CatalogService;
import lab.order.domain.InsufficientStockException;
import lab.order.domain.InvalidOrderStateException;
import lab.order.domain.OrderService;
import lab.order.domain.OrderService.LineRequest;
import lab.order.domain.OrderStatus;
import lab.order.domain.PurchaseOrder;
import lab.order.payment.PaymentClient;
import lab.order.report.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

/**
 * Runs against a real PostgreSQL 18 with the Flyway schema and reference data (customers 1-10, products 1-50;
 * product 50 has 5 units). Bulk seed is off. Skipped when Docker is not available.
 */
@SpringBootTest(properties = {
        "lab.seed.enabled=false",
        "management.tracing.sampling.probability=0.0",
        "management.logging.export.otlp.enabled=false"})
@Testcontainers(disabledWithoutDocker = true)
class OrderServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.4");

    @Autowired
    OrderService orders;

    @Autowired
    CatalogService catalog;

    @Autowired
    ReportService reports;

    @MockitoBean
    PaymentClient payments;

    @Test
    void createReservesStockAndComputesTotalFromCurrentPrices() {
        int stockBefore = catalog.get(3L).available();

        // product 3 costs 3.75, product 4 costs 5.00
        PurchaseOrder order = orders.create(1L, List.of(new LineRequest(3L, 2), new LineRequest(4L, 1)));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("12.50");
        assertThat(catalog.get(3L).available()).isEqualTo(stockBefore - 2);

        PurchaseOrder loaded = orders.find(order.getId());
        assertThat(loaded.getLines()).hasSize(2);
        assertThat(loaded.getLines().getFirst().getProduct().getSku()).isEqualTo("REF-003");
    }

    @Test
    void insufficientStockRollsBackEveryReservationInTheOrder() {
        int stockOfProduct1 = catalog.get(1L).available();

        // product 50 has only 5 units, so the whole order must fail and product 1 must be untouched
        assertThatThrownBy(() -> orders.create(2L, List.of(new LineRequest(1L, 1), new LineRequest(50L, 6))))
                .isInstanceOf(InsufficientStockException.class);

        assertThat(catalog.get(1L).available()).isEqualTo(stockOfProduct1);
    }

    @Test
    void historyIsPagedNewestFirstWithLinesLoaded() {
        UUID older = orders.create(5L, List.of(new LineRequest(10L, 1))).getId();
        UUID newer = orders.create(5L, List.of(new LineRequest(11L, 1), new LineRequest(12L, 2))).getId();

        Page<PurchaseOrder> page = orders.history(5L, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(PurchaseOrder::getId).containsExactly(newer, older);
        assertThat(page.getContent().getFirst().getLines()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void payMarksOrderPaidAndCannotPayTwice() {
        PurchaseOrder order = orders.create(3L, List.of(new LineRequest(7L, 1)));
        UUID paymentId = UUID.randomUUID();
        given(payments.pay(eq(order.getId()), any(BigDecimal.class))).willReturn(paymentId);

        PurchaseOrder paid = orders.pay(order.getId());

        assertThat(paid.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(paid.getPaymentId()).isEqualTo(paymentId);
        assertThatThrownBy(() -> orders.pay(order.getId())).isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void cancelReleasesReservedStock() {
        int stockBefore = catalog.get(20L).available();
        PurchaseOrder order = orders.create(4L, List.of(new LineRequest(20L, 3)));

        orders.cancel(order.getId());

        assertThat(catalog.get(20L).available()).isEqualTo(stockBefore);
        assertThat(orders.find(order.getId()).getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void dailySalesCountsOnlyPaidOrders() {
        PurchaseOrder order = orders.create(6L, List.of(new LineRequest(8L, 2)));   // 2 x 10.00
        given(payments.pay(eq(order.getId()), any(BigDecimal.class))).willReturn(UUID.randomUUID());
        orders.pay(order.getId());
        orders.create(6L, List.of(new LineRequest(8L, 1)));                          // stays CREATED

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        List<ReportService.DailySales> sales = reports.dailySales(today, today.plusDays(1));

        assertThat(sales).hasSize(1);
        assertThat(sales.getFirst().revenue()).isGreaterThanOrEqualTo(new BigDecimal("20.00"));
    }
}
