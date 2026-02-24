package edts.adam.smartinventoryordersystem.model;

import edts.adam.smartinventoryordersystem.enums.MembershipLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * JPA entity representing a customer.
 * Tracks total spending and automatically manages membership level upgrades.
 */
@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{customer.name.notblank}")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "{customer.email.notblank}")
    @Email(message = "{customer.email.invalid}")
    @Column(nullable = false, unique = true)
    private String email;

    @NotNull(message = "{customer.membership.notnull}")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipLevel membershipLevel = MembershipLevel.REGULAR;

    @NotNull(message = "{customer.totalspent.notnull}")
    @DecimalMin(value = "0.00", message = "{customer.totalspent.min}")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean active = true;

    // === Constructors ===

    public Customer() {
    }

    public Customer(String name, String email) {
        this.name = name;
        this.email = email;
        this.active = true;
    }

    // === Business Methods ===

    /**
     * Adds the given amount to totalSpent and recalculates membership level.
     * Membership can only be upgraded, never downgraded.
     *
     * @param amount the payment amount to add
     */
    public void addSpent(BigDecimal amount) {
        this.totalSpent = this.totalSpent.add(amount);
        recalculateMembership();
    }

    /**
     * Recalculates the membership level based on current totalSpent.
     * Only upgrades — never downgrades the membership level.
     */
    private void recalculateMembership() {
        MembershipLevel newLevel = MembershipLevel.fromTotalSpent(this.totalSpent);
        // Only upgrade, never downgrade
        if (newLevel.ordinal() > this.membershipLevel.ordinal()) {
            this.membershipLevel = newLevel;
        }
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
