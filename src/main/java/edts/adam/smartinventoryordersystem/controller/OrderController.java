package edts.adam.smartinventoryordersystem.controller;

import edts.adam.smartinventoryordersystem.dto.ApiResponse;
import edts.adam.smartinventoryordersystem.dto.order.request.CreateOrderRequest;
import edts.adam.smartinventoryordersystem.dto.order.response.OrderResponse;
import edts.adam.smartinventoryordersystem.service.OrderService;
import edts.adam.smartinventoryordersystem.util.MessageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing endpoints for order creation, payment, cancellation,
 * and retrieval.
 * All responses are wrapped in {@link ApiResponse}.
 */
@RestController
@RequestMapping("/orders")
@Tag(name = "Order", description = "Order management endpoints")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;
    private final MessageUtil messageUtil;

    public OrderController(OrderService orderService, MessageUtil messageUtil) {
        this.orderService = orderService;
        this.messageUtil = messageUtil;
    }

    @Operation(summary = "Create a new order", description = "Creates an order with stock validation, membership discount, and extra discount for orders > 5M")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("POST /orders — Creating order for customerId: {} with {} item(s)",
                request.getCustomerId(), request.getItems().size());
        OrderResponse response = orderService.createOrder(request);
        log.info("Order created successfully with id: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(messageUtil.get("order.created.success"), response));
    }

    @Operation(summary = "Pay an order", description = "Transitions order from CREATED to PAID. Updates customer totalSpent and recalculates membership")
    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<OrderResponse>> payOrder(@PathVariable Long id) {
        log.info("POST /orders/{}/pay — Processing payment", id);
        OrderResponse response = orderService.payOrder(id);
        log.info("Order id: {} paid successfully", id);
        return ResponseEntity.ok(ApiResponse.success(messageUtil.get("order.paid.success"), response));
    }

    @Operation(summary = "Cancel an order", description = "Transitions order from CREATED to CANCELLED. Restores product stock")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable Long id) {
        log.info("POST /orders/{}/cancel — Cancelling order", id);
        OrderResponse response = orderService.cancelOrder(id);
        log.info("Order id: {} cancelled successfully", id);
        return ResponseEntity.ok(ApiResponse.success(messageUtil.get("order.cancelled.success"), response));
    }

    @Operation(summary = "Get order by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        log.info("GET /orders/{} — Fetching order", id);
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(messageUtil.get("api.response.success"), response));
    }
}
