package dpd.lab.sports.fencing.pool;

import com.google.gson.Gson;
import dpd.lab.sports.fencing.pool.bout.Bout;
import dpd.lab.sports.fencing.pool.bout.BoutFencer;
import dpd.lab.sports.fencing.pool.exceptions.FencerNotInPoolException;
import dpd.lab.sports.fencing.pool.exceptions.InvalidPoolException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Pool {

    private static final Gson GSON = new Gson();

    private final UUID id;

    private final Set<Bout> bouts;

    private Pool(UUID id, Set<Bout> bouts) {
        this.id = id;
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

        return new Pool(UUID.randomUUID(), bouts);
    }

    /**
     * Restores a previously persisted pool as it was saved, including the progress of its bouts. Meant for
     * repositories, use {@link #buildFrom} to create a new pool. Creation rules such as the minimum number of
     * fencers are not checked again, but the bouts must still form a consistent pool.
     */
    public static Pool reconstitute(UUID id, Set<Bout> bouts) {
        Objects.requireNonNull(id, "A pool must have an id");
        requireConsistent(bouts);

        return new Pool(id, new HashSet<>(bouts));
    }

    private static void requireConsistent(Set<Bout> bouts) {
        if (bouts.isEmpty()) {
            throw new InvalidPoolException("A pool must have bouts");
        }

        Set<PoolFencer> fencers = bouts.stream()
                .flatMap(bout -> bout.getFencers().stream())
                .map(BoutFencer::getFencer)
                .collect(Collectors.toSet());

        if (fencers.stream().map(PoolFencer::fencer).distinct().count() != fencers.size()) {
            throw new InvalidPoolException("A pool cannot have duplicate fencers");
        }

        if (fencers.stream().map(PoolFencer::position).distinct().count() != fencers.size()) {
            throw new InvalidPoolException("Fencers in a pool must have different positions");
        }

        if (bouts.size() != fencers.size() * (fencers.size() - 1) / 2) {
            throw new InvalidPoolException("A pool must have exactly one bout between every pair of fencers");
        }
    }

    public UUID getId() {
        return id;
    }

    public Set<Bout> getBouts() {
        return Collections.unmodifiableSet(bouts);
    }

    public Bout getBoutBetween(PoolFencer fencer, PoolFencer anotherFencer) {
        return bouts.stream()
                .filter(e -> e.hasFencer(fencer) && e.hasFencer(anotherFencer))
                .findFirst()
                .orElseThrow(() -> new FencerNotInPoolException(
                        "There is no bout between " + fencer + " and " + anotherFencer));
    }

    public Set<Bout> getBoutsOf(PoolFencer fencer) {
        return bouts.stream()
                .filter(e -> e.hasFencer(fencer))
                .collect(Collectors.toSet());
    }

    public ResultRecorder recordResult() {
        return new ResultRecorder();
    }

    @Override
    public boolean equals(Object that) {
        if (this == that)
            return true;

        if (!(that instanceof Pool thatPool))
            return false;

        return Objects.equals(id, thatPool.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }

    public class ResultRecorder {

        private PoolFencer winner;

        private int winnerScore;

        private PoolFencer defeated;

        private int defeatedScore;

        private boolean retired;

        public ResultRecorder withWinner(PoolFencer fencer, int score) {
            winner = fencer;
            winnerScore = score;
            return this;
        }

        public ResultRecorder withDefeated(PoolFencer fencer, int score) {
            defeated = fencer;
            defeatedScore = score;
            retired = false;
            return this;
        }

        public ResultRecorder withRetired(PoolFencer fencer, int score) {
            defeated = fencer;
            defeatedScore = score;
            retired = true;
            return this;
        }

        public Bout record() {
            if (winner == null || defeated == null) {
                throw new InvalidPoolException("A winner and a defeated fencer must both be specified");
            }

            Bout bout = getBoutBetween(winner, defeated);
            Bout.BoutConclusion conclusion = bout.finishBout().withWinner(winner, winnerScore);

            if (retired) {
                conclusion.withRetired(defeated, defeatedScore);
            } else {
                conclusion.withDefeated(defeated, defeatedScore);
            }

            return conclusion.conclude();
        }
    }
}
