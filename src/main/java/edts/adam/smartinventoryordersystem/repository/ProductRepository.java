package edts.adam.smartinventoryordersystem.repository;

import edts.adam.smartinventoryordersystem.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Product} entities.
 * Provides custom queries for name uniqueness checks and active product
 * filtering.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Optional<Product> findByIdAndActiveTrue(Long id);

    Page<Product> findByActiveTrue(Pageable pageable);
}
