package dpd.lab.sports.fencing.pool.exceptions;

public class FencerNotInPoolException extends IllegalArgumentException {
    public FencerNotInPoolException(String message) {
        super(message);
    }
}
