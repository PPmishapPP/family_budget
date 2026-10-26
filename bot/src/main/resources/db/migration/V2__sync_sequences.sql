DO
$$
    DECLARE
        table_name text;
        max_id     bigint;
    BEGIN
        FOREACH table_name IN ARRAY ARRAY [
            'account',
            'account_history',
            'media',
            'periodic_change_rule',
            'app_user',
            'user_account'
            ]
            LOOP
                EXECUTE format('SELECT COALESCE(MAX(id), 0) FROM %I', table_name) INTO max_id;
                IF max_id = 0 THEN
                    PERFORM setval(pg_get_serial_sequence(table_name, 'id'), 1, false);
                ELSE
                    PERFORM setval(pg_get_serial_sequence(table_name, 'id'), max_id, true);
                END IF;
            END LOOP;
    END
$$;
