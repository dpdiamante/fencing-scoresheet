package dpd.lab.sports.fencing.pool.bout.assertions;

import dpd.lab.sports.fencing.pool.bout.Bout;

public class BoutAssertions {

    public static BoutAssert assertThat(Bout actual) {
        return new BoutAssert(actual);
    }
}
