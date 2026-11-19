package review.ex02_customers;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CustomerRepository {

    private static final RowMapper<Customer> MAPPER = (rs, row) -> new Customer(
            rs.getLong("id"), rs.getString("name"), rs.getString("email"), rs.getString("password_hash"));

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate named;

    public CustomerRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.named = new NamedParameterJdbcTemplate(jdbc);
    }

    /** Bind parameters, never string concatenation. LIKE wildcards in user input are escaped. */
    public List<Customer> searchByName(String name, int limit) {
        String pattern = "%" + name.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        return jdbc.query("SELECT id, name, email, password_hash FROM customers WHERE name LIKE ? ESCAPE '\\' ORDER BY id LIMIT ?",
                MAPPER, pattern, limit);
    }

    public int countOrders(long customerId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM orders WHERE customer_id = ?", Integer.class, customerId);
        return count == null ? 0 : count;
    }

    /** One query for all customers instead of one per customer (N+1). */
    public Map<Long, Integer> countOrders(Collection<Long> customerIds) {
        Map<Long, Integer> counts = new HashMap<>();
        if (customerIds.isEmpty()) {
            return counts;
        }
        named.query("SELECT customer_id, COUNT(*) AS c FROM orders WHERE customer_id IN (:ids) GROUP BY customer_id",
                new MapSqlParameterSource("ids", customerIds),
                rs -> {
                    counts.put(rs.getLong("customer_id"), rs.getInt("c"));
                });
        return counts;
    }

    public long insert(String name, String email, String passwordHash) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO customers (name, email, password_hash) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            return ps;
        }, keys);
        return keys.getKey().longValue();
    }
}
