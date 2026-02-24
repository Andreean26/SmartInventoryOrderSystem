package edts.adam.smartinventoryordersystem.dto.product.request;

import edts.adam.smartinventoryordersystem.enums.ProductCategory;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/** Request DTO for creating a new product. */
public class CreateProductRequest {

    @NotBlank(message = "{product.name.notblank}")
    private String name;

    @NotNull(message = "{product.category.notnull}")
    private ProductCategory category;

    @NotNull(message = "{product.price.notnull}")
    @DecimalMin(value = "0.01", message = "{product.price.min}")
    private BigDecimal price;

    @NotNull(message = "{product.stock.notnull}")
    @Min(value = 0, message = "{product.stock.min}")
    @Max(value = 1000000, message = "{product.stock.max}")
    private Integer stock;

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

    @Override
    public String toString() {
        return "CreateProductRequest{" +
                "name='" + name + '\'' +
                ", category=" + category +
                ", price=" + price +
                ", stock=" + stock +
                '}';
    }
}
