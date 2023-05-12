create table todo
(
    id      bigserial not null primary key,
    description    varchar   not null,
    chat_id bigint    not null
)