package edts.adam.smartinventoryordersystem.model;

import edts.adam.smartinventoryordersystem.enums.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a customer order.
 * Contains order items, pricing (total/discount/final), and follows a
 * state machine lifecycle: CREATED → PAID or CANCELLED.
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "{order.customer.notnull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * Order items with cascade ALL + orphanRemoval.
     * - CascadeType.ALL: when Order is persisted/updated/deleted, OrderItems
     * follow.
     * - orphanRemoval: if an OrderItem is removed from the list, it's deleted from
     * DB.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    @NotNull(message = "{order.totalamount.notnull}")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @NotNull(message = "{order.discountamount.notnull}")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotNull(message = "{order.finalamount.notnull}")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal finalAmount;

    @NotNull(message = "{order.status.notnull}")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.CREATED;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // === Constructors ===

    public Order() {
    }

    public Order(Customer customer) {
        this.customer = customer;
        this.totalAmount = BigDecimal.ZERO;
        this.finalAmount = BigDecimal.ZERO;
    }

    // === Helper Methods ===

    /**
     * Adds an OrderItem to this order and sets the bidirectional relationship.
     */
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    /**
     * Removes an OrderItem from this order and clears the bidirectional
     * relationship.
     */
    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
    }

    // === Getters & Setters ===

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
