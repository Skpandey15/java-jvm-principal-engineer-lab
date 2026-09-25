package lab.order.receipt;

import lab.order.domain.PurchaseOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps rendered receipts in memory so a receipt can be served without re-rendering it.
 * Only active while incident scenario INC-001 is running (lab.incident=inc-001).
 */
@Component
public class ReceiptCache {

    private final Map<UUID, byte[]> receipts = new ConcurrentHashMap<>();
    private final boolean active;

    public ReceiptCache(@Value("${lab.incident:none}") String incident) {
        this.active = "inc-001".equalsIgnoreCase(incident);
    }

    public void remember(PurchaseOrder order) {
        if (active) {
            receipts.put(order.getId(), render(order));
        }
    }

    public Optional<byte[]> find(UUID orderId) {
        return Optional.ofNullable(receipts.get(orderId));
    }

    private static byte[] render(PurchaseOrder order) {
        StringBuilder receipt = new StringBuilder(8_192);
        receipt.append("RECEIPT ").append(order.getId()).append('\n')
                .append("Customer: ").append(order.getCustomerId()).append('\n')
                .append("Total: ").append(order.getTotalAmount()).append('\n');
        for (int line = 0; line < 100; line++) {
            receipt.append("  line ").append(line).append(": terms, conditions and tax breakdown ........\n");
        }
        return receipt.toString().getBytes(StandardCharsets.UTF_8);
    }
}
