package lab.order.fault;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Config-driven switches that inject reproducible failures (lab document, Section 4).
 * Enable one with e.g. {@code --lab.faults.retain-created-orders=true}.
 * Add a flag here for each failure a work package needs to reproduce on demand.
 *
 * @param retainCreatedOrders WP-02: keep every created order in a static list (heap leak)
 */
@ConfigurationProperties("lab.faults")
public record FaultFlags(boolean retainCreatedOrders) {
}
