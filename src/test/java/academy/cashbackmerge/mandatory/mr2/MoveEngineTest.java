package academy.cashbackmerge.mandatory.mr2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import academy.cashbackmerge.CampaignConfig;
import academy.cashbackmerge.GameSession;
import academy.cashbackmerge.GameState;
import academy.cashbackmerge.Move;
import academy.cashbackmerge.Position;
import academy.cashbackmerge.SpawnRule;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Обязательные тесты: движок ходов — что считается ходом и что после него происходит. */
@DisplayName("MR2. Движок ходов")
class MoveEngineTest {

    @Test
    @DisplayName("Невалидный ход не меняет поле, не тратит ход и не создаёт плитку")
    void invalidMoveChangesNothing() {
        GameSession session = createSession(10);
        GameState state = session.getState();
        state.placeTitle(new Position(0, 0), 1);
        String before = session.boardAsString();
        int freeBefore = state.getFreePosition().size();

        assertThrows(IllegalArgumentException.class, () -> session.applyMove(Move.LEFT));

        assertEquals(before, session.boardAsString());
        assertEquals(freeBefore, state.getFreePosition().size());
        assertEquals(0, state.getMovesUsed());
        assertEquals(0, state.getScore());

        session.applyMove(Move.RIGHT);
        assertEquals(1, state.getMovesUsed());
    }

    @Test
    @DisplayName("После валидного хода появляется ровно одна новая плитка")
    void validMoveSpawnsExactlyOneTile() {
        GameSession session = createSession(10);
        GameState state = session.getState();
        state.placeTitle(new Position(0, 3), 2);
        int freeBefore = state.getFreePosition().size();

        session.applyMove(Move.LEFT);

        assertEquals(freeBefore - 1, state.getFreePosition().size());
        assertEquals(2, state.getTileValue(new Position(0, 0)));
        assertEquals(3, tileSum(state));
        assertEquals(1, state.getMovesUsed());

        GameSession mergingSession = createSession(10);
        GameState mergingState = mergingSession.getState();
        mergingState.placeTitle(new Position(0, 0), 2);
        mergingState.placeTitle(new Position(0, 1), 2);
        int freeBeforeMerge = mergingState.getFreePosition().size();

        mergingSession.applyMove(Move.LEFT);

        assertEquals(freeBeforeMerge, mergingState.getFreePosition().size());
        assertEquals(4, mergingState.getTileValue(new Position(0, 0)));
        assertEquals(5, tileSum(mergingState));
        assertEquals(1, mergingState.getMovesUsed());
    }

    @Test
    @DisplayName("После N валидных ходов игра завершена")
    void gameEndsAfterMovesLimit() {
        GameSession session = createSession(2);
        GameState state = session.getState();
        state.placeTitle(new Position(0, 3), 1);

        session.applyMove(Move.LEFT);
        assertEquals(1, state.getMovesUsed());
        session.applyMove(Move.DOWN);
        assertEquals(2, state.getMovesUsed());
        String finalBoard = session.boardAsString();
        int finalScore = state.getScore();

        assertThrows(IllegalArgumentException.class, () -> session.applyMove(Move.RIGHT));
        assertEquals(finalBoard, session.boardAsString());
        assertEquals(finalScore, state.getScore());
        assertEquals(2, state.getMovesUsed());
    }

    @Test
    @DisplayName("Достижение плитки 32% не заканчивает игру — доигрываем до лимита ходов")
    void reachingMaxTileDoesNotEndGame() {
        GameSession session = createSession(3);
        GameState state = session.getState();
        state.placeTitle(new Position(0, 0), 16);
        state.placeTitle(new Position(0, 1), 16);

        session.applyMove(Move.LEFT);
        assertEquals(32, state.getTileValue(new Position(0, 0)));
        assertEquals(1, state.getMovesUsed());

        session.applyMove(Move.DOWN);
        assertEquals(32, state.getTileValue(new Position(3, 0)));
        assertEquals(2, state.getMovesUsed());

        session.applyMove(Move.RIGHT);
        assertEquals(3, state.getMovesUsed());
        assertThrows(IllegalArgumentException.class, () -> session.applyMove(Move.UP));
    }

    @Test
    @DisplayName("Score считается по правилам и растёт только при слияниях")
    void scoreIsCalculatedOnMerges() {
        GameSession shiftingSession = createSession(10);
        GameState shiftingState = shiftingSession.getState();
        shiftingState.placeTitle(new Position(0, 3), 2);
        assertEquals(0, shiftingState.getScore());

        shiftingSession.applyMove(Move.LEFT);
        assertEquals(0, shiftingState.getScore());

        GameSession mergingSession = createSession(10);
        GameState mergingState = mergingSession.getState();
        mergingState.placeTitle(new Position(0, 0), 2);
        mergingState.placeTitle(new Position(0, 1), 2);
        mergingState.placeTitle(new Position(0, 2), 4);
        mergingState.placeTitle(new Position(0, 3), 4);
        assertEquals(0, mergingState.getScore());

        mergingSession.applyMove(Move.LEFT);
        assertEquals(12, mergingState.getScore());
        mergingSession.nextStep();
        assertEquals(12, mergingState.getScore());

        // Отдельное слияние на следующем ходу прибавляет очки к уже накопленным.
        GameState accumulatingState = new GameState(4, 10);
        accumulatingState.placeTitle(new Position(0, 0), 1);
        accumulatingState.placeTitle(new Position(0, 1), 1);
        accumulatingState.applyMove(Move.LEFT);
        assertEquals(2, accumulatingState.getScore());
        accumulatingState.placeTitle(new Position(0, 1), 2);
        accumulatingState.applyMove(Move.LEFT);
        assertEquals(6, accumulatingState.getScore());
    }

    private GameSession createSession(int movesLimit) {
        CampaignConfig config = new CampaignConfig(4, movesLimit, List.of(new SpawnRule(1, BigDecimal.ONE)));
        return new GameSession(config, 42L);
    }

    private int tileSum(GameState state) {
        int sum = 0;
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 4; column++) {
                sum += state.getTileValue(new Position(row, column));
            }
        }
        return sum;
    }
}
