package academy.cashbackmerge;

import java.math.BigDecimal;

public record SpawnRule(int cashback, BigDecimal probability) {
    public SpawnRule {
        if (cashback <= 0) {
            throw new IllegalArgumentException("Размер кешбека должен быть положительным");
        }
        if (probability.compareTo(BigDecimal.ZERO) < 0 || probability.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Вероятность должна быть от 0 до 1");
        }
    }
}
