package dpd.lab.sports.fencing.persistence.exceptions;

public class PoolAlreadyExistsException extends RuntimeException {
    public PoolAlreadyExistsException(String message) {
        super(message);
    }
}
