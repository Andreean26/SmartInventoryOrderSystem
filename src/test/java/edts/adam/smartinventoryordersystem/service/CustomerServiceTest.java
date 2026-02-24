package edts.adam.smartinventoryordersystem.service;

import edts.adam.smartinventoryordersystem.dto.customer.request.CreateCustomerRequest;
import edts.adam.smartinventoryordersystem.dto.customer.response.CustomerResponse;
import edts.adam.smartinventoryordersystem.enums.MembershipLevel;
import edts.adam.smartinventoryordersystem.exception.DuplicateResourceException;
import edts.adam.smartinventoryordersystem.exception.ResourceNotFoundException;
import edts.adam.smartinventoryordersystem.model.Customer;
import edts.adam.smartinventoryordersystem.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Unit Tests")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer sampleCustomer;

    @BeforeEach
    void setUp() {
        sampleCustomer = new Customer();
        sampleCustomer.setId(1L);
        sampleCustomer.setName("John Doe");
        sampleCustomer.setEmail("john@example.com");
        sampleCustomer.setMembershipLevel(MembershipLevel.REGULAR);
        sampleCustomer.setTotalSpent(BigDecimal.ZERO);
        sampleCustomer.setActive(true);
    }

    // =========================================================================
    // createCustomer
    // =========================================================================

    @Nested
    @DisplayName("createCustomer()")
    class CreateCustomer {

        private CreateCustomerRequest createRequest(String name, String email) {
            CreateCustomerRequest request = new CreateCustomerRequest();
            request.setName(name);
            request.setEmail(email);
            return request;
        }

        @Test
        @DisplayName("Should create customer successfully")
        void shouldCreateCustomer() {
            CreateCustomerRequest request = createRequest("John Doe", "john@example.com");

            when(customerRepository.existsByEmail("john@example.com")).thenReturn(false);
            when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);

            CustomerResponse response = customerService.createCustomer(request);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("John Doe");
            assertThat(response.getEmail()).isEqualTo("john@example.com");
            assertThat(response.getMembershipLevel()).isEqualTo(MembershipLevel.REGULAR);
            assertThat(response.getTotalSpent()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.getActive()).isTrue();

            verify(customerRepository).existsByEmail("john@example.com");
            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when email already exists")
        void shouldThrowWhenEmailExists() {
            CreateCustomerRequest request = createRequest("Jane Doe", "john@example.com");

            when(customerRepository.existsByEmail("john@example.com")).thenReturn(true);

            assertThatThrownBy(() -> customerService.createCustomer(request))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(customerRepository, never()).save(any(Customer.class));
        }

        @Test
        @DisplayName("Should set default membership to REGULAR")
        void shouldSetDefaultMembership() {
            CreateCustomerRequest request = createRequest("New User", "new@example.com");

            Customer saved = new Customer();
            saved.setId(2L);
            saved.setName("New User");
            saved.setEmail("new@example.com");
            saved.setMembershipLevel(MembershipLevel.REGULAR);
            saved.setTotalSpent(BigDecimal.ZERO);
            saved.setActive(true);

            when(customerRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(customerRepository.save(any(Customer.class))).thenReturn(saved);

            CustomerResponse response = customerService.createCustomer(request);

            assertThat(response.getMembershipLevel()).isEqualTo(MembershipLevel.REGULAR);
            assertThat(response.getTotalSpent()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // =========================================================================
    // getCustomerById
    // =========================================================================

    @Nested
    @DisplayName("getCustomerById()")
    class GetCustomerById {

        @Test
        @DisplayName("Should return customer when found")
        void shouldReturnCustomerWhenFound() {
            when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));

            CustomerResponse response = customerService.getCustomerById(1L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("John Doe");
            assertThat(response.getEmail()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when customer not found")
        void shouldThrowWhenCustomerNotFound() {
            when(customerRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.getCustomerById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should return correct membership level for GOLD customer")
        void shouldReturnGoldMembership() {
            sampleCustomer.setMembershipLevel(MembershipLevel.GOLD);
            sampleCustomer.setTotalSpent(new BigDecimal("15000000"));

            when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));

            CustomerResponse response = customerService.getCustomerById(1L);

            assertThat(response.getMembershipLevel()).isEqualTo(MembershipLevel.GOLD);
            assertThat(response.getTotalSpent()).isEqualByComparingTo(new BigDecimal("15000000"));
        }
    }
}
