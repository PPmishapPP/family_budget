create table nurse_visit
(
    id          bigserial not null primary key,
    visit_day         date      not null,
    chat_id     bigint    not null,
    visit_start timestamp not null,
    visit_end   timestamp
);