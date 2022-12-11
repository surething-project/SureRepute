CREATE TABLE IF NOT EXISTS "pseudonym_score"
(
    pseudonym         VARCHAR(36) NOT NULL PRIMARY KEY,
    positive_behavior REAL        NOT NULL,
    negative_behavior REAL        NOT NULL
);

CREATE TABLE IF NOT EXISTS "pseudonym_leader"
(
    pseudonym VARCHAR(36) NOT NULL PRIMARY KEY REFERENCES pseudonym_score (pseudonym),
    server_id VARCHAR(36) NOT NULL
);

CREATE TABLE IF NOT EXISTS "pseudonym_follower"
(
    pseudonym VARCHAR(36) NOT NULL REFERENCES pseudonym_score (pseudonym),
    server_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (pseudonym, server_id)
);