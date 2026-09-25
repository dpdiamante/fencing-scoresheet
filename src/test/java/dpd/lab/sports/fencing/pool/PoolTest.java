package dpd.lab.sports.fencing.pool;

import dpd.lab.sports.fencing.Fencer;
import dpd.lab.sports.fencing.pool.bout.Bout;
import dpd.lab.sports.fencing.pool.exceptions.FencerNotInPoolException;
import dpd.lab.sports.fencing.pool.exceptions.InvalidPoolException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static dpd.lab.sports.fencing.pool.PoolAssertions.assertThat;
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

    private Pool testPool;

    @BeforeEach
    void setUp() {
        dArtagnan = new PoolFencer(new Fencer("D'Artagnan"), 1);
        athos = new PoolFencer(new Fencer("Athos"), 2);
        porthos = new PoolFencer(new Fencer("Porthos"), 3);
        aramis = new PoolFencer(new Fencer("Aramis"), 4);
        inigo = new PoolFencer(new Fencer("Inigo"), 5);
        westley = new PoolFencer(new Fencer("Westley"), 6);
        musashi = new PoolFencer(new Fencer("Musashi"), 7);

        testPool = Pool.buildFrom(dArtagnan, athos, porthos, aramis, inigo, westley, musashi);
    }

    @Test
    void shouldBeAbleToBuildPoolProperlyWithBouts() {
        Pool fencingPool = Pool.buildFrom(dArtagnan, athos, porthos, aramis, inigo, westley, musashi);
        assertThat(fencingPool.getBouts().size()).isEqualTo(21);
    }

    @Test
    void shouldNotBeAbleToBuildPoolWithDuplicateFencers() {
        assertThatExceptionOfType(InvalidPoolException.class).isThrownBy(
                () -> Pool.buildFrom(dArtagnan, athos, porthos, dArtagnan)).withMessage(
                "A pool cannot have duplicate fencers");
    }

    @Test
    void shouldNotBeAbleToBuildPoolWithLessThanThreeFencers() {
        assertThatExceptionOfType(InvalidPoolException.class).isThrownBy(
                () -> Pool.buildFrom(dArtagnan, athos)).withMessage("A pool must have at least 3 fencers");
    }

    @Test
    void shouldUpdatePoolBoutCorrectly() {
        Bout updatedBout = testPool.recordResult().withWinner(athos, 5).withDefeated(dArtagnan, 2).record();
        Bout sameBout = testPool.getBoutBetween(athos, dArtagnan);

        assertThat(sameBout).isEqualTo(updatedBout);
        assertThat(updatedBout).hasFinished().hasWinnerWithScore(athos, 5).hasLoserWithScore(dArtagnan, 2);
        assertThat(sameBout).hasFinished().hasWinnerWithScore(athos, 5).hasLoserWithScore(dArtagnan, 2);
    }

    @Test
    void shouldNotBeAbleToUpdatePoolBoutCorrectly() {
        PoolFencer darthVader = new PoolFencer(new Fencer("Darth Vader"), 9);

        assertThatExceptionOfType(FencerNotInPoolException.class).isThrownBy(
                        () -> testPool.recordResult().withWinner(darthVader, 5).withDefeated(dArtagnan, 1).record())
                .withMessageContaining("There is no bout between");
    }

    @Test
    void shouldReturnBoutsForFencer() {
        Set<Bout> fencingBouts = testPool.getBoutsOf(porthos);
        assertThat(fencingBouts).hasSize(6);
    }

    @Test
    void shouldReturnBoutBetweenFencers() {
        Bout bout =  testPool.getBoutBetween(aramis, westley);

        assertThat(bout).isNotNull().hasPlayer(aramis).hasPlayer(westley);
    }

    @Test
    void shouldThrowExceptionWhenFencerNotInPool() {
        PoolFencer darthVader = new PoolFencer(new Fencer("Darth Vader"), 9);

        assertThatExceptionOfType(FencerNotInPoolException.class).isThrownBy(
                () -> testPool.getBoutBetween(aramis, darthVader)
        ).withMessageContaining("There is no bout between");
    }
}
