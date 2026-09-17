package dpd.lab.sports.fencing.pool.bout;

import dpd.lab.sports.fencing.pool.PoolFencer;

import java.util.Objects;
import java.util.Optional;

public class BoutFencer {

    private final PoolFencer fencer;

    private int score;

    private FencerStatus status;

    BoutFencer(PoolFencer fencer) {
        this.fencer = fencer;
    }

    public PoolFencer getFencer() {
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

        if (!(that instanceof BoutFencer thatFencer))
            return false;

        return Objects.equals(fencer, thatFencer.fencer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fencer);
    }
}
