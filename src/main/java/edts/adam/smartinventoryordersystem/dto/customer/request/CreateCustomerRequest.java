package edts.adam.smartinventoryordersystem.dto.customer.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Request DTO for registering a new customer. */
public class CreateCustomerRequest {

    @NotBlank(message = "{customer.name.notblank}")
    private String name;

    @NotBlank(message = "{customer.email.notblank}")
    @Email(message = "{customer.email.invalid}")
    private String email;

    // === Getters & Setters ===

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
