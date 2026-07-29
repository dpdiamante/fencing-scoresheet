package dpd.lab.sports.fencing;

class BoutFencer {

    private final Fencer fencer;

    private int score;

    private BoutFencerStatus status;

    public BoutFencer(Fencer fencer) {
        this.fencer = fencer;
    }

    public Fencer getFencer() {
        return fencer;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getScore() {
        return score;
    }

    public BoutFencerStatus getStatus() {
        return status;
    }

    public void setStatus(BoutFencerStatus status) {
        this.status = status;
    }

    public Integer addPoint() {
        score++;
        return score;
    }
}
