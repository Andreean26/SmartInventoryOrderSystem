package edts.adam.smartinventoryordersystem.service;

import edts.adam.smartinventoryordersystem.dto.product.request.CreateProductRequest;
import edts.adam.smartinventoryordersystem.dto.product.request.UpdateProductRequest;
import edts.adam.smartinventoryordersystem.dto.product.response.ProductResponse;
import edts.adam.smartinventoryordersystem.model.Product;
import edts.adam.smartinventoryordersystem.enums.OrderStatus;
import edts.adam.smartinventoryordersystem.enums.ProductCategory;
import edts.adam.smartinventoryordersystem.exception.BadRequestException;
import edts.adam.smartinventoryordersystem.exception.DuplicateResourceException;
import edts.adam.smartinventoryordersystem.exception.ResourceNotFoundException;
import edts.adam.smartinventoryordersystem.repository.OrderRepository;
import edts.adam.smartinventoryordersystem.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service layer for product management.
 * Handles creation, updates, retrieval, and soft-deletion with business rule
 * enforcement
 * such as name uniqueness, FOOD price cap, and order-aware restrictions.
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private static final BigDecimal FOOD_MAX_PRICE = new BigDecimal("1000000");

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public ProductService(ProductRepository productRepository,
            OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        log.debug("Checking for duplicate product name: {}", request.getName());
        if (productRepository.existsByName(request.getName())) {
            log.warn("Duplicate product name detected: {}", request.getName());
            throw new DuplicateResourceException("product.name.duplicate", request.getName());
        }

        if (request.getCategory() == ProductCategory.FOOD
                && request.getPrice().compareTo(FOOD_MAX_PRICE) > 0) {
            log.warn("FOOD product price {} exceeds max limit {}", request.getPrice(), FOOD_MAX_PRICE);
            throw new BadRequestException("product.food.price.exceeded");
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setActive(true);

        Product saved = productRepository.save(product);
        log.info("Product created: id={}, name={}, category={}, stock={}",
                saved.getId(), saved.getName(), saved.getCategory(), saved.getStock());
        return ProductResponse.fromEntity(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found: id={}", id);
                    return new ResourceNotFoundException("product.not.found", id);
                });

        if (productRepository.existsByNameAndIdNot(request.getName(), id)) {
            log.warn("Duplicate product name on update: {}", request.getName());
            throw new DuplicateResourceException("product.name.duplicate", request.getName());
        }

        if (request.getCategory() == ProductCategory.FOOD
                && request.getPrice().compareTo(FOOD_MAX_PRICE) > 0) {
            log.warn("FOOD product price {} exceeds max limit {} on update", request.getPrice(), FOOD_MAX_PRICE);
            throw new BadRequestException("product.food.price.exceeded");
        }

        if (request.getPrice().compareTo(product.getPrice()) != 0) {
            log.debug("Price change detected for product id={}: {} -> {}", id, product.getPrice(), request.getPrice());
            boolean hasCompletedOrders = orderRepository
                    .existsByOrderItems_Product_IdAndStatus(id, OrderStatus.PAID);
            if (hasCompletedOrders) {
                log.warn("Cannot update price for product id={} — has completed orders", id);
                throw new BadRequestException("product.price.update.completed.orders");
            }
        }
//fix
        if (!request.getActive() && product.getActive()) {
            log.debug("Deactivation requested for product id={}", id);
            boolean hasPendingOrders = orderRepository
                    .existsByOrderItems_Product_IdAndStatus(id, OrderStatus.CREATED);
            if (hasPendingOrders) {
                log.warn("Cannot deactivate product id={} — has pending orders", id);
                throw new BadRequestException("product.deactivate.pending.orders");
            }
        }

        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setActive(request.getActive());

        Product saved = productRepository.save(product);
        log.info("Product updated: id={}, name={}, price={}, active={}",
                saved.getId(), saved.getName(), saved.getPrice(), saved.getActive());
        return ProductResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        log.debug("Fetching products — page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return productRepository.findByActiveTrue(pageable)
                .map(ProductResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found: id={}", id);
                    return new ResourceNotFoundException("product.not.found", id);
                });
        log.debug("Product retrieved: id={}, name={}", product.getId(), product.getName());
        return ProductResponse.fromEntity(product);
    }

    @Transactional
    public ProductResponse deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found for deletion: id={}", id);
                    return new ResourceNotFoundException("product.not.found", id);
                });

        if (product.getStock() > 0) {
            log.warn("Cannot delete product id={} — stock is {}", id, product.getStock());
            throw new BadRequestException("product.delete.stock.not.zero", product.getStock());
        }

        product.setActive(false);
        Product saved = productRepository.save(product);
        log.info("Product soft-deleted: id={}", id);
        return ProductResponse.fromEntity(saved);
    }
}
