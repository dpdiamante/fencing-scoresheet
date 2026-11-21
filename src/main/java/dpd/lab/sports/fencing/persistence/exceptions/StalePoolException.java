package dpd.lab.sports.fencing.persistence.exceptions;

public class StalePoolException extends RuntimeException {
    public StalePoolException(String message) {
        super(message);
    }
}
