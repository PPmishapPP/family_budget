package ru.mishapp.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.mishapp.entity.AccountHistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class AccountHistoryRepositoryImpl implements AccountHistoryRepositoryCustom {

    private static final Set<String> SORT_COLUMNS =
            Set.of("comment", "sum", "date_time", "balance");

    private static final RowMapper<AccountHistory> ROW_MAPPER = (rs, rowNum) -> new AccountHistory(
            rs.getLong("id"),
            rs.getLong("account_id"),
            rs.getInt("sum"),
            rs.getInt("balance"),
            rs.getObject("date_time", LocalDateTime.class),
            rs.getString("comment")
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<AccountHistory> findHistoryPage(
            long accountId,
            int limit,
            long offset,
            String sortColumn,
            boolean ascending
    ) {
        if (!SORT_COLUMNS.contains(sortColumn)) {
            throw new IllegalArgumentException("Неизвестное поле сортировки: " + sortColumn);
        }
        String direction = ascending ? "asc" : "desc";
        String nulls = "comment".equals(sortColumn)
                ? (ascending ? " nulls first" : " nulls last")
                : "";
        String sql = """
                select id, account_id, sum, balance, date_time, comment
                from account_history
                where account_id = :accountId
                order by %s %s%s, id %s
                limit :limit offset :offset"""
                .formatted(sortColumn, direction, nulls, direction);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("accountId", accountId)
                .addValue("limit", limit)
                .addValue("offset", offset);
        return jdbcTemplate.query(sql, params, ROW_MAPPER);
    }
}
