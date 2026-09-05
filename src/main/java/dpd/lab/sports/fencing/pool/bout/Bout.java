package dpd.lab.sports.fencing.pool.bout;

import dpd.lab.sports.fencing.pool.bout.exceptions.FencerNotFoundException;
import dpd.lab.sports.fencing.pool.bout.exceptions.InvalidBoutException;

import java.util.Optional;
import java.util.Set;

public class Bout {

    private final Set<Fencer> fencers;

    private BoutStatus status = BoutStatus.NOT_STARTED;

    public Bout(dpd.lab.sports.fencing.pool.Fencer fencer, dpd.lab.sports.fencing.pool.Fencer anotherFencer) {
        if (fencer.equals(anotherFencer)) {
            throw new InvalidBoutException("A bout must have 2 different fencers");
        }

        if (fencer.position().equals(anotherFencer.position())) {
            throw new InvalidBoutException("Bout fencers must have different positions");
        }

        this.fencers = Set.of(new Fencer(fencer), new Fencer(anotherFencer));
    }

    public Set<Fencer> getFencers() {
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

    public boolean hasFencer(dpd.lab.sports.fencing.pool.Fencer fencer) {
        return fencers.stream().anyMatch(e -> e.getFencer().equals(fencer));
    }

    public Optional<Fencer> getWinner() {
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

    public Optional<Fencer> getDefeated() {
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

    private Fencer findFencer(dpd.lab.sports.fencing.pool.Fencer fencer) {
        return fencers.stream()
                .filter(boutFencer -> boutFencer.getFencer().equals(fencer))
                .findFirst()
                .orElseThrow(() -> new FencerNotFoundException(fencer + " is not part of this bout"));
    }

    public class BoutConclusion {

        private Fencer winner;

        private Fencer defeated;

        public BoutConclusion withWinner(dpd.lab.sports.fencing.pool.Fencer fencer, Integer score) {
            winner = findFencer(fencer);
            winner.setScore(score);
            winner.setStatus(FencerStatus.VICTOR);
            return this;
        }

        public BoutConclusion withDefeated(dpd.lab.sports.fencing.pool.Fencer fencer, Integer score) {
            defeated = findFencer(fencer);
            defeated.setScore(score);
            defeated.setStatus(FencerStatus.DEFEAT);
            return this;
        }

        public BoutConclusion withRetired(dpd.lab.sports.fencing.pool.Fencer fencer, Integer score) {
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
