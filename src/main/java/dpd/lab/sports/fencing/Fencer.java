package dpd.lab.sports.fencing;

import com.google.gson.Gson;

import java.util.Objects;
import java.util.UUID;

/**
 * A fencer as this service knows them: a reference to a fencer owned by another service, identified by the id it
 * assigned, plus the name captured when the pool was created. Identity is the {@code id}, so two fencers with the
 * same name are different people unless they share an id.
 */
public record Fencer(UUID id, String name) {

    private static final Gson GSON = new Gson();

    public Fencer {
        Objects.requireNonNull(id, "A fencer must have an id");

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A fencer must have a name");
        }
    }

    @Override
    public boolean equals(Object that) {
        if (this == that)
            return true;

        if (!(that instanceof Fencer thatFencer))
            return false;

        return Objects.equals(id, thatFencer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }
}
