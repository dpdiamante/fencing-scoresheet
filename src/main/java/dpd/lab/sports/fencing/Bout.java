package dpd.lab.sports.fencing;

import dpd.lab.sports.fencing.exceptions.FencerNotFoundException;
import dpd.lab.sports.fencing.exceptions.InvalidBoutException;

import java.util.Set;

public class Bout {

    private final BoutFencer firstBoutFencer;

    private final BoutFencer secondBoutFencer;

    private BoutStatus status = BoutStatus.NOT_STARTED;

    public Bout(Fencer firstFencer, Fencer secondFencer) {
        if (firstFencer == null || secondFencer == null) {
            throw new InvalidBoutException("Fencers cannot be null");
        }

        this.firstBoutFencer = new BoutFencer(firstFencer);
        this.secondBoutFencer = new BoutFencer(secondFencer);
    }

    public Fencer getFirstFencer() {
        return firstBoutFencer.getFencer();
    }

    public Fencer getSecondFencer() {
        return secondBoutFencer.getFencer();
    }

    public BoutStatus getStatus() {
        return status;
    }

    public Set<Fencer> getFencers() {
        return Set.of(firstBoutFencer.getFencer(), secondBoutFencer.getFencer());
    }

    public Bout addPoint(Fencer fencer) {
        if (status == BoutStatus.NOT_STARTED) {
            status = BoutStatus.ONGOING;
        }

        if (firstBoutFencer.getFencer().equals(fencer)) {
            firstBoutFencer.addPoint();
            return this;
        } else if (secondBoutFencer.getFencer().equals(fencer)) {
            secondBoutFencer.addPoint();
            return this;
        } else {
            throw new FencerNotFoundException(fencer + " not found");
        }
    }

    public Bout retireFencer(Fencer fencer) {
        BoutFencer victor;
        BoutFencer defeated;

        if (firstBoutFencer.getFencer().equals(fencer)) {
            defeated = firstBoutFencer;
            victor = secondBoutFencer;
        } else {
            defeated = secondBoutFencer;
            victor = firstBoutFencer;
        }

        victor.setStatus(BoutFencerStatus.VICTOR);
        defeated.setStatus(BoutFencerStatus.DEFEAT);

        status = BoutStatus.FINISHED;
        return this;
    }

    public Bout conclude() {
        if (firstBoutFencer.getScore().equals(secondBoutFencer.getScore())) {
            throw new InvalidBoutException("Cannot conclude a bout with equal scores");
        }

        BoutFencer victor;
        BoutFencer defeated;

        if (firstBoutFencer.getScore() >  secondBoutFencer.getScore()) {
            victor = firstBoutFencer;
            defeated = secondBoutFencer;
        } else {
            victor = secondBoutFencer;
            defeated = firstBoutFencer;
        }

        victor.setStatus(BoutFencerStatus.VICTOR);
        defeated.setStatus(BoutFencerStatus.DEFEAT);

        status = BoutStatus.FINISHED;
        return this;
    }

}
