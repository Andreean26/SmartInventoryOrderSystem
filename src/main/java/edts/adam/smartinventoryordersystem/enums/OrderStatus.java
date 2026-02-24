package edts.adam.smartinventoryordersystem.enums;

import java.util.Map;
import java.util.Set;

/**
 * Defines the order lifecycle states and valid transitions.
 * CREATED → PAID and CREATED → CANCELLED are the only allowed transitions.
 * PAID and CANCELLED are terminal states.
 */
public enum OrderStatus {

    CREATED,
    PAID,
    CANCELLED;

    /**
     * Defines valid state transitions for the order lifecycle.
     * CREATED → PAID or CANCELLED
     * PAID → (terminal state, no transitions allowed)
     * CANCELLED → (terminal state, no transitions allowed)
     */
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
            CREATED, Set.of(PAID, CANCELLED),
            PAID, Set.of(),
            CANCELLED, Set.of());

    /**
     * Checks if transitioning from the current status to the target status is
     * allowed.
     *
     * @param target the desired target status
     * @return true if the transition is valid
     */
    public boolean canTransitionTo(OrderStatus target) {
        return VALID_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }
}
