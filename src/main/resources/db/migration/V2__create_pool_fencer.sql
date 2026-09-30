-- A fencer's place in a pool. It is part of the pool aggregate, so it has no id of its own: it is identified
-- by its position within the pool.
--
-- Fencers are owned by another service, so `fencer_id` is a plain reference with no foreign key. `fencer_name`
-- is a snapshot taken when the pool was created, so the pool never depends on that service to be displayed.
CREATE TABLE pool_fencer (
    pool_id      UUID NOT NULL REFERENCES pool (id) ON DELETE CASCADE,
    position     INT  NOT NULL CHECK (position >= 1),
    fencer_id    UUID NOT NULL,
    fencer_name  TEXT NOT NULL CHECK (btrim(fencer_name) <> ''),
    PRIMARY KEY (pool_id, position),
    UNIQUE (pool_id, fencer_id)
);
