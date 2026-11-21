package dpd.lab.sports.fencing.persistence;

import dpd.lab.sports.fencing.persistence.exceptions.PoolAlreadyExistsException;
import dpd.lab.sports.fencing.persistence.exceptions.StalePoolException;
import dpd.lab.sports.fencing.pool.Pool;
import dpd.lab.sports.fencing.pool.PoolFencer;
import dpd.lab.sports.fencing.pool.bout.FencerStatus;
import org.flywaydb.core.Flyway;
import org.jooq.CloseableDSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;
import java.util.stream.Collectors;

import static dpd.lab.sports.fencing.FencerFixtures.fencerNamed;
import static dpd.lab.sports.fencing.pool.PoolAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Runs against a real Postgres, started once for the class and migrated with the project's Flyway migrations.
 */
public class JooqPoolRepositoryTest {

    private static PostgreSQLContainer postgres;

    private static CloseableDSLContext dsl;

    private JooqPoolRepository repository;

    private PoolFencer dArtagnan;

    private PoolFencer athos;

    private PoolFencer porthos;

    private PoolFencer aramis;

    private Pool pool;

    @BeforeAll
    static void startDatabase() {
        postgres = new PostgreSQLContainer("postgres:18");
        postgres.start();

        Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .load()
                .migrate();

        dsl = DSL.using(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    }

    @AfterAll
    static void stopDatabase() {
        dsl.close();
        postgres.stop();
    }

    @BeforeEach
    void setUp() {
        dsl.execute("truncate pool cascade");
        repository = new JooqPoolRepository(dsl);

        dArtagnan = new PoolFencer(fencerNamed("D'Artagnan"), 1);
        athos = new PoolFencer(fencerNamed("Athos"), 2);
        porthos = new PoolFencer(fencerNamed("Porthos"), 3);
        aramis = new PoolFencer(fencerNamed("Aramis"), 4);

        pool = Pool.buildFrom(dArtagnan, athos, porthos, aramis);
    }

    @Test
    void shouldLoadAPoolThatWasCreatedAtTheInitialVersion() {
        repository.create(pool);

        Versioned<Pool> loaded = repository.load(pool.getId()).orElseThrow();

        assertThat(loaded.version()).isZero();
        assertThat(loaded.aggregate()).isEqualTo(pool);
        assertThat(loaded.aggregate().getBouts()).isEqualTo(pool.getBouts()).hasSize(6);
        assertThat(loaded.aggregate().getBoutBetween(athos, porthos)).hasNotStartedYet();
    }

    @Test
    void shouldKeepTheFencerIdsNamesAndPositions() {
        repository.create(pool);

        Pool loaded = repository.load(pool.getId()).orElseThrow().aggregate();

        assertThat(loaded.getBouts().stream()
                .flatMap(bout -> bout.getFencers().stream())
                .map(side -> side.getFencer().fencer().name() + "@" + side.getFencer().position())
                .collect(Collectors.toSet()))
                .containsExactlyInAnyOrder("D'Artagnan@1", "Athos@2", "Porthos@3", "Aramis@4");
        assertThat(loaded.getBoutsOf(athos)).hasSize(3);
    }

    @Test
    void shouldNotLoadAPoolThatDoesNotExist() {
        assertThat(repository.load(UUID.randomUUID())).isEmpty();
    }

    @Test
    void shouldNotCreateAPoolTwice() {
        repository.create(pool);

        assertThatExceptionOfType(PoolAlreadyExistsException.class).isThrownBy(() -> repository.create(pool))
                .withMessage("Pool " + pool.getId() + " already exists");
        assertThat(repository.load(pool.getId()).orElseThrow().version()).isZero();
    }

    @Test
    void shouldSaveTheProgressOfBoutsAndReturnTheNewVersion() {
        repository.create(pool);
        pool.recordResult().withWinner(athos, 5).withDefeated(porthos, 2).record();
        pool.recordResult().withWinner(dArtagnan, 5).withRetired(aramis, 1).record();
        pool.getBoutBetween(athos, aramis).startBout();

        int newVersion = repository.save(pool, 0);

        assertThat(newVersion).isEqualTo(1);

        Versioned<Pool> loaded = repository.load(pool.getId()).orElseThrow();
        assertThat(loaded.version()).isEqualTo(1);
        assertThat(loaded.aggregate().getBoutBetween(athos, porthos)).hasFinished()
                .hasWinnerWithScore(athos, 5).hasLoserWithScore(porthos, 2);
        assertThat(loaded.aggregate().getBoutBetween(dArtagnan, aramis)).hasFinished()
                .hasWinnerWithScore(dArtagnan, 5).hasLoserWithScore(aramis, 1);
        assertThat(loaded.aggregate().getBoutBetween(dArtagnan, aramis).getDefeated().orElseThrow().getStatus())
                .contains(FencerStatus.RETIRED);
        assertThat(loaded.aggregate().getBoutBetween(athos, aramis)).hasStarted().notFinishedYet();
        assertThat(loaded.aggregate().getBoutBetween(dArtagnan, porthos)).hasNotStartedYet();
    }

    @Test
    void shouldAllowSavingAgainFromTheVersionThatWasReturned() {
        repository.create(pool);
        pool.recordResult().withWinner(athos, 5).withDefeated(porthos, 2).record();
        int firstVersion = repository.save(pool, 0);

        pool.recordResult().withWinner(aramis, 5).withDefeated(dArtagnan, 4).record();
        int secondVersion = repository.save(pool, firstVersion);

        assertThat(secondVersion).isEqualTo(2);
        assertThat(repository.load(pool.getId()).orElseThrow().version()).isEqualTo(2);
    }

    @Test
    void shouldNotOverwriteAChangeMadeSinceThePoolWasLoaded() {
        repository.create(pool);
        Versioned<Pool> refereeOne = repository.load(pool.getId()).orElseThrow();
        Versioned<Pool> refereeTwo = repository.load(pool.getId()).orElseThrow();

        refereeOne.aggregate().recordResult().withWinner(athos, 5).withDefeated(porthos, 2).record();
        repository.save(refereeOne.aggregate(), refereeOne.version());

        refereeTwo.aggregate().recordResult().withWinner(aramis, 5).withDefeated(dArtagnan, 4).record();
        assertThatExceptionOfType(StalePoolException.class)
                .isThrownBy(() -> repository.save(refereeTwo.aggregate(), refereeTwo.version()));

        Versioned<Pool> stored = repository.load(pool.getId()).orElseThrow();
        assertThat(stored.version()).isEqualTo(1);
        assertThat(stored.aggregate().getBoutBetween(athos, porthos)).hasFinished();
        assertThat(stored.aggregate().getBoutBetween(aramis, dArtagnan)).hasNotStartedYet();
    }

    @Test
    void shouldNotSaveAPoolThatWasNeverCreated() {
        assertThatExceptionOfType(StalePoolException.class).isThrownBy(() -> repository.save(pool, 0));
    }
}
