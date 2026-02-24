package edts.adam.smartinventoryordersystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * JPA entity representing a line item within an order.
 * Captures the product price at purchase time to preserve historical accuracy.
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "{orderitem.order.notnull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull(message = "{orderitem.product.notnull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "{orderitem.quantity.notnull}")
    @Min(value = 1, message = "{orderitem.quantity.min}")
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Snapshot of the product price at the time of order creation.
     * This ensures historical accuracy even if the product price
     * is updated later.
     */
    @NotNull(message = "{orderitem.priceatpurchase.notnull}")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal priceAtPurchase;

    // === Constructors ===

    public OrderItem() {
    }

    public OrderItem(Product product, Integer quantity, BigDecimal priceAtPurchase) {
        this.product = product;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
    }

    // === Business Methods ===

    /**
     * Calculates subtotal for this line item: priceAtPurchase × quantity.
     */
    public BigDecimal getSubtotal() {
        return priceAtPurchase.multiply(new BigDecimal(quantity));
    }

    // === Getters & Setters ===

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPriceAtPurchase() {
        return priceAtPurchase;
    }

    public void setPriceAtPurchase(BigDecimal priceAtPurchase) {
        this.priceAtPurchase = priceAtPurchase;
    }
}
