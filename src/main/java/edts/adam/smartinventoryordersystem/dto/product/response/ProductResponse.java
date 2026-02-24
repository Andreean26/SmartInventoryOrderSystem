package edts.adam.smartinventoryordersystem.dto.product.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import edts.adam.smartinventoryordersystem.model.Product;
import edts.adam.smartinventoryordersystem.enums.ProductCategory;

import java.math.BigDecimal;
import java.time.Instant;

/** Response DTO representing a product entity returned to the client. */
@JsonPropertyOrder({ "id", "name", "category", "price", "stock", "active", "createdAt", "updatedAt" })
public class ProductResponse {

    private Long id;
    private String name;
    private ProductCategory category;
    private BigDecimal price;
    private Integer stock;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    // === Static Factory Method ===

    public static ProductResponse fromEntity(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setCategory(product.getCategory());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setActive(product.getActive());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
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

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
