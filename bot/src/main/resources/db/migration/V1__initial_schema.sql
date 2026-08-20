create table account
(
    id     bigserial not null primary key,
    name   varchar   not null,
    status boolean   not null
);

create table account_history
(
    id         bigserial not null primary key,
    account_id bigint    not null references account (id),
    sum        int       not null,
    balance    int       not null,
    date_time  timestamp not null,
    comment    varchar(256)
);

create table media
(
    id          bigserial not null primary key,
    name        varchar   not null,
    status      varchar   not null,
    type        varchar   not null,
    account_id  bigint    not null,
    rating      int check (rating is null or (rating between 1 and 10)),
    description varchar
);

create table periodic_change_rule
(
    id                bigserial   not null primary key,
    target_account_id bigint      not null references account (id),
    name              varchar     not null,
    sum               int         not null,
    type              varchar(10) not null,
    pass              int         not null default 0,
    next_day          date        not null,
    active            boolean     not null default true,
    end_date          date
);

create table app_user
(
    id        bigserial not null primary key,
    login     varchar   not null unique,
    password  varchar   not null,
    user_zone varchar   not null
);

create table user_account
(
    id         bigserial not null primary key,
    user_id    bigint    not null references app_user (id),
    account_id bigint    not null references account (id)
);
