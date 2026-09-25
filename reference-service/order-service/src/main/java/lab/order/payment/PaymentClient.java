package lab.order.payment;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.UUID;

/** HTTP client for the payment service. Uses the auto-configured builder, so calls are traced and timed. */
@Component
public class PaymentClient {

    private final RestClient rest;

    public PaymentClient(RestClient.Builder builder, PaymentProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());
        this.rest = builder.baseUrl(properties.baseUrl().toString()).requestFactory(requestFactory).build();
    }

    record PaymentRequest(UUID orderId, BigDecimal amount, String idempotencyKey) {
    }

    record PaymentResponse(UUID paymentId, UUID orderId, String status) {
    }

    /** The order id doubles as idempotency key: retrying the same order never charges twice. */
    public UUID pay(UUID orderId, BigDecimal amount) {
        try {
            PaymentResponse response = rest.post()
                    .uri("/payments")
                    .body(new PaymentRequest(orderId, amount, orderId.toString()))
                    .retrieve()
                    .body(PaymentResponse.class);
            if (response == null || !"APPROVED".equals(response.status())) {
                throw new PaymentFailedException(orderId, "payment not approved");
            }
            return response.paymentId();
        } catch (RestClientException e) {
            throw new PaymentFailedException(orderId, e.getMessage());
        }
    }
}
