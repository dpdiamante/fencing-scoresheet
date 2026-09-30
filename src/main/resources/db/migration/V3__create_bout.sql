-- A bout between two fencers of a pool. It belongs to the pool aggregate, so it is identified by the pair
-- of positions, always stored smaller first so each pair of fencers has exactly one row.
CREATE TABLE bout (
    pool_id    UUID NOT NULL,
    a_position INT  NOT NULL,
    b_position INT  NOT NULL,
    status     TEXT NOT NULL DEFAULT 'NOT_STARTED'
               CHECK (status IN ('NOT_STARTED', 'ONGOING', 'FINISHED')),
    a_score    INT  NOT NULL DEFAULT 0 CHECK (a_score >= 0),
    a_result   TEXT CHECK (a_result IN ('VICTOR', 'DEFEAT', 'RETIRED')),
    b_score    INT  NOT NULL DEFAULT 0 CHECK (b_score >= 0),
    b_result   TEXT CHECK (b_result IN ('VICTOR', 'DEFEAT', 'RETIRED')),

    PRIMARY KEY (pool_id, a_position, b_position),
    CHECK (a_position < b_position),
    FOREIGN KEY (pool_id, a_position) REFERENCES pool_fencer (pool_id, position) ON DELETE CASCADE,
    FOREIGN KEY (pool_id, b_position) REFERENCES pool_fencer (pool_id, position) ON DELETE CASCADE,

    -- Results exist only once the bout is finished, and then there is exactly one victor. The IS NOT NULL
    -- guards matter: a CHECK passes when its expression evaluates to NULL.
    CONSTRAINT bout_results_match_status CHECK (
        (status = 'FINISHED'
            AND a_result IS NOT NULL AND b_result IS NOT NULL
            AND ((a_result = 'VICTOR' AND b_result IN ('DEFEAT', 'RETIRED'))
              OR (b_result = 'VICTOR' AND a_result IN ('DEFEAT', 'RETIRED'))))
        OR
        (status <> 'FINISHED' AND a_result IS NULL AND b_result IS NULL)
    )
);
