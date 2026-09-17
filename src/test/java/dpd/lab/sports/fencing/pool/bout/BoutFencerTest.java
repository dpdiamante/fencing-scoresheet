package dpd.lab.sports.fencing.pool.bout;

import dpd.lab.sports.fencing.pool.PoolFencer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoutFencerTest {

    private PoolFencer johnPoolFencer;

    private PoolFencer janePoolFencer;

    @BeforeEach
    void setUp() {
        johnPoolFencer = new PoolFencer(new dpd.lab.sports.fencing.Fencer("John Smith"), 1);
        janePoolFencer = new PoolFencer(new dpd.lab.sports.fencing.Fencer("Jane Doe"), 2);
    }

    @Test
    void shouldBeEqualToItself() {
        BoutFencer fencer = new BoutFencer(johnPoolFencer);

        assertThat(fencer).isEqualTo(fencer).hasSameHashCodeAs(fencer);
    }

    @Test
    void shouldBeEqualWhenWrappingTheSamePoolFencerInstance() {
        BoutFencer first = new BoutFencer(johnPoolFencer);
        BoutFencer second = new BoutFencer(johnPoolFencer);

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
    }

    @Test
    void shouldBeEqualWhenWrappingAnEqualButDistinctPoolFencer() {
        PoolFencer equivalentPoolFencer =
                new PoolFencer(new dpd.lab.sports.fencing.Fencer("John Smith"), 1);

        BoutFencer first = new BoutFencer(johnPoolFencer);
        BoutFencer second = new BoutFencer(equivalentPoolFencer);

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
    }

    @Test
    void shouldRemainEqualRegardlessOfScoreOrStatus() {
        BoutFencer first = new BoutFencer(johnPoolFencer);
        BoutFencer second = new BoutFencer(johnPoolFencer);

        first.setScore(3);
        first.setStatus(FencerStatus.VICTOR);
        second.setScore(0);
        second.setStatus(FencerStatus.DEFEAT);

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
    }

    @Test
    void shouldNotBeEqualWhenWrappingDifferentPoolFencers() {
        BoutFencer first = new BoutFencer(johnPoolFencer);
        BoutFencer second = new BoutFencer(janePoolFencer);

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void shouldNotBeEqualToNull() {
        BoutFencer fencer = new BoutFencer(johnPoolFencer);

        assertThat(fencer).isNotEqualTo(null);
    }

    @Test
    void shouldNotBeEqualToAnInstanceOfAnUnrelatedType() {
        BoutFencer boutFencer = new BoutFencer(johnPoolFencer);

        assertThat(boutFencer).isNotEqualTo(johnPoolFencer);
    }
}
