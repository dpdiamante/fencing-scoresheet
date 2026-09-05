package dpd.lab.sports.fencing.pool.bout;

import java.util.Objects;
import java.util.Optional;

public class Fencer {

    private final dpd.lab.sports.fencing.pool.Fencer fencer;

    private int score;

    private FencerStatus status;

    Fencer(dpd.lab.sports.fencing.pool.Fencer fencer) {
        this.fencer = fencer;
    }

    public dpd.lab.sports.fencing.pool.Fencer getFencer() {
        return fencer;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getScore() {
        return score;
    }

    public Optional<FencerStatus> getStatus() {
        return Optional.ofNullable(status);
    }

    public void setStatus(FencerStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that)
            return true;

        if (!(that instanceof Fencer thatFencer))
            return false;

        return Objects.equals(fencer, thatFencer.fencer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fencer);
    }
}
