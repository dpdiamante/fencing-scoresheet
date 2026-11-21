package dpd.lab.sports.fencing.persistence;

import dpd.lab.sports.fencing.persistence.exceptions.PoolAlreadyExistsException;
import dpd.lab.sports.fencing.persistence.exceptions.StalePoolException;
import dpd.lab.sports.fencing.pool.Pool;

import java.util.Optional;
import java.util.UUID;

/**
 * Stores whole pools. A pool is an aggregate, so everything inside it (fencers and bouts) is saved and loaded
 * together, and concurrent edits are detected with a single version on the pool.
 */
public interface PoolRepository {

    /**
     * Stores a new pool at version 0.
     *
     * @throws PoolAlreadyExistsException if a pool with the same id is already stored
     */
    void create(Pool pool);

    /**
     * Loads a pool together with the version it has now, which the caller passes back to {@link #save}.
     */
    Optional<Versioned<Pool>> load(UUID id);

    /**
     * Stores the current state of a pool that was loaded at {@code expectedVersion}.
     *
     * @return the new version of the stored pool
     * @throws StalePoolException if the stored pool no longer has {@code expectedVersion}, because it was changed
     *                            (or removed) since it was loaded. Reload it, apply the change again and retry.
     */
    int save(Pool pool, int expectedVersion);
}
