package dpd.lab.sports.fencing.persistence;

import java.util.Objects;

/**
 * An aggregate root together with the version it had when it was loaded. The version is persistence bookkeeping
 * for optimistic locking, so it lives here and not in the domain: saving compares it with the stored version to
 * detect a concurrent edit.
 */
public record Versioned<T>(T aggregate, int version) {

    public Versioned {
        Objects.requireNonNull(aggregate, "A versioned aggregate must have an aggregate");

        if (version < 0) {
            throw new IllegalArgumentException("A version cannot be negative");
        }
    }
}
