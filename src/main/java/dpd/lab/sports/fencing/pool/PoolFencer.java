package dpd.lab.sports.fencing.pool;

import com.google.gson.Gson;
import dpd.lab.sports.fencing.Fencer;

public record PoolFencer(Fencer fencer, Integer position) {

    private static final Gson GSON = new Gson();

    @Override
    public String toString() {
        return GSON.toJson(this);
    }
}
