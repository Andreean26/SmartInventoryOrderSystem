package edts.adam.smartinventoryordersystem.dto.customer.response;

import edts.adam.smartinventoryordersystem.model.Customer;
import edts.adam.smartinventoryordersystem.enums.MembershipLevel;

import java.math.BigDecimal;

/** Response DTO representing a customer entity returned to the client. */
public class CustomerResponse {

    private Long id;
    private String name;
    private String email;
    private MembershipLevel membershipLevel;
    private BigDecimal totalSpent;
    private Boolean active;

    // === Static Factory Method ===

    public static CustomerResponse fromEntity(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setName(customer.getName());
        response.setEmail(customer.getEmail());
        response.setMembershipLevel(customer.getMembershipLevel());
        response.setTotalSpent(customer.getTotalSpent());
        response.setActive(customer.getActive());
        return response;
    }

    // === Getters & Setters ===

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public MembershipLevel getMembershipLevel() {
        return membershipLevel;
    }

    public void setMembershipLevel(MembershipLevel membershipLevel) {
        this.membershipLevel = membershipLevel;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
