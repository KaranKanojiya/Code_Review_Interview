package review.ex02_customers;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class CustomerRepository {

    private final JdbcTemplate jdbc;

    public CustomerRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Customer> searchByName(String name, int limit) {
        String sql = "SELECT id, name, email, password_hash FROM customers WHERE name LIKE '%" + name + "%' LIMIT " + limit;
        return jdbc.query(sql, (rs, row) -> new Customer(
                rs.getLong("id"), rs.getString("name"), rs.getString("email"), rs.getString("password_hash")));
    }

    public int countOrders(long customerId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM orders WHERE customer_id = " + customerId, Integer.class);
        return count == null ? 0 : count;
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
