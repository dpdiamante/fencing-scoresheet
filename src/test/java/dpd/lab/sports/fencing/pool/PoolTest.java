package dpd.lab.sports.fencing.pool;

import dpd.lab.sports.fencing.pool.bout.Bout;
import dpd.lab.sports.fencing.pool.exceptions.FencerNotInPoolException;
import dpd.lab.sports.fencing.pool.exceptions.InvalidPoolException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static dpd.lab.sports.fencing.FencerFixtures.fencerNamed;
import static dpd.lab.sports.fencing.pool.PoolAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

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
        dArtagnan = new PoolFencer(fencerNamed("D'Artagnan"), 1);
        athos = new PoolFencer(fencerNamed("Athos"), 2);
        porthos = new PoolFencer(fencerNamed("Porthos"), 3);
        aramis = new PoolFencer(fencerNamed("Aramis"), 4);
        inigo = new PoolFencer(fencerNamed("Inigo"), 5);
        westley = new PoolFencer(fencerNamed("Westley"), 6);
        musashi = new PoolFencer(fencerNamed("Musashi"), 7);

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
        PoolFencer darthVader = new PoolFencer(fencerNamed("Darth Vader"), 9);

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
        PoolFencer darthVader = new PoolFencer(fencerNamed("Darth Vader"), 9);

        assertThatExceptionOfType(FencerNotInPoolException.class).isThrownBy(
                () -> testPool.getBoutBetween(aramis, darthVader)
        ).withMessageContaining("There is no bout between");
    }

    @Test
    void shouldBeEqualToItself() {
        assertThat(testPool).isEqualTo(testPool).hasSameHashCodeAs(testPool);
    }

    @Test
    void shouldRemainEqualToItselfRegardlessOfBoutProgress() {
        int hashCodeBefore = testPool.hashCode();

        testPool.recordResult().withWinner(athos, 5).withDefeated(dArtagnan, 2).record();

        assertThat(testPool).isEqualTo(testPool);
        assertThat(testPool.hashCode()).isEqualTo(hashCodeBefore);
    }

    @Test
    void shouldNotBeEqualToAnotherPoolWithTheSameFencers() {
        Pool anotherPool = Pool.buildFrom(dArtagnan, athos, porthos, aramis, inigo, westley, musashi);

        assertThat(testPool).isNotEqualTo(anotherPool);
        assertThat(testPool.getId()).isNotEqualTo(anotherPool.getId());
    }

    @Test
    void shouldNotBeEqualToNull() {
        assertThat(testPool).isNotEqualTo(null);
    }

    @Test
    void shouldNotBeEqualToAnInstanceOfAnUnrelatedType() {
        assertThat(testPool).isNotEqualTo(dArtagnan);
    }

    @Test
    void shouldReconstitutePoolWithItsIdAndBoutProgress() {
        testPool.recordResult().withWinner(athos, 5).withDefeated(dArtagnan, 2).record();

        Pool restored = Pool.reconstitute(testPool.getId(), testPool.getBouts());

        assertThat(restored.getId()).isEqualTo(testPool.getId());
        assertThat(restored.getBouts()).hasSize(21);
        assertThat(restored.getBoutBetween(athos, dArtagnan)).hasFinished()
                .hasWinnerWithScore(athos, 5).hasLoserWithScore(dArtagnan, 2);
    }

    @Test
    void shouldBeEqualWhenReconstitutedWithTheSameIdRegardlessOfBouts() {
        Pool anotherPool = Pool.buildFrom(dArtagnan, athos, porthos, aramis, inigo, westley, musashi);

        Pool restored = Pool.reconstitute(testPool.getId(), anotherPool.getBouts());

        assertThat(restored).isEqualTo(testPool).hasSameHashCodeAs(testPool);
    }

    @Test
    void shouldNotReconstitutePoolWithoutAnId() {
        assertThatNullPointerException().isThrownBy(() -> Pool.reconstitute(null, testPool.getBouts()))
                .withMessage("A pool must have an id");
    }

    @Test
    void shouldNotReconstitutePoolWithoutBouts() {
        assertThatExceptionOfType(InvalidPoolException.class).isThrownBy(
                () -> Pool.reconstitute(UUID.randomUUID(), Set.of())).withMessage("A pool must have bouts");
    }

    @Test
    void shouldNotReconstitutePoolMissingABout() {
        Set<Bout> bouts = new HashSet<>(testPool.getBouts());
        bouts.remove(testPool.getBoutBetween(athos, dArtagnan));

        assertThatExceptionOfType(InvalidPoolException.class).isThrownBy(
                () -> Pool.reconstitute(UUID.randomUUID(), bouts)).withMessage(
                "A pool must have exactly one bout between every pair of fencers");
    }

    @Test
    void shouldNotReconstitutePoolWithTheSameFencerAtDifferentPositions() {
        PoolFencer athosAgain = new PoolFencer(athos.fencer(), 4);
        Set<Bout> bouts = Set.of(new Bout(athos, porthos), new Bout(porthos, dArtagnan),
                new Bout(athosAgain, dArtagnan));

        assertThatExceptionOfType(InvalidPoolException.class).isThrownBy(
                () -> Pool.reconstitute(UUID.randomUUID(), bouts)).withMessage("A pool cannot have duplicate fencers");
    }

    @Test
    void shouldNotReconstitutePoolWithFencersSharingAPosition() {
        PoolFencer sameSpotAsDArtagnan = new PoolFencer(fencerNamed("Cyrano"), dArtagnan.position());
        Set<Bout> bouts = Set.of(new Bout(dArtagnan, athos), new Bout(athos, sameSpotAsDArtagnan));

        assertThatExceptionOfType(InvalidPoolException.class).isThrownBy(
                () -> Pool.reconstitute(UUID.randomUUID(), bouts)).withMessage(
                "Fencers in a pool must have different positions");
    }
}
