create table regular_tasks
(
    id                    bigserial not null primary key,
    name                  varchar   not null,
    type                  varchar   not null,
    start_date            timestamp not null,
    description           varchar   not null,
    pass                  int default 0,
    chat_id               bigserial not null,
    datetime_notification timestamp not null,
    unique (name, chat_id)
);


