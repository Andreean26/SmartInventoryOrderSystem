package edts.adam.smartinventoryordersystem.repository;

import edts.adam.smartinventoryordersystem.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Customer} entities.
 * Provides a custom query for email uniqueness validation.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByEmail(String email);
}
