CREATE TABLE users (
    id UUID NOT NULL PRIMARY KEY,
    email TEXT NOT NULL,
    password TEXT NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    modified_at  TIMESTAMP NOT NULL
);

CREATE INDEX ON users (email);

CREATE TABLE user_password(
  user_id UUID PRIMARY KEY NOT NULL,
  password TEXT NOT NULL
);
CREATE UNIQUE INDEX ON  user_password(user_id);

CREATE TABLE budget (
    id UUID NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    modified_at  TIMESTAMP NOT NULL
);