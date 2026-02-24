package edts.adam.smartinventoryordersystem.controller;

import edts.adam.smartinventoryordersystem.dto.ApiResponse;
import edts.adam.smartinventoryordersystem.dto.product.request.CreateProductRequest;
import edts.adam.smartinventoryordersystem.dto.product.request.UpdateProductRequest;
import edts.adam.smartinventoryordersystem.dto.product.response.ProductResponse;
import edts.adam.smartinventoryordersystem.service.ProductService;
import edts.adam.smartinventoryordersystem.util.MessageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing CRUD endpoints for product management.
 * All responses are wrapped in {@link ApiResponse}.
 */
@RestController
@RequestMapping("/products")
@Tag(name = "Product", description = "Product management endpoints")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;
    private final MessageUtil messageUtil;

    public ProductController(ProductService productService, MessageUtil messageUtil) {
        this.productService = productService;
        this.messageUtil = messageUtil;
    }

    @Operation(summary = "Create a new product", description = "Creates a product with name uniqueness check and FOOD price limit validation")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        log.info("POST /products — Creating product with name: {}", request.getName());
        ProductResponse response = productService.createProduct(request);
        log.info("Product created successfully with id: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(messageUtil.get("product.created.success"), response));
    }

    @Operation(summary = "Update an existing product", description = "Updates product details including stock. Price change blocked if completed orders exist; deactivation blocked if pending orders exist")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        log.info("PUT /products/{} — Updating product", id);
        ProductResponse response = productService.updateProduct(id, request);
        log.info("Product id: {} updated successfully", id);
        return ResponseEntity.ok(ApiResponse.success(messageUtil.get("product.updated.success"), response));
    }

    @Operation(summary = "Get all products", description = "Returns a paginated list of products")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        log.info("GET /products — Fetching products page: {}, size: {}", pageable.getPageNumber(),
                pageable.getPageSize());
        Page<ProductResponse> response = productService.getAllProducts(pageable);
        log.info("Returned {} products (page {} of {})", response.getNumberOfElements(),
                pageable.getPageNumber(), response.getTotalPages());
        return ResponseEntity.ok(ApiResponse.success(messageUtil.get("api.response.success"), response));
    }

    @Operation(summary = "Get product by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        log.info("GET /products/{} — Fetching product", id);
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(messageUtil.get("api.response.success"), response));
    }

    @Operation(summary = "Soft-delete a product", description = "Sets product active=false. Only allowed if stock is zero")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> deleteProduct(@PathVariable Long id) {
        log.info("DELETE /products/{} — Soft-deleting product", id);
        ProductResponse response = productService.deleteProduct(id);
        log.info("Product id: {} soft-deleted successfully", id);
        return ResponseEntity.ok(ApiResponse.success(messageUtil.get("product.deleted.success"), response));
    }
}
