package dpd.lab.sports.fencing;

import java.util.UUID;

public final class FencerFixtures {

    private FencerFixtures() {
    }

    public static Fencer fencerNamed(String name) {
        return new Fencer(UUID.randomUUID(), name);
    }
}
