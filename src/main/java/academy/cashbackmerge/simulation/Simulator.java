package academy.cashbackmerge.simulation;

import academy.cashbackmerge.Load;
import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.game.ReplayData;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class Simulator {
    private static final int RATE_SCALE = 10;
    private final long seed;

    public Simulator(long seed) {
        this.seed = seed;
    }

    public Map<String, SimulatorResponse> compareCampaigns(List<Path> configPaths, int countPlayers, Strategy strategy)
            throws IOException {
        if (configPaths.isEmpty()) {
            throw new IllegalArgumentException("Укажите хотя бы один конфиг для сравнения");
        }
        Map<String, SimulatorResponse> reports = new LinkedHashMap<>();
        for (Path path : configPaths) {
            CampaignConfig config = Load.loadConfig(path);
            reports.put(path.toString(), simulate(config, countPlayers, strategy));
        }
        return Collections.unmodifiableMap(reports);
    }

    public SimulatorResponse simulate(CampaignConfig config, int countPlayers, Strategy strategy) throws IOException {
        return simulate(config, countPlayers, strategy, -1);
    }

    public SimulatorResponse simulate(CampaignConfig config, int countPlayers, Strategy strategy, int id)
            throws IOException {
        if (countPlayers <= 0) {
            throw new IllegalArgumentException("количество запусков должно быть больше 0");
        }
        if (id != -1 && (id < 1 || id > countPlayers)) {
            throw new IllegalArgumentException(
                    "Номер игрока должен быть от 1 до " + countPlayers + ", либо -1 для отключения записи replay");
        }
        Random random = new Random(seed);
        Map<Integer, Integer> maxTileDistribution = new TreeMap<>();
        Map<BigDecimal, Integer> rewardDistribution = new TreeMap<>();
        int maxRewardCount = 0;
        int maxScore = 0;
        long sumScore = 0;

        for (int i = 0; i < countPlayers; i++) {
            long playerSeed = random.nextLong();
            Player player;
            if (strategy.equals(Strategy.RANDOM)) {
                player = new RandomPlayer(config, playerSeed);
            } else {
                player = new GreedyScorePlayer(config, playerSeed);
            }
            ResponseSession response = player.play();

            maxTileDistribution.merge(response.maxField(), 1, Integer::sum);
            rewardDistribution.merge(response.reward(), 1, Integer::sum);

            if (response.value() >= config.maxRewardCashback()) {
                maxRewardCount++;
            }

            sumScore += response.score();
            maxScore = Integer.max(maxScore, response.score());

            if (id == i + 1) {
                Load.writeJson(Path.of("replay.json"), new ReplayData(config, playerSeed, player.getMoves()));
            }
        }

        BigDecimal rate = BigDecimal.valueOf(maxRewardCount)
                .divide(BigDecimal.valueOf(countPlayers), RATE_SCALE, RoundingMode.HALF_UP);

        boolean ok = BigDecimal.valueOf(maxRewardCount)
                        .compareTo(config.maxRewardAllowedRate().multiply(BigDecimal.valueOf(countPlayers)))
                <= 0;
        return new SimulatorResponse(
                ok ? SimulationStatus.OK : SimulationStatus.FAILED,
                (double) sumScore / countPlayers,
                maxScore,
                rate,
                countPlayers,
                config.movesLimit(),
                seed,
                strategy,
                maxTileDistribution,
                rewardDistribution);
    }
}
