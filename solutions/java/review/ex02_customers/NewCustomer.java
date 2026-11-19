package review.ex02_customers;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewCustomer(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 12, max = 128) String password) {

    @Override
    public String toString() {
        return "NewCustomer[name=" + name + ", email=" + email + ", password=****]";
    }
}
