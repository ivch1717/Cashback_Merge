package academy.cashbackmerge.simulation;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public record SimulatorResponse(
        SimulationStatus simulationStatus,
        double averageScore,
        int maxScore,
        BigDecimal maxRewardRate,
        int countPlayers,
        int movesLimit,
        long seed,
        Strategy strategy,
        Map<Integer, Integer> maxTileDistribution,
        Map<BigDecimal, Integer> rewardDistribution) {
    public SimulatorResponse {
        maxTileDistribution = Collections.unmodifiableMap(new TreeMap<>(maxTileDistribution));
        rewardDistribution = Collections.unmodifiableMap(new TreeMap<>(rewardDistribution));
    }
}
