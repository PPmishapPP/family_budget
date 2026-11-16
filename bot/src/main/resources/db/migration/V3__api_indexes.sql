create index if not exists idx_account_history_account_date
    on account_history (account_id, date_time);

create index if not exists idx_media_account_type
    on media (account_id, type);

create index if not exists idx_periodic_change_rule_target_account
    on periodic_change_rule (target_account_id);
