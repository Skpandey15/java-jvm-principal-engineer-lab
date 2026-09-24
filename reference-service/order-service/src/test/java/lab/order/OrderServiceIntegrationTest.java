package lab.order;

import lab.order.domain.OrderService;
import lab.order.domain.OrderStatus;
import lab.order.domain.PurchaseOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** Runs against a real PostgreSQL 18 container; skipped when Docker is not available. */
@SpringBootTest(properties = {
        "management.tracing.sampling.probability=0.0",
        "management.logging.export.otlp.enabled=false"})
@Testcontainers(disabledWithoutDocker = true)
class OrderServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired
    OrderService orders;

    @Test
    void createdOrderIsPersistedByFlywaySchema() {
        PurchaseOrder created = orders.create("customer-1", new BigDecimal("42.50"));

        PurchaseOrder loaded = orders.find(created.getId()).orElseThrow();

        assertThat(loaded.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(loaded.getTotalAmount()).isEqualByComparingTo("42.50");
    }
}
