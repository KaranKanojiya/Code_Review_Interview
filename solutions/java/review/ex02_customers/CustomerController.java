package review.ex02_customers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    static final int MAX_PAGE_SIZE = 100;

    private final CustomerRepository repository;
    private final PasswordEncoder passwordEncoder;

    public CustomerController(CustomerRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<CustomerSummary> search(@RequestParam String name, @RequestParam(defaultValue = "20") int limit) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        int pageSize = Math.clamp(limit, 1, MAX_PAGE_SIZE);
        List<Customer> customers = repository.searchByName(name, pageSize);
        Map<Long, Integer> orderCounts = repository.countOrders(customers.stream().map(Customer::id).toList());
        return customers.stream()
                .map(c -> new CustomerSummary(c.id(), c.name(), c.email(), orderCounts.getOrDefault(c.id(), 0)))
                .toList();
    }

    @PostMapping
    public ResponseEntity<CustomerSummary> create(@Valid @RequestBody NewCustomer request) {
        long id = repository.insert(request.name(), request.email(), passwordEncoder.encode(request.password()));
        return ResponseEntity.created(URI.create("/customers/" + id))
                .body(new CustomerSummary(id, request.name(), request.email(), 0));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ProblemDetail> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage()));
    }
}
