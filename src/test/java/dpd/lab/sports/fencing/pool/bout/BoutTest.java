package dpd.lab.sports.fencing.pool.bout;

import dpd.lab.sports.fencing.pool.PoolFencer;
import dpd.lab.sports.fencing.pool.bout.exceptions.InvalidBoutException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dpd.lab.sports.fencing.FencerFixtures.fencerNamed;
import static dpd.lab.sports.fencing.pool.PoolAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BoutTest {

    private PoolFencer zorro;

    private PoolFencer luke;

    @BeforeEach
    void setUp() {
        zorro = new PoolFencer(fencerNamed("Zorro"), 1);
        luke = new PoolFencer(fencerNamed("Luke"), 5);
    }

    @Test
    void shouldThrowExceptionWhenInstantiatedWithTheSameFencers() {
        assertThatThrownBy(() -> new Bout(zorro, zorro))
                .isInstanceOf(InvalidBoutException.class)
                .hasMessage("A bout must have 2 different fencers");
    }

    @Test
    void shouldThrowExceptionWhenInstantiatedWithFencersOfTheSamePosition() {
        PoolFencer vader = new PoolFencer(fencerNamed("Darth"), 1);

        assertThatThrownBy(() -> new Bout(zorro, vader))
                .isInstanceOf(InvalidBoutException.class)
                .hasMessage("Bout fencers must have different positions");
    }

    @Test
    void shouldSuccessfullyCreateBout() {
        assertThat(new Bout(zorro, luke))
                .hasPlayer(zorro).hasPlayer(luke)
                .hasNotStartedYet()
                .hasNoWinner().hasNoLoser();
    }

    @Test
    void shouldStartMatchProperly() {
        Bout testBout = new Bout(zorro, luke);
        testBout.startBout();

        assertThat(testBout).hasStarted().notFinishedYet()
                .hasNoWinner().hasNoLoser();
    }

    @Test
    void shouldFinishMatchProperly() {
        Bout testBout = new Bout(zorro, luke);
        testBout.finishBout().withWinner(zorro, 5).withDefeated(luke, 3).conclude();

        assertThat(testBout).hasFinished()
                .hasWinnerWithScore(zorro, 5)
                .hasLoserWithScore(luke, 3);
    }

    @Test
    void shouldFinishMatchWhenSomeoneRetired() {
        Bout testBout = new Bout(zorro, luke);
        testBout.finishBout().withRetired(zorro, 5).withWinner(luke, 3).conclude();

        assertThat(testBout).hasFinished()
                .hasLoserWithScore(zorro, 5)
                .hasWinnerWithScore(luke, 3);
    }

    @Test
    void shouldNotConcludeBoutWithNoWinner() {
        Bout testBout = new Bout(zorro, luke);
        Bout.BoutConclusion conclusion = testBout.finishBout().withDefeated(zorro, 5);

        assertThatThrownBy(conclusion::conclude)
                .isInstanceOf(InvalidBoutException.class)
                .hasMessage("A winner and a defeated fencer must both be specified");
    }

    @Test
    void shouldNotConcludeBoutWithNoLoser() {
        Bout testBout = new Bout(zorro, luke);
        Bout.BoutConclusion conclusion = testBout.finishBout().withWinner(zorro, 5);

        assertThatThrownBy(conclusion::conclude)
                .isInstanceOf(InvalidBoutException.class)
                .hasMessage("A winner and a defeated fencer must both be specified");
    }

    @Test
    void shouldNotConcludeBoutWithSameWinnerOrLoser() {
        Bout testBout = new Bout(zorro, luke);
        Bout.BoutConclusion conclusion = testBout.finishBout().withWinner(zorro, 5).withDefeated(zorro, 5);

        assertThatThrownBy(conclusion::conclude)
                .isInstanceOf(InvalidBoutException.class)
                .hasMessage("The winner and the defeated fencer must be different");
    }

    @Test
    void shouldBeEqualToItself() {
        Bout bout = new Bout(zorro, luke);

        Assertions.assertThat(bout).isEqualTo(bout).hasSameHashCodeAs(bout);
    }

    @Test
    void shouldBeEqualWhenWrappingTheSameFencersRegardlessOfOrder() {
        Bout first = new Bout(zorro, luke);
        Bout second = new Bout(luke, zorro);

        Assertions.assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
    }

    @Test
    void shouldRemainEqualRegardlessOfBoutProgress() {
        Bout first = new Bout(zorro, luke);
        Bout second = new Bout(zorro, luke);

        first.startBout();
        first.finishBout().withWinner(zorro, 5).withDefeated(luke, 3).conclude();

        Assertions.assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
    }

    @Test
    void shouldNotBeEqualWhenWrappingDifferentFencers() {
        PoolFencer vader = new PoolFencer(fencerNamed("Darth"), 2);

        Bout first = new Bout(zorro, luke);
        Bout second = new Bout(zorro, vader);

        Assertions.assertThat(first).isNotEqualTo(second);
    }

    @Test
    void shouldNotBeEqualToNull() {
        Bout bout = new Bout(zorro, luke);

        Assertions.assertThat(bout).isNotEqualTo(null);
    }

    @Test
    void shouldNotBeEqualToAnInstanceOfAnUnrelatedType() {
        Bout bout = new Bout(zorro, luke);

        Assertions.assertThat(bout).isNotEqualTo(zorro);
    }

    @Test
    void shouldReconstituteBoutThatHasNotStarted() {
        Bout bout = Bout.reconstitute(BoutStatus.NOT_STARTED,
                new BoutFencerState(zorro, 0, null), new BoutFencerState(luke, 0, null));

        assertThat(bout).hasPlayer(zorro).hasPlayer(luke)
                .hasNotStartedYet()
                .hasNoWinner().hasNoLoser();
        Assertions.assertThat(bout).isEqualTo(new Bout(zorro, luke));
    }

    @Test
    void shouldReconstituteBoutThatIsOngoing() {
        Bout bout = Bout.reconstitute(BoutStatus.ONGOING,
                new BoutFencerState(zorro, 0, null), new BoutFencerState(luke, 0, null));

        assertThat(bout).hasStarted().notFinishedYet()
                .hasNoWinner().hasNoLoser();
    }

    @Test
    void shouldReconstituteFinishedBoutWithItsScores() {
        Bout bout = Bout.reconstitute(BoutStatus.FINISHED,
                new BoutFencerState(zorro, 5, FencerStatus.VICTOR),
                new BoutFencerState(luke, 3, FencerStatus.DEFEAT));

        assertThat(bout).hasFinished()
                .hasWinnerWithScore(zorro, 5)
                .hasLoserWithScore(luke, 3);
    }

    @Test
    void shouldReconstituteFinishedBoutWhenSomeoneRetiredRegardlessOfArgumentOrder() {
        Bout bout = Bout.reconstitute(BoutStatus.FINISHED,
                new BoutFencerState(zorro, 5, FencerStatus.RETIRED),
                new BoutFencerState(luke, 3, FencerStatus.VICTOR));

        assertThat(bout).hasFinished()
                .hasLoserWithScore(zorro, 5)
                .hasWinnerWithScore(luke, 3);
        Assertions.assertThat(bout.getDefeated().orElseThrow().getStatus()).contains(FencerStatus.RETIRED);
    }

    @Test
    void shouldReconstituteTheSameBoutThatWasFinishedTheRegularWay() {
        Bout original = new Bout(zorro, luke);
        original.finishBout().withWinner(zorro, 5).withDefeated(luke, 3).conclude();

        Bout restored = Bout.reconstitute(BoutStatus.FINISHED,
                new BoutFencerState(luke, 3, FencerStatus.DEFEAT),
                new BoutFencerState(zorro, 5, FencerStatus.VICTOR));

        Assertions.assertThat(restored).isEqualTo(original).hasSameHashCodeAs(original);
        assertThat(restored).hasFinished().hasWinnerWithScore(zorro, 5).hasLoserWithScore(luke, 3);
    }

    @Test
    void shouldNotReconstituteUnfinishedBoutWithResults() {
        assertThatThrownBy(() -> Bout.reconstitute(BoutStatus.ONGOING,
                new BoutFencerState(zorro, 5, FencerStatus.VICTOR), new BoutFencerState(luke, 0, null)))
                .isInstanceOf(InvalidBoutException.class)
                .hasMessage("A bout that has not finished cannot have results");
    }

    @Test
    void shouldNotReconstituteFinishedBoutWithoutResults() {
        assertThatThrownBy(() -> Bout.reconstitute(BoutStatus.FINISHED,
                new BoutFencerState(zorro, 0, null), new BoutFencerState(luke, 0, null)))
                .isInstanceOf(InvalidBoutException.class)
                .hasMessage("A finished bout must have exactly one winner and one defeated fencer");
    }

    @Test
    void shouldNotReconstituteFinishedBoutWithTwoWinners() {
        assertThatThrownBy(() -> Bout.reconstitute(BoutStatus.FINISHED,
                new BoutFencerState(zorro, 5, FencerStatus.VICTOR),
                new BoutFencerState(luke, 5, FencerStatus.VICTOR)))
                .isInstanceOf(InvalidBoutException.class)
                .hasMessage("A finished bout must have exactly one winner and one defeated fencer");
    }

    @Test
    void shouldNotReconstituteFinishedBoutWithoutAWinner() {
        assertThatThrownBy(() -> Bout.reconstitute(BoutStatus.FINISHED,
                new BoutFencerState(zorro, 5, FencerStatus.DEFEAT),
                new BoutFencerState(luke, 5, FencerStatus.RETIRED)))
                .isInstanceOf(InvalidBoutException.class)
                .hasMessage("A finished bout must have exactly one winner and one defeated fencer");
    }

    @Test
    void shouldNotReconstituteBoutBetweenTheSameFencer() {
        assertThatThrownBy(() -> Bout.reconstitute(BoutStatus.NOT_STARTED,
                new BoutFencerState(zorro, 0, null), new BoutFencerState(zorro, 0, null)))
                .isInstanceOf(InvalidBoutException.class)
                .hasMessage("A bout must have 2 different fencers");
    }

    @Test
    void shouldNotReconstituteBoutWithoutAStatus() {
        assertThatThrownBy(() -> Bout.reconstitute(null,
                new BoutFencerState(zorro, 0, null), new BoutFencerState(luke, 0, null)))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("A bout must have a status");
    }
}
