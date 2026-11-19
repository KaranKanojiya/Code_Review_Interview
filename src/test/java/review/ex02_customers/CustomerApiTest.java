package review.ex02_customers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomerApiTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private EmbeddedDatabase db;
    private JdbcTemplate jdbc;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        db = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).generateUniqueName(true).build();
        jdbc = new JdbcTemplate(db);
        jdbc.execute("CREATE TABLE customers (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), email VARCHAR(100), password_hash VARCHAR(100))");
        jdbc.execute("CREATE TABLE orders (id BIGINT AUTO_INCREMENT PRIMARY KEY, customer_id BIGINT)");
        jdbc.update("INSERT INTO customers (name, email, password_hash) VALUES ('alice', 'alice@example.com', 'hash-a')");
        jdbc.update("INSERT INTO customers (name, email, password_hash) VALUES ('bob', 'bob@example.com', 'hash-b')");
        jdbc.update("INSERT INTO orders (customer_id) VALUES (1)");
        mockMvc = MockMvcBuilders.standaloneSetup(new CustomerController(new CustomerRepository(jdbc), encoder)).build();
    }

    @AfterEach
    void tearDown() {
        db.shutdown();
    }

    @Test
    void searchIsNotVulnerableToSqlInjection() throws Exception {
        mockMvc.perform(get("/customers").param("name", "x' OR 1=1 --"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void responseNeverContainsPasswordData() throws Exception {
        mockMvc.perform(get("/customers").param("name", "ali"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderCount").value(1))
                .andExpect(content().string(not(containsString("hash-a"))))
                .andExpect(content().string(not(containsString("password"))));
    }

    @Test
    void pageSizeIsCapped() throws Exception {
        for (int i = 0; i < 150; i++) {
            jdbc.update("INSERT INTO customers (name, email, password_hash) VALUES (?, ?, 'h')", "bulk-" + i, i + "@example.com");
        }
        mockMvc.perform(get("/customers").param("name", "bulk").param("limit", "100000"))
                .andExpect(jsonPath("$", hasSize(lessThanOrEqualTo(100))));
    }

    @Test
    void blankSearchIsRejected() throws Exception {
        mockMvc.perform(get("/customers").param("name", " ")).andExpect(status().isBadRequest());
    }

    @Test
    void createReturns201WithLocationAndStoresAHash() throws Exception {
        mockMvc.perform(post("/customers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"carol\",\"email\":\"carol@example.com\",\"password\":\"correct-horse-battery\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(content().string(not(containsString("correct-horse-battery"))));

        String stored = jdbc.queryForObject("SELECT password_hash FROM customers WHERE name = 'carol'", String.class);
        assertThat(stored).isNotEqualTo("correct-horse-battery");
        assertThat(encoder.matches("correct-horse-battery", stored)).isTrue();
    }
}
