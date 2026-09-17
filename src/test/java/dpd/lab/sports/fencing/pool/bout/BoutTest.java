package dpd.lab.sports.fencing.pool.bout;

import dpd.lab.sports.fencing.Fencer;
import dpd.lab.sports.fencing.pool.PoolFencer;
import dpd.lab.sports.fencing.pool.bout.exceptions.InvalidBoutException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dpd.lab.sports.fencing.pool.bout.assertions.BoutAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BoutTest {

    private PoolFencer zorro;

    private PoolFencer luke;

    @BeforeEach
    void setUp() {
        zorro = new PoolFencer(new Fencer("Zorro"), 1);
        luke = new PoolFencer(new Fencer("Luke"), 5);
    }

    @Test
    void shouldThrowExceptionWhenInstantiatedWithTheSameFencers() {
        assertThatThrownBy(() -> new Bout(zorro, zorro))
                .isInstanceOf(InvalidBoutException.class)
                .hasMessage("A bout must have 2 different fencers");
    }

    @Test
    void shouldThrowExceptionWhenInstantiatedWithFencersOfTheSamePosition() {
        PoolFencer vader = new PoolFencer(new Fencer("Darth"), 1);

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
        PoolFencer vader = new PoolFencer(new Fencer("Darth"), 2);

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

}
