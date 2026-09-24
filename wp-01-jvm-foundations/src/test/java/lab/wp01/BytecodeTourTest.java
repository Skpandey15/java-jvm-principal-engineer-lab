package lab.wp01;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BytecodeTourTest {

    private final BytecodeTour tour = new BytecodeTour();

    @Test
    void behaviourIsWhatTheSourceSays() {
        assertThat(tour.concat("Ada", 3)).isEqualTo("Hello Ada, you have 3 messages");
        assertThat(tour.lambda("x").get()).isEqualTo("lambda x");
        assertThat(tour.anonymous("x").get()).isEqualTo("anonymous x");
        assertThat(tour.sum(List.of(1, 2, 3))).isEqualTo(6);
    }

    @Test
    void boxedIdentityDependsOnTheIntegerCache() {
        assertThat(tour.sameBoxes(127, 127)).isTrue();    // cached: same object
        assertThat(tour.sameBoxes(128, 128)).isFalse();   // outside -128..127: two objects
    }
}
