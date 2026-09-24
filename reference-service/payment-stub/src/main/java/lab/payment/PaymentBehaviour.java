package lab.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;

/**
 * Runtime-adjustable behaviour of the stub: added latency, failure rate and a hard
 * concurrency limit that models a downstream with finite capacity (WP-04 bottleneck lab).
 */
@Component
public final class PaymentBehaviour {

    private volatile long latencyMs;
    private volatile double failureRate;
    private volatile int maxConcurrent;
    private volatile Semaphore permits;

    public PaymentBehaviour(
            @Value("${lab.payment.latency-ms:50}") long latencyMs,
            @Value("${lab.payment.failure-rate:0.0}") double failureRate,
            @Value("${lab.payment.max-concurrent:20}") int maxConcurrent) {
        update(latencyMs, failureRate, maxConcurrent);
    }

    public synchronized void update(long latencyMs, double failureRate, int maxConcurrent) {
        if (latencyMs < 0 || failureRate < 0 || failureRate > 1 || maxConcurrent < 1) {
            throw new IllegalArgumentException("latencyMs >= 0, 0 <= failureRate <= 1, maxConcurrent >= 1");
        }
        this.latencyMs = latencyMs;
        this.failureRate = failureRate;
        this.maxConcurrent = maxConcurrent;
        this.permits = new Semaphore(maxConcurrent);
    }

    public long latencyMs() {
        return latencyMs;
    }

    public double failureRate() {
        return failureRate;
    }

    public int maxConcurrent() {
        return maxConcurrent;
    }

    public Semaphore permits() {
        return permits;
    }
}
