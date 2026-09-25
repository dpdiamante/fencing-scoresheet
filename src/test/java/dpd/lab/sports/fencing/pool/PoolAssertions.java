package dpd.lab.sports.fencing.pool;

import dpd.lab.sports.fencing.pool.bout.Bout;
import dpd.lab.sports.fencing.pool.bout.assertions.BoutAssert;

public class PoolAssertions {

    public static BoutAssert assertThat(Bout actual) {
        return new BoutAssert(actual);
    }
}
