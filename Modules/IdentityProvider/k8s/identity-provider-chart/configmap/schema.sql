CREATE TABLE IF NOT EXISTS "pseudonym"
(
    user_id   VARCHAR(120) PRIMARY KEY,
    pseudonym VARCHAR(36) NOT NULL
);