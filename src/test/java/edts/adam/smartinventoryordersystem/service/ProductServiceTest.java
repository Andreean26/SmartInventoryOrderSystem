package edts.adam.smartinventoryordersystem.service;

import edts.adam.smartinventoryordersystem.dto.product.request.CreateProductRequest;
import edts.adam.smartinventoryordersystem.dto.product.request.UpdateProductRequest;
import edts.adam.smartinventoryordersystem.dto.product.response.ProductResponse;
import edts.adam.smartinventoryordersystem.enums.OrderStatus;
import edts.adam.smartinventoryordersystem.enums.ProductCategory;
import edts.adam.smartinventoryordersystem.exception.BadRequestException;
import edts.adam.smartinventoryordersystem.exception.DuplicateResourceException;
import edts.adam.smartinventoryordersystem.exception.ResourceNotFoundException;
import edts.adam.smartinventoryordersystem.model.Product;
import edts.adam.smartinventoryordersystem.repository.OrderRepository;
import edts.adam.smartinventoryordersystem.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product();
        sampleProduct.setId(1L);
        sampleProduct.setName("Nasi Goreng");
        sampleProduct.setCategory(ProductCategory.FOOD);
        sampleProduct.setPrice(new BigDecimal("25000"));
        sampleProduct.setStock(100);
        sampleProduct.setActive(true);
        sampleProduct.setCreatedAt(Instant.now());
        sampleProduct.setUpdatedAt(Instant.now());
    }

    // =========================================================================
    // createProduct
    // =========================================================================

    @Nested
    @DisplayName("createProduct()")
    class CreateProduct {

        private CreateProductRequest createRequest(String name, ProductCategory category,
                BigDecimal price, Integer stock) {
            CreateProductRequest request = new CreateProductRequest();
            request.setName(name);
            request.setCategory(category);
            request.setPrice(price);
            request.setStock(stock);
            return request;
        }

        @Test
        @DisplayName("Should create product successfully")
        void shouldCreateProduct() {
            CreateProductRequest request = createRequest("Nasi Goreng", ProductCategory.FOOD,
                    new BigDecimal("25000"), 100);

            when(productRepository.existsByName("Nasi Goreng")).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

            ProductResponse response = productService.createProduct(request);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("Nasi Goreng");
            assertThat(response.getCategory()).isEqualTo(ProductCategory.FOOD);
            assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("25000"));
            assertThat(response.getStock()).isEqualTo(100);
            assertThat(response.getActive()).isTrue();

            verify(productRepository).existsByName("Nasi Goreng");
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when name already exists")
        void shouldThrowDuplicateWhenNameExists() {
            CreateProductRequest request = createRequest("Nasi Goreng", ProductCategory.FOOD,
                    new BigDecimal("25000"), 100);

            when(productRepository.existsByName("Nasi Goreng")).thenReturn(true);

            assertThatThrownBy(() -> productService.createProduct(request))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when FOOD price exceeds 1.000.000")
        void shouldThrowWhenFoodPriceExceedsLimit() {
            CreateProductRequest request = createRequest("Premium Food", ProductCategory.FOOD,
                    new BigDecimal("1500000"), 10);

            when(productRepository.existsByName("Premium Food")).thenReturn(false);

            assertThatThrownBy(() -> productService.createProduct(request))
                    .isInstanceOf(BadRequestException.class);

            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should allow FOOD product at exactly max price (1.000.000)")
        void shouldAllowFoodAtExactMaxPrice() {
            CreateProductRequest request = createRequest("Max Food", ProductCategory.FOOD,
                    new BigDecimal("1000000"), 5);

            Product saved = new Product();
            saved.setId(2L);
            saved.setName("Max Food");
            saved.setCategory(ProductCategory.FOOD);
            saved.setPrice(new BigDecimal("1000000"));
            saved.setStock(5);
            saved.setActive(true);
            saved.setCreatedAt(Instant.now());
            saved.setUpdatedAt(Instant.now());

            when(productRepository.existsByName("Max Food")).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(saved);

            ProductResponse response = productService.createProduct(request);

            assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("1000000"));
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should allow non-FOOD product with price exceeding 1.000.000")
        void shouldAllowNonFoodHighPrice() {
            CreateProductRequest request = createRequest("Laptop", ProductCategory.ELECTRONICS,
                    new BigDecimal("15000000"), 10);

            Product saved = new Product();
            saved.setId(3L);
            saved.setName("Laptop");
            saved.setCategory(ProductCategory.ELECTRONICS);
            saved.setPrice(new BigDecimal("15000000"));
            saved.setStock(10);
            saved.setActive(true);
            saved.setCreatedAt(Instant.now());
            saved.setUpdatedAt(Instant.now());

            when(productRepository.existsByName("Laptop")).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(saved);

            ProductResponse response = productService.createProduct(request);

            assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("15000000"));
        }
    }

    // =========================================================================
    // updateProduct
    // =========================================================================

    @Nested
    @DisplayName("updateProduct()")
    class UpdateProduct {

        private UpdateProductRequest createUpdateRequest(String name, ProductCategory category,
                BigDecimal price, Integer stock, Boolean active) {
            UpdateProductRequest request = new UpdateProductRequest();
            request.setName(name);
            request.setCategory(category);
            request.setPrice(price);
            request.setStock(stock);
            request.setActive(active);
            return request;
        }

        @Test
        @DisplayName("Should update product successfully")
        void shouldUpdateProduct() {
            UpdateProductRequest request = createUpdateRequest("Nasi Goreng Updated",
                    ProductCategory.FOOD, new BigDecimal("30000"), 100, true);

            Product updated = new Product();
            updated.setId(1L);
            updated.setName("Nasi Goreng Updated");
            updated.setCategory(ProductCategory.FOOD);
            updated.setPrice(new BigDecimal("30000"));
            updated.setStock(100);
            updated.setActive(true);
            updated.setCreatedAt(Instant.now());
            updated.setUpdatedAt(Instant.now());

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.existsByNameAndIdNot("Nasi Goreng Updated", 1L)).thenReturn(false);
            when(orderRepository.existsByOrderItems_Product_IdAndStatus(1L, OrderStatus.PAID)).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(updated);

            ProductResponse response = productService.updateProduct(1L, request);

            assertThat(response.getName()).isEqualTo("Nasi Goreng Updated");
            assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("30000"));
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product not found")
        void shouldThrowWhenProductNotFound() {
            UpdateProductRequest request = createUpdateRequest("Any", ProductCategory.FOOD,
                    new BigDecimal("10000"), 50, true);

            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.updateProduct(999L, request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when new name conflicts")
        void shouldThrowWhenNameConflictsOnUpdate() {
            UpdateProductRequest request = createUpdateRequest("Existing Name",
                    ProductCategory.FOOD, new BigDecimal("25000"), 100, true);

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.existsByNameAndIdNot("Existing Name", 1L)).thenReturn(true);

            assertThatThrownBy(() -> productService.updateProduct(1L, request))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when FOOD price exceeds limit on update")
        void shouldThrowWhenFoodPriceExceedsOnUpdate() {
            UpdateProductRequest request = createUpdateRequest("Nasi Goreng",
                    ProductCategory.FOOD, new BigDecimal("2000000"), 100, true);

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.existsByNameAndIdNot("Nasi Goreng", 1L)).thenReturn(false);

            assertThatThrownBy(() -> productService.updateProduct(1L, request))
                    .isInstanceOf(BadRequestException.class);

            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when price changed and has completed orders")
        void shouldThrowWhenPriceChangedWithCompletedOrders() {
            UpdateProductRequest request = createUpdateRequest("Nasi Goreng",
                    ProductCategory.FOOD, new BigDecimal("30000"), 100, true);

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.existsByNameAndIdNot("Nasi Goreng", 1L)).thenReturn(false);
            when(orderRepository.existsByOrderItems_Product_IdAndStatus(1L, OrderStatus.PAID)).thenReturn(true);

            assertThatThrownBy(() -> productService.updateProduct(1L, request))
                    .isInstanceOf(BadRequestException.class);

            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should allow price change when no completed orders")
        void shouldAllowPriceChangeWithNoCompletedOrders() {
            UpdateProductRequest request = createUpdateRequest("Nasi Goreng",
                    ProductCategory.FOOD, new BigDecimal("30000"), 100, true);

            Product updated = new Product();
            updated.setId(1L);
            updated.setName("Nasi Goreng");
            updated.setCategory(ProductCategory.FOOD);
            updated.setPrice(new BigDecimal("30000"));
            updated.setStock(100);
            updated.setActive(true);
            updated.setCreatedAt(Instant.now());
            updated.setUpdatedAt(Instant.now());

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.existsByNameAndIdNot("Nasi Goreng", 1L)).thenReturn(false);
            when(orderRepository.existsByOrderItems_Product_IdAndStatus(1L, OrderStatus.PAID)).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(updated);

            ProductResponse response = productService.updateProduct(1L, request);

            assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("30000"));
        }

        @Test
        @DisplayName("Should throw BadRequestException when deactivating product with pending orders")
        void shouldThrowWhenDeactivatingWithPendingOrders() {
            UpdateProductRequest request = createUpdateRequest("Nasi Goreng",
                    ProductCategory.FOOD, new BigDecimal("25000"), 100, false);

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.existsByNameAndIdNot("Nasi Goreng", 1L)).thenReturn(false);
            when(orderRepository.existsByOrderItems_Product_IdAndStatus(1L, OrderStatus.CREATED)).thenReturn(true);

            assertThatThrownBy(() -> productService.updateProduct(1L, request))
                    .isInstanceOf(BadRequestException.class);

            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should allow deactivation when no pending orders")
        void shouldAllowDeactivationWithNoPendingOrders() {
            UpdateProductRequest request = createUpdateRequest("Nasi Goreng",
                    ProductCategory.FOOD, new BigDecimal("25000"), 100, false);

            Product updated = new Product();
            updated.setId(1L);
            updated.setName("Nasi Goreng");
            updated.setCategory(ProductCategory.FOOD);
            updated.setPrice(new BigDecimal("25000"));
            updated.setStock(100);
            updated.setActive(false);
            updated.setCreatedAt(Instant.now());
            updated.setUpdatedAt(Instant.now());

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.existsByNameAndIdNot("Nasi Goreng", 1L)).thenReturn(false);
            when(orderRepository.existsByOrderItems_Product_IdAndStatus(1L, OrderStatus.CREATED)).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(updated);

            ProductResponse response = productService.updateProduct(1L, request);

            assertThat(response.getActive()).isFalse();
        }

        @Test
        @DisplayName("Should skip price-change check when price is unchanged")
        void shouldSkipPriceCheckWhenPriceUnchanged() {
            UpdateProductRequest request = createUpdateRequest("Nasi Goreng Updated",
                    ProductCategory.FOOD, new BigDecimal("25000"), 100, true);

            Product updated = new Product();
            updated.setId(1L);
            updated.setName("Nasi Goreng Updated");
            updated.setCategory(ProductCategory.FOOD);
            updated.setPrice(new BigDecimal("25000"));
            updated.setStock(100);
            updated.setActive(true);
            updated.setCreatedAt(Instant.now());
            updated.setUpdatedAt(Instant.now());

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.existsByNameAndIdNot("Nasi Goreng Updated", 1L)).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(updated);

            ProductResponse response = productService.updateProduct(1L, request);

            // price check for completed orders should NOT be called
            verify(orderRepository, never()).existsByOrderItems_Product_IdAndStatus(eq(1L), eq(OrderStatus.PAID));
            assertThat(response.getName()).isEqualTo("Nasi Goreng Updated");
        }
    }

    // =========================================================================
    // getAllProducts
    // =========================================================================

    @Nested
    @DisplayName("getAllProducts()")
    class GetAllProducts {

        @Test
        @DisplayName("Should return paginated products")
        void shouldReturnPaginatedProducts() {
            Pageable pageable = PageRequest.of(0, 10);

            Product product2 = new Product();
            product2.setId(2L);
            product2.setName("Mie Goreng");
            product2.setCategory(ProductCategory.FOOD);
            product2.setPrice(new BigDecimal("20000"));
            product2.setStock(50);
            product2.setActive(true);
            product2.setCreatedAt(Instant.now());
            product2.setUpdatedAt(Instant.now());

            Page<Product> productPage = new PageImpl<>(
                    List.of(sampleProduct, product2), pageable, 2);

            when(productRepository.findByActiveTrue(pageable)).thenReturn(productPage);

            Page<ProductResponse> result = productService.getAllProducts(pageable);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Nasi Goreng");
            assertThat(result.getContent().get(1).getName()).isEqualTo("Mie Goreng");
            verify(productRepository).findByActiveTrue(pageable);
        }

        @Test
        @DisplayName("Should return empty page when no products")
        void shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(productRepository.findByActiveTrue(pageable)).thenReturn(emptyPage);

            Page<ProductResponse> result = productService.getAllProducts(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    // =========================================================================
    // getProductById
    // =========================================================================

    @Nested
    @DisplayName("getProductById()")
    class GetProductById {

        @Test
        @DisplayName("Should return product when found")
        void shouldReturnProductWhenFound() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

            ProductResponse response = productService.getProductById(1L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("Nasi Goreng");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product not found")
        void shouldThrowWhenProductNotFound() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =========================================================================
    // deleteProduct (soft delete)
    // =========================================================================

    @Nested
    @DisplayName("deleteProduct()")
    class DeleteProduct {

        @Test
        @DisplayName("Should soft-delete product when stock is zero")
        void shouldSoftDeleteWhenStockIsZero() {
            sampleProduct.setStock(0);

            Product deleted = new Product();
            deleted.setId(1L);
            deleted.setName("Nasi Goreng");
            deleted.setCategory(ProductCategory.FOOD);
            deleted.setPrice(new BigDecimal("25000"));
            deleted.setStock(0);
            deleted.setActive(false);
            deleted.setCreatedAt(Instant.now());
            deleted.setUpdatedAt(Instant.now());

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(any(Product.class))).thenReturn(deleted);

            ProductResponse response = productService.deleteProduct(1L);

            assertThat(response.getActive()).isFalse();
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when stock is not zero")
        void shouldThrowWhenStockNotZero() {
            sampleProduct.setStock(50);

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

            assertThatThrownBy(() -> productService.deleteProduct(1L))
                    .isInstanceOf(BadRequestException.class);

            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product not found")
        void shouldThrowWhenProductNotFoundForDelete() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.deleteProduct(999L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(productRepository, never()).save(any(Product.class));
        }
    }
}
