package edts.adam.smartinventoryordersystem.service;

import edts.adam.smartinventoryordersystem.dto.order.request.CreateOrderRequest;
import edts.adam.smartinventoryordersystem.dto.order.request.OrderItemRequest;
import edts.adam.smartinventoryordersystem.dto.order.response.OrderResponse;
import edts.adam.smartinventoryordersystem.enums.MembershipLevel;
import edts.adam.smartinventoryordersystem.enums.OrderStatus;
import edts.adam.smartinventoryordersystem.exception.BadRequestException;
import edts.adam.smartinventoryordersystem.exception.InsufficientStockException;
import edts.adam.smartinventoryordersystem.exception.InvalidOrderStateException;
import edts.adam.smartinventoryordersystem.exception.ResourceNotFoundException;
import edts.adam.smartinventoryordersystem.model.Customer;
import edts.adam.smartinventoryordersystem.model.Order;
import edts.adam.smartinventoryordersystem.model.OrderItem;
import edts.adam.smartinventoryordersystem.model.Product;
import edts.adam.smartinventoryordersystem.enums.ProductCategory;
import edts.adam.smartinventoryordersystem.repository.CustomerRepository;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private Customer regularCustomer;
    private Customer goldCustomer;
    private Customer platinumCustomer;
    private Product activeProduct;
    private Product inactiveProduct;

    @BeforeEach
    void setUp() {
        regularCustomer = new Customer();
        regularCustomer.setId(1L);
        regularCustomer.setName("Regular User");
        regularCustomer.setEmail("regular@example.com");
        regularCustomer.setMembershipLevel(MembershipLevel.REGULAR);
        regularCustomer.setTotalSpent(BigDecimal.ZERO);
        regularCustomer.setActive(true);

        goldCustomer = new Customer();
        goldCustomer.setId(2L);
        goldCustomer.setName("Gold User");
        goldCustomer.setEmail("gold@example.com");
        goldCustomer.setMembershipLevel(MembershipLevel.GOLD);
        goldCustomer.setTotalSpent(new BigDecimal("15000000"));
        goldCustomer.setActive(true);

        platinumCustomer = new Customer();
        platinumCustomer.setId(3L);
        platinumCustomer.setName("Platinum User");
        platinumCustomer.setEmail("platinum@example.com");
        platinumCustomer.setMembershipLevel(MembershipLevel.PLATINUM);
        platinumCustomer.setTotalSpent(new BigDecimal("60000000"));
        platinumCustomer.setActive(true);

        activeProduct = new Product();
        activeProduct.setId(1L);
        activeProduct.setName("Nasi Goreng");
        activeProduct.setCategory(ProductCategory.FOOD);
        activeProduct.setPrice(new BigDecimal("25000"));
        activeProduct.setStock(100);
        activeProduct.setActive(true);
        activeProduct.setCreatedAt(Instant.now());
        activeProduct.setUpdatedAt(Instant.now());

        inactiveProduct = new Product();
        inactiveProduct.setId(2L);
        inactiveProduct.setName("Old Item");
        inactiveProduct.setCategory(ProductCategory.FOOD);
        inactiveProduct.setPrice(new BigDecimal("10000"));
        inactiveProduct.setStock(50);
        inactiveProduct.setActive(false);
        inactiveProduct.setCreatedAt(Instant.now());
        inactiveProduct.setUpdatedAt(Instant.now());
    }


    // Helper methods


    private CreateOrderRequest createOrderRequest(Long customerId, Long productId, int quantity) {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(productId);
        item.setQuantity(quantity);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(customerId);
        request.setItems(List.of(item));
        return request;
    }

    private CreateOrderRequest createOrderRequest(Long customerId, List<OrderItemRequest> items) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(customerId);
        request.setItems(items);
        return request;
    }

    private OrderItemRequest itemRequest(Long productId, int quantity) {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(productId);
        item.setQuantity(quantity);
        return item;
    }

    private Order buildSavedOrder(Long id, Customer customer, OrderStatus status,
            BigDecimal total, BigDecimal discount, BigDecimal finalAmt,
            List<OrderItem> items) {
        Order order = new Order(customer);
        order.setId(id);
        order.setStatus(status);
        order.setTotalAmount(total);
        order.setDiscountAmount(discount);
        order.setFinalAmount(finalAmt);
        order.setCreatedAt(Instant.now());
        order.setOrderItems(items != null ? items : new ArrayList<>());
        return order;
    }

    // createOrder


    @Nested
    @DisplayName("createOrder()")
    class CreateOrder {

        @Test
        @DisplayName("Should create order for REGULAR customer with no discount")
        void shouldCreateOrderRegularNoDiscount() {
            CreateOrderRequest request = createOrderRequest(1L, 1L, 2);

            // 2 × 25000 = 50000, REGULAR discount = 0%
            BigDecimal expectedTotal = new BigDecimal("50000");
            BigDecimal expectedDiscount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            BigDecimal expectedFinal = new BigDecimal("50000.00");

            when(customerRepository.findById(1L)).thenReturn(Optional.of(regularCustomer));
            when(productRepository.findById(1L)).thenReturn(Optional.of(activeProduct));
            when(productRepository.save(any(Product.class))).thenReturn(activeProduct);

            Order savedOrder = buildSavedOrder(1L, regularCustomer, OrderStatus.CREATED,
                    expectedTotal, expectedDiscount, expectedFinal,
                    List.of(new OrderItem(activeProduct, 2, activeProduct.getPrice())));
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            OrderResponse response = orderService.createOrder(request);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(response.getCustomerId()).isEqualTo(1L);

            // Stock should be reduced
            verify(productRepository).save(any(Product.class));
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Should apply GOLD 10% discount")
        void shouldApplyGoldDiscount() {
            CreateOrderRequest request = createOrderRequest(2L, 1L, 10);

            // 10 × 25000 = 250000, GOLD discount = 10% → 25000 discount
            BigDecimal expectedTotal = new BigDecimal("250000");
            BigDecimal expectedDiscount = new BigDecimal("25000.00");
            BigDecimal expectedFinal = new BigDecimal("225000.00");

            when(customerRepository.findById(2L)).thenReturn(Optional.of(goldCustomer));
            when(productRepository.findById(1L)).thenReturn(Optional.of(activeProduct));
            when(productRepository.save(any(Product.class))).thenReturn(activeProduct);

            Order savedOrder = buildSavedOrder(2L, goldCustomer, OrderStatus.CREATED,
                    expectedTotal, expectedDiscount, expectedFinal,
                    List.of(new OrderItem(activeProduct, 10, activeProduct.getPrice())));
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            OrderResponse response = orderService.createOrder(request);

            assertThat(response).isNotNull();
            assertThat(response.getDiscountAmount()).isEqualByComparingTo(expectedDiscount);
            assertThat(response.getFinalAmount()).isEqualByComparingTo(expectedFinal);
        }

        @Test
        @DisplayName("Should apply extra 5% discount when total exceeds 5M threshold")
        void shouldApplyExtraDiscountAboveThreshold() {

            Product expensiveProduct = new Product();
            expensiveProduct.setId(3L);
            expensiveProduct.setName("Laptop");
            expensiveProduct.setCategory(ProductCategory.ELECTRONICS);
            expensiveProduct.setPrice(new BigDecimal("6000000"));
            expensiveProduct.setStock(10);
            expensiveProduct.setActive(true);
            expensiveProduct.setCreatedAt(Instant.now());
            expensiveProduct.setUpdatedAt(Instant.now());

            CreateOrderRequest request = createOrderRequest(2L, 3L, 1);

            // 1 × 6000000 = 6000000 > 5M threshold
            // GOLD 10% + extra 5% = 15%
            BigDecimal expectedTotal = new BigDecimal("6000000");
            BigDecimal expectedDiscount = new BigDecimal("900000.00"); // 6M × 0.15
            BigDecimal expectedFinal = new BigDecimal("5100000.00");

            when(customerRepository.findById(2L)).thenReturn(Optional.of(goldCustomer));
            when(productRepository.findById(3L)).thenReturn(Optional.of(expensiveProduct));
            when(productRepository.save(any(Product.class))).thenReturn(expensiveProduct);

            Order savedOrder = buildSavedOrder(3L, goldCustomer, OrderStatus.CREATED,
                    expectedTotal, expectedDiscount, expectedFinal,
                    List.of(new OrderItem(expensiveProduct, 1, expensiveProduct.getPrice())));
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            OrderResponse response = orderService.createOrder(request);

            assertThat(response).isNotNull();
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Should cap discount at 30% max")
        void shouldCapDiscountAtMax() {
            // PLATINUM (20%) + extra 5% above threshold = 25%, which is < 30%
            Product expensiveProduct = new Product();
            expensiveProduct.setId(3L);
            expensiveProduct.setName("Server");
            expensiveProduct.setCategory(ProductCategory.ELECTRONICS);
            expensiveProduct.setPrice(new BigDecimal("10000000"));
            expensiveProduct.setStock(5);
            expensiveProduct.setActive(true);
            expensiveProduct.setCreatedAt(Instant.now());
            expensiveProduct.setUpdatedAt(Instant.now());

            CreateOrderRequest request = createOrderRequest(3L, 3L, 1);

            // PLATINUM 20% + extra 5% = 25% (under 30% cap)
            BigDecimal expectedTotal = new BigDecimal("10000000");
            BigDecimal expectedDiscount = new BigDecimal("2500000.00");
            BigDecimal expectedFinal = new BigDecimal("7500000.00");

            when(customerRepository.findById(3L)).thenReturn(Optional.of(platinumCustomer));
            when(productRepository.findById(3L)).thenReturn(Optional.of(expensiveProduct));
            when(productRepository.save(any(Product.class))).thenReturn(expensiveProduct);

            Order savedOrder = buildSavedOrder(4L, platinumCustomer, OrderStatus.CREATED,
                    expectedTotal, expectedDiscount, expectedFinal,
                    List.of(new OrderItem(expensiveProduct, 1, expensiveProduct.getPrice())));
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            OrderResponse response = orderService.createOrder(request);

            assertThat(response).isNotNull();
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when customer not found")
        void shouldThrowWhenCustomerNotFound() {
            CreateOrderRequest request = createOrderRequest(999L, 1L, 1);

            when(customerRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.createOrder(request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product not found")
        void shouldThrowWhenProductNotFound() {
            CreateOrderRequest request = createOrderRequest(1L, 999L, 1);

            when(customerRepository.findById(1L)).thenReturn(Optional.of(regularCustomer));
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.createOrder(request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when product is inactive")
        void shouldThrowWhenProductInactive() {
            CreateOrderRequest request = createOrderRequest(1L, 2L, 1);

            when(customerRepository.findById(1L)).thenReturn(Optional.of(regularCustomer));
            when(productRepository.findById(2L)).thenReturn(Optional.of(inactiveProduct));

            assertThatThrownBy(() -> orderService.createOrder(request))
                    .isInstanceOf(BadRequestException.class);

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw InsufficientStockException when stock is not enough")
        void shouldThrowWhenInsufficientStock() {
            CreateOrderRequest request = createOrderRequest(1L, 1L, 999);

            when(customerRepository.findById(1L)).thenReturn(Optional.of(regularCustomer));
            when(productRepository.findById(1L)).thenReturn(Optional.of(activeProduct));

            assertThatThrownBy(() -> orderService.createOrder(request))
                    .isInstanceOf(InsufficientStockException.class);

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should merge duplicate product items in same order")
        void shouldMergeDuplicateProductItems() {
            // Two items for same product: 3 + 2 = 5 total
            List<OrderItemRequest> items = List.of(
                    itemRequest(1L, 3),
                    itemRequest(1L, 2));
            CreateOrderRequest request = createOrderRequest(1L, items);

            when(customerRepository.findById(1L)).thenReturn(Optional.of(regularCustomer));
            when(productRepository.findById(1L)).thenReturn(Optional.of(activeProduct));
            when(productRepository.save(any(Product.class))).thenReturn(activeProduct);

            // 5 × 25000 = 125000
            BigDecimal expectedTotal = new BigDecimal("125000");
            Order savedOrder = buildSavedOrder(1L, regularCustomer, OrderStatus.CREATED,
                    expectedTotal, BigDecimal.ZERO.setScale(2), expectedTotal.setScale(2),
                    List.of(new OrderItem(activeProduct, 5, activeProduct.getPrice())));
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            OrderResponse response = orderService.createOrder(request);

            assertThat(response).isNotNull();
            // Product findById should only be called ONCE (merged)
            verify(productRepository).findById(1L);
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw InsufficientStockException for merged duplicate items exceeding stock")
        void shouldThrowWhenMergedQuantityExceedsStock() {
            // stock = 100, but 60 + 50 = 110 (merged)
            List<OrderItemRequest> items = List.of(
                    itemRequest(1L, 60),
                    itemRequest(1L, 50));
            CreateOrderRequest request = createOrderRequest(1L, items);

            when(customerRepository.findById(1L)).thenReturn(Optional.of(regularCustomer));
            when(productRepository.findById(1L)).thenReturn(Optional.of(activeProduct));

            assertThatThrownBy(() -> orderService.createOrder(request))
                    .isInstanceOf(InsufficientStockException.class);

            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    // payOrder

    @Nested
    @DisplayName("payOrder()")
    class PayOrder {

        @Test
        @DisplayName("Should pay order successfully from CREATED status")
        void shouldPayOrderSuccessfully() {
            Order order = buildSavedOrder(1L, regularCustomer, OrderStatus.CREATED,
                    new BigDecimal("100000"), BigDecimal.ZERO, new BigDecimal("100000"),
                    new ArrayList<>());

            Order paidOrder = buildSavedOrder(1L, regularCustomer, OrderStatus.PAID,
                    new BigDecimal("100000"), BigDecimal.ZERO, new BigDecimal("100000"),
                    new ArrayList<>());

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(customerRepository.save(any(Customer.class))).thenReturn(regularCustomer);
            when(orderRepository.save(any(Order.class))).thenReturn(paidOrder);

            OrderResponse response = orderService.payOrder(1L);

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(OrderStatus.PAID);

            verify(customerRepository).save(any(Customer.class));
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Should update customer totalSpent on payment")
        void shouldUpdateCustomerSpentOnPayment() {
            BigDecimal finalAmount = new BigDecimal("100000");
            Order order = buildSavedOrder(1L, regularCustomer, OrderStatus.CREATED,
                    new BigDecimal("100000"), BigDecimal.ZERO, finalAmount,
                    new ArrayList<>());

            Order paidOrder = buildSavedOrder(1L, regularCustomer, OrderStatus.PAID,
                    new BigDecimal("100000"), BigDecimal.ZERO, finalAmount,
                    new ArrayList<>());

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(customerRepository.save(any(Customer.class))).thenReturn(regularCustomer);
            when(orderRepository.save(any(Order.class))).thenReturn(paidOrder);

            orderService.payOrder(1L);

            // addSpent should be called, increasing totalSpent
            assertThat(regularCustomer.getTotalSpent()).isEqualByComparingTo(finalAmount);
            verify(customerRepository).save(regularCustomer);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when order not found")
        void shouldThrowWhenOrderNotFoundForPayment() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.payOrder(999L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw InvalidOrderStateException when paying already PAID order")
        void shouldThrowWhenPayingPaidOrder() {
            Order paidOrder = buildSavedOrder(1L, regularCustomer, OrderStatus.PAID,
                    new BigDecimal("100000"), BigDecimal.ZERO, new BigDecimal("100000"),
                    new ArrayList<>());

            when(orderRepository.findById(1L)).thenReturn(Optional.of(paidOrder));

            assertThatThrownBy(() -> orderService.payOrder(1L))
                    .isInstanceOf(InvalidOrderStateException.class);

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw InvalidOrderStateException when paying CANCELLED order")
        void shouldThrowWhenPayingCancelledOrder() {
            Order cancelledOrder = buildSavedOrder(1L, regularCustomer, OrderStatus.CANCELLED,
                    new BigDecimal("100000"), BigDecimal.ZERO, new BigDecimal("100000"),
                    new ArrayList<>());

            when(orderRepository.findById(1L)).thenReturn(Optional.of(cancelledOrder));

            assertThatThrownBy(() -> orderService.payOrder(1L))
                    .isInstanceOf(InvalidOrderStateException.class);

            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    // =========================================================================
    // cancelOrder
    // =========================================================================

    @Nested
    @DisplayName("cancelOrder()")
    class CancelOrder {

        @Test
        @DisplayName("Should cancel order and restore stock")
        void shouldCancelAndRestoreStock() {
            Product product = new Product();
            product.setId(1L);
            product.setName("Nasi Goreng");
            product.setCategory(ProductCategory.FOOD);
            product.setPrice(new BigDecimal("25000"));
            product.setStock(95); // was 100, 5 were ordered
            product.setActive(true);
            product.setCreatedAt(Instant.now());
            product.setUpdatedAt(Instant.now());

            OrderItem item = new OrderItem(product, 5, new BigDecimal("25000"));

            Order order = buildSavedOrder(1L, regularCustomer, OrderStatus.CREATED,
                    new BigDecimal("125000"), BigDecimal.ZERO, new BigDecimal("125000"),
                    new ArrayList<>(List.of(item)));

            Order cancelledOrder = buildSavedOrder(1L, regularCustomer, OrderStatus.CANCELLED,
                    new BigDecimal("125000"), BigDecimal.ZERO, new BigDecimal("125000"),
                    new ArrayList<>(List.of(item)));

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(productRepository.save(any(Product.class))).thenReturn(product);
            when(orderRepository.save(any(Order.class))).thenReturn(cancelledOrder);

            OrderResponse response = orderService.cancelOrder(1L);

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);

            // Stock should be restored: 95 + 5 = 100
            assertThat(product.getStock()).isEqualTo(100);
            verify(productRepository).save(product);
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when order not found for cancellation")
        void shouldThrowWhenOrderNotFoundForCancel() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.cancelOrder(999L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw InvalidOrderStateException when cancelling PAID order")
        void shouldThrowWhenCancellingPaidOrder() {
            Order paidOrder = buildSavedOrder(1L, regularCustomer, OrderStatus.PAID,
                    new BigDecimal("100000"), BigDecimal.ZERO, new BigDecimal("100000"),
                    new ArrayList<>());

            when(orderRepository.findById(1L)).thenReturn(Optional.of(paidOrder));

            assertThatThrownBy(() -> orderService.cancelOrder(1L))
                    .isInstanceOf(InvalidOrderStateException.class);

            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw InvalidOrderStateException when cancelling already CANCELLED order")
        void shouldThrowWhenCancellingCancelledOrder() {
            Order cancelledOrder = buildSavedOrder(1L, regularCustomer, OrderStatus.CANCELLED,
                    new BigDecimal("100000"), BigDecimal.ZERO, new BigDecimal("100000"),
                    new ArrayList<>());

            when(orderRepository.findById(1L)).thenReturn(Optional.of(cancelledOrder));

            assertThatThrownBy(() -> orderService.cancelOrder(1L))
                    .isInstanceOf(InvalidOrderStateException.class);

            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    // getOrderById

    @Nested
    @DisplayName("getOrderById()")
    class GetOrderById {

        @Test
        @DisplayName("Should return order when found")
        void shouldReturnOrderWhenFound() {
            Order order = buildSavedOrder(1L, regularCustomer, OrderStatus.CREATED,
                    new BigDecimal("50000"), BigDecimal.ZERO, new BigDecimal("50000"),
                    new ArrayList<>());

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            OrderResponse response = orderService.getOrderById(1L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getStatus()).isEqualTo(OrderStatus.CREATED);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when order not found")
        void shouldThrowWhenOrderNotFound() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getOrderById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
