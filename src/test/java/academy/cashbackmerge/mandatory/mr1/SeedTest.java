package academy.cashbackmerge.mandatory.mr1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import academy.cashbackmerge.CampaignConfig;
import academy.cashbackmerge.GameSession;
import academy.cashbackmerge.Load;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Обязательные тесты: воспроизводимость по seed. */
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
    @Disabled("MR3: реализуй тест и удали эту строку")
    @DisplayName("Одинаковый конфиг, seed и стратегия дают одинаковый отчёт симуляции")
    void sameInputsProduceSameSimulationReport() {
        fail("Тест не реализован");
    }
}
