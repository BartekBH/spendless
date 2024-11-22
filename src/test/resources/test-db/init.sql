CREATE TABLE "user" (
    id UUID NOT NULL PRIMARY KEY,
    email TEXT NOT NULL,
    name TEXT NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    modified_at  TIMESTAMP NOT NULL
);

CREATE INDEX ON "user" (email);

CREATE TABLE user_password(
  user_id UUID PRIMARY KEY NOT NULL,
  password TEXT NOT NULL
);
CREATE UNIQUE INDEX ON  user_password(user_id);

CREATE TABLE budget (
    id UUID NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    created_by  UUID      NOT NULL REFERENCES "user"(id),
    modified_at  TIMESTAMP NOT NULL,
    modified_by UUID      NOT NULL REFERENCES "user"(id)
);

CREATE TABLE budget_user (
    budget_id UUID NOT NULL REFERENCES budget(id),
    user_id UUID NOT NULL REFERENCES "user"(id),
    PRIMARY KEY (budget_id, user_id)
);