create table chat
(
    id          bigserial not null primary key,
    name        varchar   not null,
    chat_id     bigint    not null,
    unique (chat_id, name)
);

create table media
(
    id          bigserial not null primary key,
    name        varchar   not null,
    status      varchar   not null,
    type        varchar   not null,
    chat_id     bigint    not null references chat(id) ON DELETE CASCADE,
    rating      int CHECK (rating IS NULL OR (rating BETWEEN 1 AND 10)),
    description varchar,
    unique (chat_id, name, type)
);


