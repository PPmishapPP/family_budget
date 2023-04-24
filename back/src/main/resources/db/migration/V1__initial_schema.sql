create table account
(
    id      bigserial not null primary key,
    name    varchar   not null,
    status  boolean   not null,
    chat_id bigint    not null,
    unique (chat_id, name)
);

create table periodic_change
(
    id      bigserial   not null primary key,
    name    varchar(50) not null,
    chat_id bigint      not null,
    unique (chat_id, name)
);

create table periodic_change_rule
(
    id                   bigserial   not null primary key,
    periodic_change_id   bigint      not null references periodic_change (id),
    target_account_id    bigint      not null references account (id),
    receiving_account_id bigint references account (id),
    sum                  int         not null,
    type                 varchar(10) not null,
    pass                 int default 0,
    start_day            date        not null
);

create table account_history
(
    id                 bigserial not null primary key,
    account_id         bigint    not null references account (id),
    periodic_change_id bigint references periodic_change (id),
    sum                int       not null,
    balance            int       not null,
    date_time          timestamp not null,
    comment            varchar(256)
);

