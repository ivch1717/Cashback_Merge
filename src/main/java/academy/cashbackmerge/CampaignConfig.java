package academy.cashbackmerge;

import java.math.BigDecimal;
import java.util.List;

public record CampaignConfig(
        int boardSize,
        int movesLimit,
        List<SpawnRule> spawnRules,
        List<RewardTier> rewardTable,
        int maxRewardCashback,
        BigDecimal maxRewardAllowedRate) {
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

        if (maxRewardCashback <= 0) {
            throw new IllegalArgumentException("Уровень кэшбэка для максимальной награды должен быть положительным");
        }

        if (rewardTable.isEmpty()) {
            throw new IllegalArgumentException("Таблица наград не должна быть пустой");
        }

        if (maxRewardAllowedRate.compareTo(BigDecimal.ZERO) <= 0
                || maxRewardAllowedRate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException(
                    "Допустимая доля игроков с максимальной наградой должна быть больше 0 и не больше 1");
        }

        RewardTier maxTier = rewardTable.stream()
            .filter(tier -> tier.cashback() == maxRewardCashback)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Уровень максимальной награды отсутствует в таблице наград"));


        spawnRules = List.copyOf(spawnRules);
        rewardTable = List.copyOf(rewardTable);
    }
}
