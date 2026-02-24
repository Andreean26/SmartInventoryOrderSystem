package edts.adam.smartinventoryordersystem.enums;

import java.math.BigDecimal;

/**
 * Customer membership tiers with associated discount percentages.
 * Levels are determined by cumulative spending and can only be upgraded.
 *
 * <ul>
 * <li>REGULAR — 0% discount (default)</li>
 * <li>GOLD — 10% discount (totalSpent ≥ 10,000,000)</li>
 * <li>PLATINUM — 20% discount (totalSpent ≥ 50,000,000)</li>
 * </ul>
 */
public enum MembershipLevel {

    REGULAR(BigDecimal.ZERO),
    GOLD(new BigDecimal("0.10")),
    PLATINUM(new BigDecimal("0.20"));

    private final BigDecimal discountPercentage;

    MembershipLevel(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    /**
     * Returns the discount percentage as a decimal (e.g., 0.10 for 10%).
     */
    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    /**
     * Determines the appropriate membership level based on total spent.
     * - totalSpent >= 50,000,000 → PLATINUM
     * - totalSpent >= 10,000,000 → GOLD
     * - otherwise → REGULAR
     */
    public static MembershipLevel fromTotalSpent(BigDecimal totalSpent) {
        if (totalSpent.compareTo(new BigDecimal("50000000")) >= 0) {
            return PLATINUM;
        } else if (totalSpent.compareTo(new BigDecimal("10000000")) >= 0) {
            return GOLD;
        }
        return REGULAR;
    }
}
