package edts.adam.smartinventoryordersystem.service;

import edts.adam.smartinventoryordersystem.dto.customer.request.CreateCustomerRequest;
import edts.adam.smartinventoryordersystem.dto.customer.response.CustomerResponse;
import edts.adam.smartinventoryordersystem.model.Customer;
import edts.adam.smartinventoryordersystem.exception.DuplicateResourceException;
import edts.adam.smartinventoryordersystem.exception.ResourceNotFoundException;
import edts.adam.smartinventoryordersystem.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for customer management.
 * Handles customer registration with email uniqueness and customer retrieval.
 */
@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        log.debug("Checking for duplicate email: {}", request.getEmail());
        if (customerRepository.existsByEmail(request.getEmail())) {
            log.warn("Duplicate email detected: {}", request.getEmail());
            throw new DuplicateResourceException("customer.email.duplicate", request.getEmail());
        }

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());

        Customer saved = customerRepository.save(customer);
        log.info("Customer created: id={}, name={}, email={}", saved.getId(), saved.getName(), saved.getEmail());
        return CustomerResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Customer not found: id={}", id);
                    return new ResourceNotFoundException("customer.not.found", id);
                });
        log.debug("Customer retrieved: id={}, name={}", customer.getId(), customer.getName());
        return CustomerResponse.fromEntity(customer);
    }
}
