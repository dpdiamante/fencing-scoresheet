package dpd.lab.sports.fencing.pool.bout.assertions;

import dpd.lab.sports.fencing.pool.PoolFencer;
import dpd.lab.sports.fencing.pool.bout.Bout;
import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class BoutAssert extends AbstractAssert<BoutAssert, Bout> {

    BoutAssert(Bout actual) {
        super(actual, BoutAssert.class);
    }

    public BoutAssert hasPlayer(PoolFencer fencer) {
        assertThat(actual.hasFencer(fencer)).isTrue();

        return myself;
    }

    public BoutAssert hasNotStartedYet() {
        assertThat(actual.hasStarted()).isFalse();

        return myself;
    }

    public BoutAssert hasStarted() {
        assertThat(actual.hasStarted()).isTrue();

        return myself;
    }

    public BoutAssert notFinishedYet() {
        assertThat(actual.isFinished()).isFalse();

        return myself;
    }

    public BoutAssert hasFinished() {
        assertThat(actual.isFinished()).isTrue();

        return myself;
    }

    public BoutAssert hasNoWinner() {
        assertThat(actual.getWinner()).isEmpty();

        return myself;
    }

    public BoutAssert hasWinnerWithScore(PoolFencer fencer, int score) {
        assertThat(actual.getWinner()).isNotEmpty();
        assertThat(actual.getWinner().get().getFencer()).isEqualTo(fencer);
        assertThat(actual.getWinner().get().getScore()).isEqualTo(score);

        return myself;
    }

    public BoutAssert hasLoserWithScore(PoolFencer fencer, int score) {
        assertThat(actual.getDefeated()).isNotEmpty();
        assertThat(actual.getDefeated().get().getFencer()).isEqualTo(fencer);
        assertThat(actual.getDefeated().get().getScore()).isEqualTo(score);

        return myself;
    }

    public BoutAssert hasNoLoser() {
        assertThat(actual.getDefeated()).isEmpty();

        return myself;
    }

}
