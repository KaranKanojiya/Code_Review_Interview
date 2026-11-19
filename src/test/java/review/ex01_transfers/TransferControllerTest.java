package review.ex01_transfers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransferControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InMemoryAccountRepository accounts = new InMemoryAccountRepository();
        accounts.save(new Account("A-1001", new BigDecimal("100.00")));
        accounts.save(new Account("B-2002", new BigDecimal("0.00")));
        mockMvc = MockMvcBuilders.standaloneSetup(new TransferController(new TransferService(accounts))).build();
    }

    @Test
    void successfulTransferReturns201() throws Exception {
        expectStatus("{\"fromAccount\":\"A-1001\",\"toAccount\":\"B-2002\",\"amount\":10}", 201);
    }

    @Test
    void unknownAccountReturns404NotA200() throws Exception {
        expectStatus("{\"fromAccount\":\"A-1001\",\"toAccount\":\"NOPE\",\"amount\":10}", 404);
    }

    @Test
    void invalidRequestReturns400() throws Exception {
        expectStatus("{\"fromAccount\":\"A-1001\",\"toAccount\":\"B-2002\",\"amount\":-10}", 400);
    }

    private void expectStatus(String json, int expectedStatus) throws Exception {
        mockMvc.perform(post("/transfers").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().is(expectedStatus));
    }
}
