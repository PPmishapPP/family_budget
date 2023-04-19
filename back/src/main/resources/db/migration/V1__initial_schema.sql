create table account
(
    id     bigserial not null primary key,
    name   varchar   not null unique,
    status boolean   not null,
    chat_id bigint    not null
);

create table external_changes
(
    id   bigserial   not null primary key,
    sum  int         not null,
    name varchar(50) not null,
    type varchar(10) not null,
    pass int
);

create table account_history
(
    id                  bigserial not null primary key,
    account_id          bigint    not null references account (id),
    external_changes_id bigint references external_changes (id),
    sum                 int       not null,
    balance             int       not null,
    date_time           timestamp not null,
    comment             varchar(256)
);

