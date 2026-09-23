package dpd.lab.sports.fencing.pool;

import dpd.lab.sports.fencing.Fencer;
import dpd.lab.sports.fencing.pool.exceptions.InvalidPoolException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class PoolTest {

    private PoolFencer dArtagnan;

    private PoolFencer athos;

    private PoolFencer porthos;

    private PoolFencer aramis;

    private PoolFencer inigo;

    private PoolFencer westley;

    private PoolFencer musashi;

    @BeforeEach
    void setUp() {
        dArtagnan = new PoolFencer(new Fencer("D'Artagnan"), 1);
        athos = new PoolFencer(new Fencer("Athos"), 2);
        porthos = new PoolFencer(new Fencer("Porthos"), 3);
        aramis = new PoolFencer(new Fencer("Aramis"), 4);
        inigo = new PoolFencer(new Fencer("Inigo"), 5);
        westley = new PoolFencer(new Fencer("Westley"), 6);
        musashi = new PoolFencer(new Fencer("Musashi"), 7);
    }

    @Test
    void shouldBeAbleToBuildPoolProperlyWithBouts() {
        Pool fencingPool = Pool.buildFrom(dArtagnan, athos, porthos, aramis, inigo, westley, musashi);
        assertThat(fencingPool.getBouts().size()).isEqualTo(21);
    }

    @Test
    void shouldNotBeAbleToBuildPoolWithDuplicateFencers() {
        assertThatExceptionOfType(InvalidPoolException.class)
                .isThrownBy(() -> Pool.buildFrom(dArtagnan, athos, porthos, dArtagnan))
                .withMessage("A pool cannot have duplicate fencers");
    }

    @Test
    void shouldNotBeAbleToBuildPoolWithLessThanThreeFencers() {
        assertThatExceptionOfType(InvalidPoolException.class)
                .isThrownBy(() -> Pool.buildFrom(dArtagnan, athos))
                .withMessage("A pool must have at least 3 fencers");
    }
}
