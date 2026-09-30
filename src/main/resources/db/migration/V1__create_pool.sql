-- Pool aggregate root. `version` backs optimistic locking for the whole aggregate:
-- every save bumps it with `UPDATE pool SET version = version + 1 WHERE id = ? AND version = ?`.
CREATE TABLE pool (
    id      UUID PRIMARY KEY,
    version INT  NOT NULL DEFAULT 0
);
