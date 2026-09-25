package dpd.lab.sports.fencing.pool;

import com.google.gson.Gson;
import dpd.lab.sports.fencing.pool.bout.Bout;
import dpd.lab.sports.fencing.pool.exceptions.FencerNotInPoolException;
import dpd.lab.sports.fencing.pool.exceptions.InvalidPoolException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
