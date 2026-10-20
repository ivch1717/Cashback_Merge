package academy.cashbackmerge.simulation;

import academy.cashbackmerge.CampaignConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class Simulator {
    private static final int RATE_SCALE = 10;
    private final long seed;

    public Simulator(long seed){
        this.seed = seed;
    }

    public SimulatorResponse simulate(CampaignConfig config, int countPlayers, Strategy strategy){
        if (countPlayers <= 0){
            throw new IllegalArgumentException("количество запусков должно быть больше 0");
        }
        Random random = new Random(seed);
        Map<Integer, Integer> maxTileDistribution = new TreeMap<>();
        Map<BigDecimal, Integer> rewardDistribution = new TreeMap<>();
        int maxRewardCount = 0;
        int maxScore = 0;
        long sumScore = 0;

        for (int i = 0; i < countPlayers; i++) {
            RandomPlayer player = new RandomPlayer(config, random.nextLong());
            ResponseSession response = player.play();

            maxTileDistribution.merge(response.maxField(), 1, Integer::sum);
            rewardDistribution.merge(response.reward(), 1, Integer::sum);

            if (response.value() >= config.maxRewardCashback()) {
                maxRewardCount++;
            }

            sumScore += response.score();
            maxScore = Integer.max(maxScore, response.score());
        }

        BigDecimal rate = BigDecimal.valueOf(maxRewardCount)
            .divide(BigDecimal.valueOf(countPlayers), RATE_SCALE, RoundingMode.HALF_UP);

        boolean ok = BigDecimal.valueOf(maxRewardCount).compareTo(
                config.maxRewardAllowedRate().multiply(BigDecimal.valueOf(countPlayers))) <= 0;
        return new SimulatorResponse(ok ? SimulationStatus.OK : SimulationStatus.FAILED,
                (double) sumScore / countPlayers, maxScore, rate,
                countPlayers, config.movesLimit(), seed, strategy,
                maxTileDistribution, rewardDistribution);
    }
}
