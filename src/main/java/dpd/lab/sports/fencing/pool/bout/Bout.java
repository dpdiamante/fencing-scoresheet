package dpd.lab.sports.fencing.pool.bout;

import com.google.gson.Gson;
import dpd.lab.sports.fencing.pool.PoolFencer;
import dpd.lab.sports.fencing.pool.bout.exceptions.FencerNotFoundException;
import dpd.lab.sports.fencing.pool.bout.exceptions.InvalidBoutException;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class Bout {

    private static final Gson GSON = new Gson();

    private final Set<BoutFencer> fencers;

    private BoutStatus status = BoutStatus.NOT_STARTED;

    public Bout(PoolFencer fencer, PoolFencer anotherFencer) {
        if (fencer.equals(anotherFencer)) {
            throw new InvalidBoutException("A bout must have 2 different fencers");
        }

        if (fencer.position().equals(anotherFencer.position())) {
            throw new InvalidBoutException("Bout fencers must have different positions");
        }

        this.fencers = Set.of(new BoutFencer(fencer), new BoutFencer(anotherFencer));
    }

    /**
     * Restores a previously persisted bout as it was saved, including its progress. Meant for repositories, use the
     * constructor to create a new bout.
     */
    public static Bout reconstitute(BoutStatus status, BoutFencerState fencer, BoutFencerState anotherFencer) {
        Objects.requireNonNull(status, "A bout must have a status");
        requireResultsMatch(status, fencer, anotherFencer);

        Bout bout = new Bout(fencer.fencer(), anotherFencer.fencer());
        bout.status = status;
        bout.restore(fencer);
        bout.restore(anotherFencer);

        return bout;
    }

    private static void requireResultsMatch(BoutStatus status, BoutFencerState fencer,
                                            BoutFencerState anotherFencer) {
        if (status != BoutStatus.FINISHED) {
            if (fencer.result() != null || anotherFencer.result() != null) {
                throw new InvalidBoutException("A bout that has not finished cannot have results");
            }
            return;
        }

        boolean hasOneVictor = (fencer.result() == FencerStatus.VICTOR && isDefeated(anotherFencer.result()))
                || (anotherFencer.result() == FencerStatus.VICTOR && isDefeated(fencer.result()));

        if (!hasOneVictor) {
            throw new InvalidBoutException("A finished bout must have exactly one winner and one defeated fencer");
        }
    }

    private static boolean isDefeated(FencerStatus result) {
        return result == FencerStatus.DEFEAT || result == FencerStatus.RETIRED;
    }

    private void restore(BoutFencerState state) {
        BoutFencer boutFencer = findFencer(state.fencer());
        boutFencer.setScore(state.score());
        boutFencer.setStatus(state.result());
    }

    public Set<BoutFencer> getFencers() {
        return fencers;
    }

    public BoutStatus getStatus() {
        return status;
    }

    public void setStatus(BoutStatus status) {
        this.status = status;
    }

    public void startBout() {
        if (status == BoutStatus.FINISHED) {
            throw new InvalidBoutException("The bout is already finished");
        }

        status = BoutStatus.ONGOING;
    }

    public boolean hasFencer(PoolFencer fencer) {
        return fencers.stream().anyMatch(e -> e.getFencer().equals(fencer));
    }

    public Optional<BoutFencer> getWinner() {
        if (!isFinished()) {
            return Optional.empty();
        }

        return fencers.stream().filter(e -> {
            if (e.getStatus().isPresent()) {
                return e.getStatus().get().equals(FencerStatus.VICTOR);
            }

            return false;
        }).findFirst();
    }

    public Optional<BoutFencer> getDefeated() {
        if (!isFinished()) {
            return Optional.empty();
        }

        return fencers.stream().filter(e -> {
            if (e.getStatus().isPresent()) {
                FencerStatus fencerStatus = e.getStatus().get();

                return fencerStatus.equals(FencerStatus.DEFEAT) || fencerStatus.equals(FencerStatus.RETIRED);
            }

            return false;
        }).findFirst();
    }

    public boolean isFinished() {
        return status == BoutStatus.FINISHED;
    }

    public boolean hasStarted() {
        return status == BoutStatus.ONGOING || status == BoutStatus.FINISHED;
    }

    public BoutConclusion finishBout() {
        return new BoutConclusion();
    }

    private BoutFencer findFencer(PoolFencer fencer) {
        return fencers.stream()
                .filter(boutFencer -> boutFencer.getFencer().equals(fencer))
                .findFirst()
                .orElseThrow(() -> new FencerNotFoundException(fencer + " is not part of this bout"));
    }

    @Override
    public boolean equals(Object that) {
        if (this == that)
            return true;

        if (!(that instanceof Bout thatBout))
            return false;

        return Objects.equals(fencers, thatBout.fencers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fencers);
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }

    public class BoutConclusion {

        private BoutFencer winner;

        private BoutFencer defeated;

        public BoutConclusion withWinner(PoolFencer fencer, Integer score) {
            winner = findFencer(fencer);
            winner.setScore(score);
            winner.setStatus(FencerStatus.VICTOR);
            return this;
        }

        public BoutConclusion withDefeated(PoolFencer fencer, Integer score) {
            defeated = findFencer(fencer);
            defeated.setScore(score);
            defeated.setStatus(FencerStatus.DEFEAT);
            return this;
        }

        public BoutConclusion withRetired(PoolFencer fencer, Integer score) {
            defeated = findFencer(fencer);
            defeated.setScore(score);
            defeated.setStatus(FencerStatus.RETIRED);
            return this;
        }

        public Bout conclude() {
            if (winner == null || defeated == null) {
                throw new InvalidBoutException("A winner and a defeated fencer must both be specified");
            }

            if (winner.equals(defeated)) {
                throw new InvalidBoutException("The winner and the defeated fencer must be different");
            }

            status = BoutStatus.FINISHED;

            return Bout.this;
        }
    }

}
