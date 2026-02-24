package edts.adam.smartinventoryordersystem.dto.order.response;

import edts.adam.smartinventoryordersystem.model.OrderItem;

import java.math.BigDecimal;

/** Response DTO representing a line item within an order response. */
public class OrderItemResponse {

    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal priceAtPurchase;
    private BigDecimal subtotal;

    // === Static Factory Method ===

    public static OrderItemResponse fromEntity(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        response.setQuantity(item.getQuantity());
        response.setPriceAtPurchase(item.getPriceAtPurchase());
        response.setSubtotal(item.getSubtotal());
        return response;
    }

    // === Getters & Setters ===

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
