create table duty
(
    id           bigserial   not null primary key,
    name         varchar(50) not null,
    chat_id      bigint      not null,
    duty_account bigint references account (id),
    last_messages timestamp,
    last_user_id bigint references "user" (id),
    award        bigint default 0,
    unique (chat_id, name)
);

create table "user"
(
    id      bigserial   not null primary key,
    chat_id bigint      not null,
    name    varchar(50) not null,
    unique (chat_id, name)
);

create table duty_user
(
    user_id bigint references "user" (id),
    duty_id bigint references duty (id),
    primary key (user_id, duty_id)
)