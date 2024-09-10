CREATE TABLE users (
    id UUID NOT NULL PRIMARY KEY,
    email TEXT NOT NULL,
    password TEXT NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    modified_at  TIMESTAMP NOT NULL
);

CREATE INDEX ON users (email);

CREATE TABLE budget (
    id UUID NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    modified_at  TIMESTAMP NOT NULL
);