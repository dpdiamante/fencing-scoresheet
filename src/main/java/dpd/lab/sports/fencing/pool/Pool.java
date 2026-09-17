package dpd.lab.sports.fencing.pool;

import dpd.lab.sports.fencing.pool.bout.Bout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Pool {

    private final Set<Bout> bouts;

    private Pool(Set<Bout> bouts) {
        this.bouts = bouts;
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
}
