ALTER TABLE media ADD COLUMN account_id BIGINT;
ALTER TABLE periodic_change_rule DROP COLUMN periodic_change_id;
DROP TABLE periodic_change;
