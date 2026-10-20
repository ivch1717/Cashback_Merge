package academy.cashbackmerge.mandatory.mr2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.config.RewardTier;
import academy.cashbackmerge.config.SpawnRule;
import academy.cashbackmerge.game.GameSession;
import academy.cashbackmerge.game.GameState;
import academy.cashbackmerge.game.Move;
import academy.cashbackmerge.game.Position;
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
        GameSession session = createSession(10, new int[] {1, 0, 0, 0});
        String before = session.boardAsString();
        int tilesBefore = tileCount(session);

        assertThrows(IllegalArgumentException.class, () -> session.applyMove(Move.LEFT));

        assertEquals(before, session.boardAsString());
        assertEquals(tilesBefore, tileCount(session));
        assertEquals(0, session.getMovesUsed());
        assertEquals(0, session.getScore());

        session.applyMove(Move.RIGHT);
        assertEquals(1, session.getMovesUsed());
    }

    @Test
    @DisplayName("После валидного хода появляется ровно одна новая плитка")
    void validMoveSpawnsExactlyOneTile() {
        GameSession session = createSession(10, new int[] {0, 0, 0, 2});
        int tilesBefore = tileCount(session);

        session.applyMove(Move.LEFT);

        assertEquals(tilesBefore + 1, tileCount(session));
        assertEquals(2, session.getTileValue(new Position(0, 0)));
        assertEquals(3, tileSum(session));
        assertEquals(1, session.getMovesUsed());

        GameSession mergingSession = createSession(10, new int[] {2, 2, 0, 0});
        int tilesBeforeMerge = tileCount(mergingSession);

        mergingSession.applyMove(Move.LEFT);

        assertEquals(tilesBeforeMerge, tileCount(mergingSession));
        assertEquals(4, mergingSession.getTileValue(new Position(0, 0)));
        assertEquals(5, tileSum(mergingSession));
        assertEquals(1, mergingSession.getMovesUsed());
    }

    @Test
    @DisplayName("После N валидных ходов игра завершена")
    void gameEndsAfterMovesLimit() {
        GameSession session = createSession(2, new int[] {0, 0, 0, 1});

        session.applyMove(Move.LEFT);
        assertEquals(1, session.getMovesUsed());
        session.applyMove(Move.DOWN);
        assertEquals(2, session.getMovesUsed());
        String finalBoard = session.boardAsString();
        int finalScore = session.getScore();

        assertThrows(IllegalArgumentException.class, () -> session.applyMove(Move.RIGHT));
        assertEquals(finalBoard, session.boardAsString());
        assertEquals(finalScore, session.getScore());
        assertEquals(2, session.getMovesUsed());
    }

    @Test
    @DisplayName("Достижение плитки 32% не заканчивает игру — доигрываем до лимита ходов")
    void reachingMaxTileDoesNotEndGame() {
        GameSession session = createSession(3, new int[] {16, 16, 0, 0});

        session.applyMove(Move.LEFT);
        assertEquals(32, session.getTileValue(new Position(0, 0)));
        assertEquals(1, session.getMovesUsed());

        session.applyMove(Move.DOWN);
        assertEquals(32, session.getTileValue(new Position(3, 0)));
        assertEquals(2, session.getMovesUsed());

        session.applyMove(Move.RIGHT);
        assertEquals(3, session.getMovesUsed());
        assertThrows(IllegalArgumentException.class, () -> session.applyMove(Move.UP));
    }

    @Test
    @DisplayName("Score считается по правилам и растёт только при слияниях")
    void scoreIsCalculatedOnMerges() {
        GameSession shiftingSession = createSession(10, new int[] {0, 0, 0, 2});
        assertEquals(0, shiftingSession.getScore());

        shiftingSession.applyMove(Move.LEFT);
        assertEquals(0, shiftingSession.getScore());

        GameSession mergingSession = createSession(10, new int[] {2, 2, 4, 4});
        assertEquals(0, mergingSession.getScore());

        mergingSession.applyMove(Move.LEFT);
        assertEquals(12, mergingSession.getScore());
        mergingSession.nextStep();
        assertEquals(12, mergingSession.getScore());

        GameState state = new GameState(4, 10);
        state.placeTitle(new Position(0, 0), 1);
        state.placeTitle(new Position(0, 1), 1);
        state.applyMove(Move.LEFT);
        assertEquals(2, state.getScore());
        state.placeTitle(new Position(0, 1), 2);
        state.applyMove(Move.LEFT);
        assertEquals(6, state.getScore());
    }

    private GameSession createSession(int movesLimit, int[] firstRow) {
        int[][] board = new int[4][4];
        board[0] = firstRow;
        return new GameSession(config(movesLimit), 42L, board);
    }

    private CampaignConfig config(int movesLimit) {
        return new CampaignConfig(
                4,
                movesLimit,
                List.of(new SpawnRule(1, BigDecimal.ONE)),
                List.of(new RewardTier(32, new BigDecimal("2000"))),
                32,
                new BigDecimal("0.20"));
    }

    private int tileCount(GameSession session) {
        int count = 0;
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 4; column++) {
                if (session.getTileValue(new Position(row, column)) != 0) {
                    count++;
                }
            }
        }
        return count;
    }

    private int tileSum(GameSession session) {
        int sum = 0;
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 4; column++) {
                sum += session.getTileValue(new Position(row, column));
            }
        }
        return sum;
    }
}
