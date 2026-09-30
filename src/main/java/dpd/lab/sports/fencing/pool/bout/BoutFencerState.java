package dpd.lab.sports.fencing.pool.bout;

import dpd.lab.sports.fencing.pool.PoolFencer;

/**
 * The stored state of one fencer in a bout, used to restore a bout through {@link Bout#reconstitute}.
 * The {@code result} is null until the bout is finished.
 */
public record BoutFencerState(PoolFencer fencer, int score, FencerStatus result) {
}
