package dpd.lab.sports.fencing;

import com.google.gson.Gson;

public record Fencer(String name) {

    private static final Gson GSON = new Gson();

    @Override
    public String toString() {
        return GSON.toJson(this);
    }
}
