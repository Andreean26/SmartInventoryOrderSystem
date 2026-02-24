package edts.adam.smartinventoryordersystem.repository;

import edts.adam.smartinventoryordersystem.model.Order;
import edts.adam.smartinventoryordersystem.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Order} entities.
 * Provides a custom query to check if a product has orders with a specific
 * status.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByOrderItems_Product_IdAndStatus(Long productId, OrderStatus status);
}
