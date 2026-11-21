package dpd.lab.sports.fencing.persistence;

import dpd.lab.sports.fencing.Fencer;
import dpd.lab.sports.fencing.persistence.exceptions.PoolAlreadyExistsException;
import dpd.lab.sports.fencing.persistence.exceptions.StalePoolException;
import dpd.lab.sports.fencing.persistence.jooq.tables.records.BoutRecord;
import dpd.lab.sports.fencing.persistence.jooq.tables.records.PoolFencerRecord;
import dpd.lab.sports.fencing.pool.Pool;
import dpd.lab.sports.fencing.pool.PoolFencer;
import dpd.lab.sports.fencing.pool.bout.Bout;
import dpd.lab.sports.fencing.pool.bout.BoutFencer;
import dpd.lab.sports.fencing.pool.bout.BoutFencerState;
import dpd.lab.sports.fencing.pool.bout.BoutStatus;
import dpd.lab.sports.fencing.pool.bout.FencerStatus;
import org.jooq.DSLContext;
import org.jooq.Query;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static dpd.lab.sports.fencing.persistence.jooq.Tables.BOUT;
import static dpd.lab.sports.fencing.persistence.jooq.Tables.POOL;
import static dpd.lab.sports.fencing.persistence.jooq.Tables.POOL_FENCER;

public class JooqPoolRepository implements PoolRepository {

    private static final int INITIAL_VERSION = 0;

    private final DSLContext dsl;

    public JooqPoolRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void create(Pool pool) {
        dsl.transaction(configuration -> {
            DSLContext tx = configuration.dsl();

            int inserted = tx.insertInto(POOL)
                    .set(POOL.ID, pool.getId())
                    .set(POOL.VERSION, INITIAL_VERSION)
                    .onConflictDoNothing()
                    .execute();

            if (inserted == 0) {
                throw new PoolAlreadyExistsException("Pool " + pool.getId() + " already exists");
            }

            insertFencers(tx, pool);
            insertBouts(tx, pool);
        });
    }

    @Override
    public Optional<Versioned<Pool>> load(UUID id) {
        return dsl.transactionResult(configuration -> {
            DSLContext tx = configuration.dsl();

            // The pool, its fencers and its bouts are read by separate queries, so read them all from one snapshot
            // to make sure the version returned belongs to the state returned.
            tx.execute("set transaction isolation level repeatable read read only");

            Optional<Integer> version = tx.select(POOL.VERSION).from(POOL).where(POOL.ID.eq(id))
                    .fetchOptional(POOL.VERSION);

            if (version.isEmpty()) {
                return Optional.empty();
            }

            Map<Integer, PoolFencer> fencersByPosition = tx.selectFrom(POOL_FENCER)
                    .where(POOL_FENCER.POOL_ID.eq(id))
                    .fetch().stream()
                    .map(JooqPoolRepository::toPoolFencer)
                    .collect(Collectors.toMap(PoolFencer::position, Function.identity()));

            Set<Bout> bouts = tx.selectFrom(BOUT)
                    .where(BOUT.POOL_ID.eq(id))
                    .fetch().stream()
                    .map(row -> toBout(row, fencersByPosition))
                    .collect(Collectors.toSet());

            return Optional.of(new Versioned<>(Pool.reconstitute(id, bouts), version.get()));
        });
    }

    @Override
    public int save(Pool pool, int expectedVersion) {
        return dsl.transactionResult(configuration -> {
            DSLContext tx = configuration.dsl();

            // The version check and the bump are one statement, so of two concurrent saves from the same version
            // exactly one finds it still current.
            Optional<Integer> newVersion = tx.update(POOL)
                    .set(POOL.VERSION, POOL.VERSION.plus(1))
                    .where(POOL.ID.eq(pool.getId()).and(POOL.VERSION.eq(expectedVersion)))
                    .returning(POOL.VERSION)
                    .fetchOptional(POOL.VERSION);

            if (newVersion.isEmpty()) {
                throw new StalePoolException("Pool " + pool.getId() + " is no longer at version " + expectedVersion
                        + ", it was changed or removed since it was loaded");
            }

            updateBouts(tx, pool);

            return newVersion.get();
        });
    }

