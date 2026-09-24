package lab.payment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

@RestController
public class PaymentController {

    private final PaymentBehaviour behaviour;

    public PaymentController(PaymentBehaviour behaviour) {
        this.behaviour = behaviour;
    }

    /** Callers beyond max-concurrent queue here, which is what makes the stub a real bottleneck. */
    @PostMapping("/payments")
    public ResponseEntity<PaymentResponse> pay(@RequestBody PaymentRequest request) throws InterruptedException {
        Semaphore permits = behaviour.permits();
        permits.acquire();
        try {
            Thread.sleep(behaviour.latencyMs());
            if (ThreadLocalRandom.current().nextDouble() < behaviour.failureRate()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
            return ResponseEntity.ok(new PaymentResponse(UUID.randomUUID(), request.orderId(), "APPROVED"));
        } finally {
            permits.release();
        }
    }

    /** Example: POST /admin/behaviour?latencyMs=2000&failureRate=0.3&maxConcurrent=5 */
    @PostMapping("/admin/behaviour")
    public Settings update(@RequestParam long latencyMs,
                           @RequestParam double failureRate,
                           @RequestParam int maxConcurrent) {
        behaviour.update(latencyMs, failureRate, maxConcurrent);
        return current();
    }

    @GetMapping("/admin/behaviour")
    public Settings current() {
        return new Settings(behaviour.latencyMs(), behaviour.failureRate(), behaviour.maxConcurrent());
    }

    public record PaymentRequest(UUID orderId, BigDecimal amount, String idempotencyKey) {
    }

    public record PaymentResponse(UUID paymentId, UUID orderId, String status) {
    }

    public record Settings(long latencyMs, double failureRate, int maxConcurrent) {
    }
}
