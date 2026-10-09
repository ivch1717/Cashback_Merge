package academy.cashbackmerge.mandatory.mr1;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import academy.cashbackmerge.CampaignConfig;
import academy.cashbackmerge.Load;
import academy.cashbackmerge.SpawnRule;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Обязательные тесты: конфиг кампании. */
@DisplayName("MR1. Конфиг кампании")
class ConfigValidationTest {

    @Test
    @DisplayName("Валидный конфиг принимается")
    void validConfigIsAccepted() {
        CampaignConfig config = assertDoesNotThrow(() -> Load.loadConfig(Path.of("config/campaign.example.json")));
        assertEquals(4, config.boardSize());
        assertEquals(100, config.movesLimit());
        assertEquals(2, config.spawnRules().size());
    }

    @Test
    @DisplayName("Сумма вероятностей spawn-правил не равна 1 — конфиг отклоняется")
    void probabilitiesNotSummingToOneAreRejected() {
        List<SpawnRule> rules =
                List.of(new SpawnRule(1, new BigDecimal("0.85")), new SpawnRule(2, new BigDecimal("0.10")));
        assertThrows(IllegalArgumentException.class, () -> new CampaignConfig(4, 100, rules));
    }

    @Test
    @DisplayName("movesLimit <= 0 — конфиг отклоняется")
    void nonPositiveMovesLimitIsRejected() {
        List<SpawnRule> rules = List.of(new SpawnRule(1, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class, () -> new CampaignConfig(4, 0, rules));
        assertThrows(IllegalArgumentException.class, () -> new CampaignConfig(4, -1, rules));
    }

    @Test
    @DisplayName("Пустой список spawn-правил — конфиг отклоняется")
    void emptySpawnRulesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new CampaignConfig(4, 100, List.of()));
    }
}
