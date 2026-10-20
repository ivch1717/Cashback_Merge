package academy.cashbackmerge.mandatory.mr3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import academy.cashbackmerge.Load;
import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.config.RewardTier;
import academy.cashbackmerge.config.SpawnRule;
import academy.cashbackmerge.game.GameSession;
import academy.cashbackmerge.simulation.ResponseSession;
import academy.cashbackmerge.simulation.SimulationStatus;
import academy.cashbackmerge.simulation.Simulator;
import academy.cashbackmerge.simulation.SimulatorResponse;
import academy.cashbackmerge.simulation.Strategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("MR3. Награды и симуляция")
class RewardAndSimulationTest {

    @Test
    @DisplayName("Максимальная плитка 16% даёт награду соответствующего tier")
    void rewardMatchesHighestTile() throws IOException {
        CampaignConfig config = Load.loadConfig(Path.of("config/campaign.example.json"));
        int[][] board = {
            {8, 0, 0, 0},
            {0, 16, 0, 0},
            {0, 0, 4, 0},
            {0, 0, 0, 2}
        };
        GameSession session = new GameSession(config, 42L, board);

        ResponseSession result = session.getReward();

        assertEquals(16, result.maxField());
        assertEquals(16, result.value());
        assertEquals(0, new BigDecimal("500").compareTo(result.reward()));
        assertEquals(0, result.score());
    }

    @Test
    @DisplayName("Weighted random учитывает вероятности из конфига")
    void spawnRespectsConfiguredProbabilities() {
        int samples = 5000;
        CampaignConfig config = config(
                List.of(new SpawnRule(1, new BigDecimal("0.85")), new SpawnRule(2, new BigDecimal("0.15"))), "0.20");
        Random seeds = new Random(42L);
        int ones = 0;
        for (int i = 0; i < samples; i++) {
            GameSession session = new GameSession(config, seeds.nextLong());
            session.nextStep();
            int value = session.getReward().maxField();
            assertTrue(value == 1 || value == 2);
            if (value == 1) {
                ones++;
            }
        }
        assertTrue(ones >= 4100 && ones <= 4400, "Количество единиц: " + ones);

        CampaignConfig onlyTwos =
                config(List.of(new SpawnRule(1, BigDecimal.ZERO), new SpawnRule(2, BigDecimal.ONE)), "0.20");
        for (int i = 0; i < 100; i++) {
            GameSession session = new GameSession(onlyTwos, seeds.nextLong());
            session.nextStep();
            assertEquals(2, session.getReward().maxField());
        }
    }

    @Test
    @DisplayName("Отчёт содержит распределение максимальных плиток и наград")
    void reportContainsDistributions(@TempDir Path directory) throws IOException {
        SimulatorResponse report = mixedReport("0.50");

        assertEquals(Map.of(2, 2, 4, 1), report.maxTileDistribution());
        assertEquals(Map.of(BigDecimal.ZERO, 2, new BigDecimal("50"), 1), report.rewardDistribution());
        assertEquals(3, report.countPlayers());
        assertEquals(1, report.movesLimit());
        assertEquals(14L, report.seed());
        assertEquals(Strategy.RANDOM, report.strategy());
        assertEquals(4.0 / 3, report.averageScore(), 0.000001);
        assertEquals(4, report.maxScore());

        Path output = directory.resolve("report.json");
        Load.writeReport(output, report);
        JsonNode json = new ObjectMapper().readTree(Load.readFile(output));
        assertEquals(2, json.path("maxTileDistribution").path("2").asInt());
        assertEquals(1, json.path("maxTileDistribution").path("4").asInt());
        assertEquals(2, json.path("rewardDistribution").path("0").asInt());
        assertEquals(1, json.path("rewardDistribution").path("50").asInt());
        assertEquals(3, json.path("countPlayers").asInt());
        assertEquals(14L, json.path("seed").asLong());
    }

    @Test
    @DisplayName("Отчёт содержит долю игроков с максимальной наградой")
    void reportContainsMaxRewardRate() throws IOException {
        SimulatorResponse report = mixedReport("0.50");
        assertEquals(0, new BigDecimal("0.3333333333").compareTo(report.maxRewardRate()));

        SimulatorResponse noMaxReward = new Simulator(42L)
                .simulate(config(List.of(new SpawnRule(1, BigDecimal.ONE)), "0.20"), 20, Strategy.RANDOM);
        assertEquals(0, noMaxReward.maxRewardRate().compareTo(BigDecimal.ZERO));

        SimulatorResponse allMaxReward = new Simulator(42L)
                .simulate(config(List.of(new SpawnRule(4, BigDecimal.ONE)), "1"), 20, Strategy.RANDOM);
        assertEquals(0, allMaxReward.maxRewardRate().compareTo(BigDecimal.ONE));
    }

    @Test
    @DisplayName("Статус OK, если actualMaxRewardRate <= maxRewardAllowedRate, иначе FAILED")
    void reportStatusReflectsBusinessConstraint() throws IOException {
        assertEquals(SimulationStatus.OK, mixedReport("0.50").simulationStatus());
        assertEquals(SimulationStatus.FAILED, mixedReport("0.20").simulationStatus());

        SimulatorResponse atBoundary = new Simulator(42L)
                .simulate(config(List.of(new SpawnRule(4, BigDecimal.ONE)), "1"), 20, Strategy.RANDOM);
        assertEquals(SimulationStatus.OK, atBoundary.simulationStatus());

        assertEquals(SimulationStatus.FAILED, mixedReport("0.33333333332").simulationStatus());
    }

    private SimulatorResponse mixedReport(String allowedRate) throws IOException {
        CampaignConfig config = config(
                List.of(new SpawnRule(1, new BigDecimal("0.5")), new SpawnRule(2, new BigDecimal("0.5"))), allowedRate);
        return new Simulator(14L).simulate(config, 3, Strategy.RANDOM);
    }

    private CampaignConfig config(List<SpawnRule> rules, String allowedRate) {
        return new CampaignConfig(
                4,
                1,
                rules,
                List.of(new RewardTier(4, new BigDecimal("50")), new RewardTier(2, BigDecimal.ZERO)),
                4,
                new BigDecimal(allowedRate));
    }
}
