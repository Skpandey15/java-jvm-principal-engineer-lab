package lab.payment;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class PaymentBehaviourTest {

    @Test
    void updateReplacesSettingsAndPermits() {
        PaymentBehaviour behaviour = new PaymentBehaviour(50, 0.0, 20);

        behaviour.update(2000, 0.3, 5);

        assertThat(behaviour.latencyMs()).isEqualTo(2000);
        assertThat(behaviour.failureRate()).isEqualTo(0.3);
        assertThat(behaviour.permits().availablePermits()).isEqualTo(5);
    }

    @Test
    void rejectsInvalidSettings() {
        PaymentBehaviour behaviour = new PaymentBehaviour(50, 0.0, 20);

        assertThatIllegalArgumentException().isThrownBy(() -> behaviour.update(10, 1.5, 5));
        assertThatIllegalArgumentException().isThrownBy(() -> behaviour.update(10, 0.1, 0));
    }
}
