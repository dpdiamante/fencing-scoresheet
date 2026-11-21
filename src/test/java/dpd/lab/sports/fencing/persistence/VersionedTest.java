package dpd.lab.sports.fencing.persistence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class VersionedTest {

    @Test
    void shouldHoldTheAggregateAndItsVersion() {
        Versioned<String> versioned = new Versioned<>("pool", 3);

        assertThat(versioned.aggregate()).isEqualTo("pool");
        assertThat(versioned.version()).isEqualTo(3);
    }

    @Test
    void shouldAcceptTheInitialVersion() {
        assertThat(new Versioned<>("pool", 0).version()).isZero();
    }

    @Test
    void shouldNotBeCreatedWithoutAnAggregate() {
        assertThatNullPointerException().isThrownBy(() -> new Versioned<>(null, 0))
                .withMessage("A versioned aggregate must have an aggregate");
    }

    @Test
    void shouldNotBeCreatedWithANegativeVersion() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new Versioned<>("pool", -1))
                .withMessage("A version cannot be negative");
    }

    @Test
    void shouldBeEqualWhenHoldingTheSameAggregateAndVersion() {
        Versioned<String> first = new Versioned<>("pool", 3);
        Versioned<String> second = new Versioned<>("pool", 3);

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
    }

    @Test
    void shouldNotBeEqualWhenTheVersionDiffers() {
        assertThat(new Versioned<>("pool", 3)).isNotEqualTo(new Versioned<>("pool", 4));
    }
}
