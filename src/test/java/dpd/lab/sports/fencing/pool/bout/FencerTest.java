package dpd.lab.sports.fencing.pool.bout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FencerTest {

    private dpd.lab.sports.fencing.pool.Fencer johnPoolFencer;

    private dpd.lab.sports.fencing.pool.Fencer janePoolFencer;

    @BeforeEach
    void setUp() {
        johnPoolFencer = new dpd.lab.sports.fencing.pool.Fencer(new dpd.lab.sports.fencing.Fencer("John Smith"), 1);
        janePoolFencer = new dpd.lab.sports.fencing.pool.Fencer(new dpd.lab.sports.fencing.Fencer("Jane Doe"), 2);
    }

    @Test
    void shouldBeEqualToItself() {
        Fencer fencer = new Fencer(johnPoolFencer);

        assertThat(fencer).isEqualTo(fencer).hasSameHashCodeAs(fencer);
    }

    @Test
    void shouldBeEqualWhenWrappingTheSamePoolFencerInstance() {
        Fencer first = new Fencer(johnPoolFencer);
        Fencer second = new Fencer(johnPoolFencer);

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
    }

    @Test
    void shouldBeEqualWhenWrappingAnEqualButDistinctPoolFencer() {
        dpd.lab.sports.fencing.pool.Fencer equivalentPoolFencer =
                new dpd.lab.sports.fencing.pool.Fencer(new dpd.lab.sports.fencing.Fencer("John Smith"), 1);

        Fencer first = new Fencer(johnPoolFencer);
        Fencer second = new Fencer(equivalentPoolFencer);

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
    }

    @Test
    void shouldRemainEqualRegardlessOfScoreOrStatus() {
        Fencer first = new Fencer(johnPoolFencer);
        Fencer second = new Fencer(johnPoolFencer);

        first.setScore(3);
        first.setStatus(FencerStatus.VICTOR);
        second.setScore(0);
        second.setStatus(FencerStatus.DEFEAT);

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
    }

    @Test
    void shouldNotBeEqualWhenWrappingDifferentPoolFencers() {
        Fencer first = new Fencer(johnPoolFencer);
        Fencer second = new Fencer(janePoolFencer);

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void shouldNotBeEqualToNull() {
        Fencer fencer = new Fencer(johnPoolFencer);

        assertThat(fencer).isNotEqualTo(null);
    }

    @Test
    void shouldNotBeEqualToAnInstanceOfAnUnrelatedType() {
        Fencer boutFencer = new Fencer(johnPoolFencer);

        assertThat(boutFencer).isNotEqualTo(johnPoolFencer);
    }
}
