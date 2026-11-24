create table refresh_token
(
    id         bigserial   not null primary key,
    user_id    bigint      not null references app_user (id) on delete cascade,
    token_hash varchar(64) not null unique,
    expires_at timestamp   not null,
    revoked    boolean     not null default false,
    created_at timestamp   not null
);

create index idx_refresh_token_user on refresh_token (user_id);
create index idx_refresh_token_expires on refresh_token (expires_at);
