package lab.order.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

/**
 * @param baseUrl        payment-stub (or payment via Toxiproxy on :28081)
 * @param connectTimeout fail fast when the dependency is unreachable
 * @param readTimeout    upper bound for a payment call; derived from the order API latency budget
 */
@ConfigurationProperties("lab.payment")
public record PaymentProperties(URI baseUrl, Duration connectTimeout, Duration readTimeout) {
}
