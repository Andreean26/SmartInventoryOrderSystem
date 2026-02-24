package edts.adam.smartinventoryordersystem.dto.order.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO representing a single item (product + quantity) within an order.
 */
public class OrderItemRequest {

    @NotNull(message = "{orderitem.productid.notnull}")
    private Long productId;

    @NotNull(message = "{orderitem.quantity.notnull}")
    @Min(value = 1, message = "{orderitem.quantity.min}")
    private Integer quantity;

    // === Getters & Setters ===

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
