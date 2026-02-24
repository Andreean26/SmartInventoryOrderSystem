package edts.adam.smartinventoryordersystem.controller;

import edts.adam.smartinventoryordersystem.dto.ApiResponse;
import edts.adam.smartinventoryordersystem.dto.customer.request.CreateCustomerRequest;
import edts.adam.smartinventoryordersystem.dto.customer.response.CustomerResponse;
import edts.adam.smartinventoryordersystem.service.CustomerService;
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
 * REST controller exposing endpoints for customer registration and retrieval.
 * All responses are wrapped in {@link ApiResponse}.
 */
@RestController
@RequestMapping("/customers")
@Tag(name = "Customer", description = "Customer management endpoints")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;
    private final MessageUtil messageUtil;

    public CustomerController(CustomerService customerService, MessageUtil messageUtil) {
        this.customerService = customerService;
        this.messageUtil = messageUtil;
    }

    @Operation(summary = "Register a new customer", description = "Creates a customer with unique email. Defaults to REGULAR membership")
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        log.info("POST /customers — Creating customer with email: {}", request.getEmail());
        CustomerResponse response = customerService.createCustomer(request);
        log.info("Customer created successfully with id: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(messageUtil.get("customer.created.success"), response));
    }

    @Operation(summary = "Get customer by ID", description = "Returns customer details including membership level and total spent")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable Long id) {
        log.info("GET /customers/{} — Fetching customer", id);
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success(messageUtil.get("api.response.success"), response));
    }
}
