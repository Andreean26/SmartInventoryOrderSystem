package edts.adam.smartinventoryordersystem.dto.order.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Request DTO for creating a new order with a customer and list of items. */
public class CreateOrderRequest {

    @NotNull(message = "{order.customerid.notnull}")
    private Long customerId;

    @NotEmpty(message = "{order.items.notempty}")
    @Valid
    private List<OrderItemRequest> items;

    // === Getters & Setters ===

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}
