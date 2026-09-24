package lab.wp03;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class LostUpdateTest {

    @ParameterizedTest
    @ValueSource(classes = {LostUpdate.SynchronizedCounter.class, LostUpdate.AtomicCounter.class, LostUpdate.AdderCounter.class})
    void threadSafeCountersNeverLoseUpdates(Class<? extends LostUpdate.Counter> type) throws Exception {
        LostUpdate.Counter counter = type.getDeclaredConstructor().newInstance();

        assertThat(LostUpdate.run(counter, 8, 100_000)).isEqualTo(800_000);
    }
}
