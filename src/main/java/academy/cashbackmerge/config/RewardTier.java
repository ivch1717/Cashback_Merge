package academy.cashbackmerge.config;

import java.math.BigDecimal;

public record RewardTier(int cashback, BigDecimal reward) {
    public RewardTier {
        if (cashback <= 0) {
            throw new IllegalArgumentException("Размер кэшбека должен быть положительным");
        }
        if (reward.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Размер кэшбека должен быть положительным");
        }
    }
}
