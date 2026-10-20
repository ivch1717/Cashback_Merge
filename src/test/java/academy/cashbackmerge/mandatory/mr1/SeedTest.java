package academy.cashbackmerge.mandatory.mr1;

import static org.junit.jupiter.api.Assertions.assertEquals;

import academy.cashbackmerge.CampaignConfig;
import academy.cashbackmerge.game.GameSession;
import academy.cashbackmerge.Load;
import academy.cashbackmerge.simulation.Simulator;
import academy.cashbackmerge.simulation.SimulatorResponse;
import academy.cashbackmerge.simulation.Strategy;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("MR1. Воспроизводимость по seed")
class SeedTest {

    @Test
    @DisplayName("Одинаковый конфиг и seed дают одинаковое начальное поле")
    void sameConfigAndSeedProduceSameInitialBoard() throws IOException {
        CampaignConfig config = Load.loadConfig(Path.of("config/campaign.example.json"));
        GameSession session1 = new GameSession(config, 42L);
        GameSession session2 = new GameSession(config, 42L);

        session1.startGame();
        session2.startGame();

        assertEquals(session1.boardAsString(), session2.boardAsString());
    }

    @Test
    @DisplayName("Одинаковый конфиг, seed и стратегия дают одинаковый отчёт симуляции")
    void sameInputsProduceSameSimulationReport(@TempDir Path directory) throws IOException {
        CampaignConfig config = Load.loadConfig(Path.of("config/campaign.example.json"));
        Simulator simulator = new Simulator(42L);

        SimulatorResponse first = simulator.simulate(config, 100, Strategy.RANDOM);
        SimulatorResponse second = new Simulator(42L).simulate(config, 100, Strategy.RANDOM);

        assertEquals(first, second);
        assertEquals(first, simulator.simulate(config, 100, Strategy.RANDOM));

        Path firstFile = directory.resolve("first.json");
        Path secondFile = directory.resolve("second.json");
        Load.writeReport(firstFile, first);
        Load.writeReport(secondFile, second);
        assertEquals(Load.readFile(firstFile), Load.readFile(secondFile));
    }
}
