package dpd.lab.sports.fencing.pool;

import com.google.gson.Gson;
import dpd.lab.sports.fencing.pool.bout.Bout;
import dpd.lab.sports.fencing.pool.exceptions.InvalidPoolException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Pool {

    private static final Gson GSON = new Gson();

    private final Set<Bout> bouts;

    private Pool(Set<Bout> bouts) {
        this.bouts = bouts;
    }

    public static Pool buildFrom(PoolFencer... players) {
        Set<PoolFencer> fencers = new HashSet<>(Arrays.asList(players));

        if (fencers.size() != players.length) {
            throw new InvalidPoolException("A pool cannot have duplicate fencers");
        }

        if (fencers.size() < 3) {
            throw new InvalidPoolException("A pool must have at least 3 fencers");
        }

        return buildFrom(fencers);
    }

    public static Pool buildFrom(Set<PoolFencer> fencers) {
        List<PoolFencer> orderedFencers = new ArrayList<>(fencers);
        Set<Bout> bouts = new HashSet<>();

        for (int i = 0; i < orderedFencers.size(); i++) {
            for (int j = i + 1; j < orderedFencers.size(); j++) {
                bouts.add(new Bout(orderedFencers.get(i), orderedFencers.get(j)));
            }
        }

        return new Pool(bouts);
    }

    public Set<Bout> getBouts() {
        return bouts;
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }
}