    private static void insertFencers(DSLContext tx, Pool pool) {
        List<PoolFencerRecord> rows = fencersOf(pool).stream().map(fencer -> {
            PoolFencerRecord row = tx.newRecord(POOL_FENCER);
            row.setPoolId(pool.getId());
            row.setPosition(fencer.position());
            row.setFencerId(fencer.fencer().id());
            row.setFencerName(fencer.fencer().name());
            return row;
        }).toList();

        tx.batchInsert(rows).execute();
    }

    private static void insertBouts(DSLContext tx, Pool pool) {
        List<BoutRecord> rows = pool.getBouts().stream().map(bout -> {
            List<BoutFencer> sides = sidesOf(bout);
            BoutRecord row = tx.newRecord(BOUT);
            row.setPoolId(pool.getId());
            row.setAPosition(sides.get(0).getFencer().position());
            row.setBPosition(sides.get(1).getFencer().position());
            row.setStatus(bout.getStatus().name());
            row.setAScore(sides.get(0).getScore());
            row.setAResult(resultOf(sides.get(0)));
            row.setBScore(sides.get(1).getScore());
            row.setBResult(resultOf(sides.get(1)));
            return row;
        }).toList();

        tx.batchInsert(rows).execute();
    }

    /**
     * The fencers of a pool and the pairs they fence never change once it exists (a different draw means a new
     * pool), so saving only has to update how far each bout has got.
     */
    private static void updateBouts(DSLContext tx, Pool pool) {
        List<Query> updates = pool.getBouts().stream().map(bout -> {
            List<BoutFencer> sides = sidesOf(bout);
            return (Query) tx.update(BOUT)
                    .set(BOUT.STATUS, bout.getStatus().name())
                    .set(BOUT.A_SCORE, sides.get(0).getScore())
                    .set(BOUT.A_RESULT, resultOf(sides.get(0)))
                    .set(BOUT.B_SCORE, sides.get(1).getScore())
                    .set(BOUT.B_RESULT, resultOf(sides.get(1)))
                    .where(BOUT.POOL_ID.eq(pool.getId()))
                    .and(BOUT.A_POSITION.eq(sides.get(0).getFencer().position()))
                    .and(BOUT.B_POSITION.eq(sides.get(1).getFencer().position()));
        }).toList();

        boolean anyBoutMissing = Arrays.stream(tx.batch(updates).execute()).anyMatch(updated -> updated == 0);

        if (anyBoutMissing) {
            throw new IllegalStateException("Pool " + pool.getId() + " does not have the same bouts as the stored pool");
        }
    }

    private static Set<PoolFencer> fencersOf(Pool pool) {
        return pool.getBouts().stream()
                .flatMap(bout -> bout.getFencers().stream())
                .map(BoutFencer::getFencer)
                .collect(Collectors.toSet());
    }

    /**
     * A bout holds its two fencers as an unordered set, the table always stores the lower position first.
     */
    private static List<BoutFencer> sidesOf(Bout bout) {
        return bout.getFencers().stream()
                .sorted(Comparator.comparing(side -> side.getFencer().position()))
                .toList();
    }

    private static String resultOf(BoutFencer side) {
        return side.getStatus().map(Enum::name).orElse(null);
    }

    private static PoolFencer toPoolFencer(PoolFencerRecord row) {
        return new PoolFencer(new Fencer(row.getFencerId(), row.getFencerName()), row.getPosition());
    }

    private static Bout toBout(BoutRecord row, Map<Integer, PoolFencer> fencersByPosition) {
        return Bout.reconstitute(BoutStatus.valueOf(row.getStatus()),
                toState(fencersByPosition.get(row.getAPosition()), row.getAScore(), row.getAResult()),
                toState(fencersByPosition.get(row.getBPosition()), row.getBScore(), row.getBResult()));
    }

    private static BoutFencerState toState(PoolFencer fencer, int score, String result) {
        return new BoutFencerState(fencer, score, result == null ? null : FencerStatus.valueOf(result));
    }
}
