package academy.cashbackmerge;

import java.math.BigDecimal;
import java.util.List;

public record CampaignConfig(int boardSize, int movesLimit, List<SpawnRule> spawnRules) {
    private static final int BOARD_SIZE = 4;

    public CampaignConfig {
        if (boardSize != BOARD_SIZE) {
            throw new IllegalArgumentException("Размер поля должен быть 4 * 4");
        }
        if (movesLimit <= 0) {
            throw new IllegalArgumentException("Количество ходов должно быть положительным");
        }
        if (spawnRules.isEmpty()) {
            throw new IllegalArgumentException("Правила появления новых плиток не должно быть пустым");
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (SpawnRule rule : spawnRules) {
            sum = sum.add(rule.probability());
        }
        if (sum.compareTo(BigDecimal.ONE) != 0) {
            throw new IllegalArgumentException("Сумма вероятностей должна быть равна 1");
        }

        spawnRules = List.copyOf(spawnRules);
    }
}
