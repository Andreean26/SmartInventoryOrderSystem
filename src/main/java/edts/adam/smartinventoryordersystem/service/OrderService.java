package edts.adam.smartinventoryordersystem.service;

import edts.adam.smartinventoryordersystem.dto.order.request.CreateOrderRequest;
import edts.adam.smartinventoryordersystem.dto.order.request.OrderItemRequest;
import edts.adam.smartinventoryordersystem.dto.order.response.OrderResponse;
import edts.adam.smartinventoryordersystem.model.Customer;
import edts.adam.smartinventoryordersystem.model.Order;
import edts.adam.smartinventoryordersystem.model.OrderItem;
import edts.adam.smartinventoryordersystem.model.Product;
import edts.adam.smartinventoryordersystem.enums.OrderStatus;
import edts.adam.smartinventoryordersystem.exception.BadRequestException;
import edts.adam.smartinventoryordersystem.exception.InsufficientStockException;
import edts.adam.smartinventoryordersystem.exception.InvalidOrderStateException;
import edts.adam.smartinventoryordersystem.exception.ResourceNotFoundException;
import edts.adam.smartinventoryordersystem.repository.CustomerRepository;
import edts.adam.smartinventoryordersystem.repository.OrderRepository;
import edts.adam.smartinventoryordersystem.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service layer for order lifecycle management.
 * Handles order creation (with stock validation and discount calculation),
 * payment processing, cancellation with stock restoration, and retrieval.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final BigDecimal EXTRA_DISCOUNT_THRESHOLD = new BigDecimal("5000000");
    private static final BigDecimal EXTRA_DISCOUNT_RATE = new BigDecimal("0.05");
    private static final BigDecimal MAX_DISCOUNT_RATE = new BigDecimal("0.30");

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    // Public API Methods

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Customer customer = findCustomerOrThrow(request.getCustomerId());
        Order order = new Order(customer);

        Map<Long, Integer> mergedItems = mergeOrderItems(request.getItems());
        BigDecimal totalAmount = processOrderItems(order, mergedItems);
        applyDiscount(order, customer, totalAmount);

        order.setStatus(OrderStatus.CREATED);
        Order saved = orderRepository.save(order);

        log.info("Order created: id={}, customerId={}, items={}, total={}, discount={}, final={}",
                saved.getId(), customer.getId(), saved.getOrderItems().size(),
                order.getTotalAmount(), order.getDiscountAmount(), order.getFinalAmount());
        return OrderResponse.fromEntity(saved);
    }

    @Transactional
    public OrderResponse payOrder(Long id) {
        Order order = findOrderOrThrow(id);
        validateStatusTransition(order, OrderStatus.PAID, "order.pay.invalid.status");

        order.setStatus(OrderStatus.PAID);

        Customer customer = order.getCustomer();
        customer.addSpent(order.getFinalAmount());
        customerRepository.save(customer);
        log.debug("Customer id={} totalSpent updated to {}", customer.getId(), customer.getTotalSpent());

        Order saved = orderRepository.save(order);
        log.info("Order paid: id={}, finalAmount={}, customerId={}", id, order.getFinalAmount(), customer.getId());
        return OrderResponse.fromEntity(saved);
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = findOrderOrThrow(id);
        validateStatusTransition(order, OrderStatus.CANCELLED, "order.cancel.invalid.status");

        restoreStock(order.getOrderItems());
        order.setStatus(OrderStatus.CANCELLED);

        Order saved = orderRepository.save(order);
        log.info("Order cancelled: id={}, {} item(s) stock restored", id, order.getOrderItems().size());
        return OrderResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = findOrderOrThrow(id);
        log.debug("Order retrieved: id={}, status={}", order.getId(), order.getStatus());
        return OrderResponse.fromEntity(order);
    }

    // Entity Lookup Helpers

    private Customer findCustomerOrThrow(Long customerId) {
        log.debug("Looking up customer id={}", customerId);
        return customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    log.warn("Customer not found: id={}", customerId);
                    return new ResourceNotFoundException("customer.not.found", customerId);
                });
    }

    private Order findOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found: id={}", orderId);
                    return new ResourceNotFoundException("order.not.found", orderId);
                });
    }

    private Product findProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product not found: id={}", productId);
                    return new ResourceNotFoundException("product.not.found", productId);
                });
    }


    // Order Item Processing

    /**
     * Merges duplicate productIds by summing their quantities.
     * Preserves insertion order via LinkedHashMap.
     */
    private Map<Long, Integer> mergeOrderItems(List<OrderItemRequest> items) {
        Map<Long, Integer> merged = new LinkedHashMap<>();
        for (OrderItemRequest item : items) {
            merged.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }
        log.debug("Merged {} item(s) into {} unique product(s)", items.size(), merged.size());
        return merged;
    }

    /**
     * Validates, deducts stock, and creates OrderItems for each merged entry.
     *
     * @return the total amount before discount
     */
    private BigDecimal processOrderItems(Order order, Map<Long, Integer> mergedItems) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : mergedItems.entrySet()) {
            OrderItem orderItem = createValidatedOrderItem(entry.getKey(), entry.getValue());
            order.addOrderItem(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }

        return totalAmount;
    }

    /**
     * Validates a single product (exists, active, sufficient stock),
     * deducts stock, and returns a new OrderItem with a price snapshot.
     */
    private OrderItem createValidatedOrderItem(Long productId, int quantity) {
        Product product = findProductOrThrow(productId);

        if (!product.getActive()) {
            log.warn("Attempted to order inactive product: id={}, name={}", product.getId(), product.getName());
            throw new BadRequestException("order.product.not.active", product.getName());
        }

        if (product.getStock() < quantity) {
            log.warn("Insufficient stock for product id={}: available={}, requested={}",
                    product.getId(), product.getStock(), quantity);
            throw new InsufficientStockException("order.insufficient.stock",
                    product.getName(), product.getStock(), quantity);
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
        log.debug("Stock reduced for product id={}: new stock={}", product.getId(), product.getStock());

        return new OrderItem(product, quantity, product.getPrice());
    }


    // Discount Calculation

    /**
     * Calculates discount based on membership level and order total,
     * then sets totalAmount, discountAmount, and finalAmount on the order.
     */
    private void applyDiscount(Order order, Customer customer, BigDecimal totalAmount) {
        BigDecimal discountRate = customer.getMembershipLevel().getDiscountPercentage();
        log.debug("Base discount rate for customer id={} ({}): {}",
                customer.getId(), customer.getMembershipLevel(), discountRate);

        if (totalAmount.compareTo(EXTRA_DISCOUNT_THRESHOLD) > 0) {
            discountRate = discountRate.add(EXTRA_DISCOUNT_RATE);
            log.debug("Extra discount applied — total {} exceeds threshold {}", totalAmount, EXTRA_DISCOUNT_THRESHOLD);
        }

        if (discountRate.compareTo(MAX_DISCOUNT_RATE) > 0) {
            discountRate = MAX_DISCOUNT_RATE;
            log.debug("Discount rate capped at max: {}", MAX_DISCOUNT_RATE);
        }

        BigDecimal discountAmount = totalAmount.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalAmount = totalAmount.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);

        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setFinalAmount(finalAmount);
    }

    // Order Status & Stock Helpers

    /**
     * Validates that the order can transition to the target status.
     *
     * @throws InvalidOrderStateException if the transition is not allowed
     */
    private void validateStatusTransition(Order order, OrderStatus target, String errorKey) {
        if (!order.getStatus().canTransitionTo(target)) {
            log.warn("Invalid status transition — order id={} status={} → {}",
                    order.getId(), order.getStatus(), target);
            throw new InvalidOrderStateException(errorKey, order.getStatus());
        }
    }

    /**
     * Restores stock for all items in a cancelled order.
     */
    private void restoreStock(List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            Product product = item.getProduct();
            int restoredQty = item.getQuantity();
            product.setStock(product.getStock() + restoredQty);
            productRepository.save(product);
            log.debug("Stock restored for product id={}: +{} (new stock={})",
                    product.getId(), restoredQty, product.getStock());
        }
    }
}
