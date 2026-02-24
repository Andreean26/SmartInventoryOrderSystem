package edts.adam.smartinventoryordersystem.dto.product.request;

import edts.adam.smartinventoryordersystem.enums.ProductCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Request DTO for updating an existing product. */
public class UpdateProductRequest {

    @NotBlank(message = "{product.name.notblank}")
    private String name;

    @NotNull(message = "{product.category.notnull}")
    private ProductCategory category;

    @NotNull(message = "{product.price.notnull}")
    @DecimalMin(value = "0.01", message = "{product.price.min}")
    private BigDecimal price;

    @NotNull(message = "{product.stock.notnull}")
    @Min(value = 0, message = "{product.stock.min}")
    private Integer stock;

    @NotNull(message = "{product.active.notnull}")
    private Boolean active;

    // === Getters & Setters ===

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
}
