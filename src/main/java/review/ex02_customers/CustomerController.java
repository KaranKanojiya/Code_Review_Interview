package review.ex02_customers;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerRepository repository;
    private final PasswordEncoder passwordEncoder;

    public CustomerController(CustomerRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<CustomerSummary> search(@RequestParam String name, @RequestParam(defaultValue = "1000") int limit) {
        List<CustomerSummary> result = new ArrayList<>();
        for (Customer c : repository.searchByName(name, limit)) {
            result.add(new CustomerSummary(c.id(), c.name(), c.email(), c.passwordHash(), repository.countOrders(c.id())));
        }
        return result;
    }

    @PostMapping
    public CustomerSummary create(@RequestBody NewCustomer request) {
        long id = repository.insert(request.name(), request.email(), request.password());
        return new CustomerSummary(id, request.name(), request.email(), request.password(), 0);
    }
}
