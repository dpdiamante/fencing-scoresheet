package dpd.lab.sports.fencing;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static dpd.lab.sports.fencing.FencerFixtures.fencerNamed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class FencerTest {

    @Test
    void shouldCreateFencerWithTheGivenIdAndName() {
        UUID id = UUID.randomUUID();

        Fencer athos = new Fencer(id, "Athos");

        assertThat(athos.id()).isEqualTo(id);
        assertThat(athos.name()).isEqualTo("Athos");
    }

    @Test
    void shouldNotCreateFencerWithoutAnId() {
        assertThatNullPointerException().isThrownBy(() -> new Fencer(null, "Athos"))
                .withMessage("A fencer must have an id");
    }

    @Test
    void shouldNotCreateFencerWithoutAName() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new Fencer(UUID.randomUUID(), null))
                .withMessage("A fencer must have a name");
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new Fencer(UUID.randomUUID(), "  "))
                .withMessage("A fencer must have a name");
    }

    @Test
    void shouldBeEqualWhenSharingTheSameIdRegardlessOfName() {
        UUID id = UUID.randomUUID();
        Fencer first = new Fencer(id, "Athos");
        Fencer second = new Fencer(id, "Athos Renamed");

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
    }

    @Test
    void shouldNotBeEqualWhenDifferentFencersShareTheSameName() {
        assertThat(fencerNamed("John Smith")).isNotEqualTo(fencerNamed("John Smith"));
    }

    @Test
    void shouldBeEqualToItself() {
        Fencer athos = fencerNamed("Athos");

        assertThat(athos).isEqualTo(athos).hasSameHashCodeAs(athos);
    }

    @Test
    void shouldNotBeEqualToNull() {
        assertThat(fencerNamed("Athos")).isNotEqualTo(null);
    }

    @Test
    void shouldNotBeEqualToAnInstanceOfAnUnrelatedType() {
        assertThat(fencerNamed("Athos")).isNotEqualTo("Athos");
    }
}
