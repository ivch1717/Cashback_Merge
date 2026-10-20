package academy.cashbackmerge.bonus;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import academy.cashbackmerge.game.GameState;
import academy.cashbackmerge.game.Move;
import academy.cashbackmerge.game.Position;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("Бонус. Генеративные проверки слияний")
class MergePropertiesTest {
    private static final long SEED = 42L;
    private static final int[] VALUES = {0, 1, 2, 4, 8, 16, 32, 64};

    @ParameterizedTest
    @EnumSource(Move.class)
    @DisplayName("Случайные поля совпадают с эталоном и сохраняют свойства слияния")
    void generatedBoardsMatchMergeRules(Move move) {
        Random random = new Random(SEED);
        for (int attempt = 0; attempt < 1000; attempt++) {
            int size = 2 + random.nextInt(7);
            int[][] board = randomBoard(random, size);
            GameState state = createState(board);
            checkMove(state, board, move, "seed=" + SEED + ", попытка=" + attempt);
        }
    }

    @ParameterizedTest
    @EnumSource(Move.class)
    @DisplayName("Полный перебор коротких строк проверяет пары, пустоты и запрет двойного слияния")
    void allSmallLinesMatchMergeRules(Move move) {
        int[] values = {0, 1, 2, 4};
        for (int sample = 0; sample < 256; sample++) {
            int[][] board = new int[4][4];
            int code = sample;
            for (int offset = 0; offset < 4; offset++) {
                Position position = position(move, 0, offset, 4);
                board[position.x()][position.y()] = values[code % values.length];
                code /= values.length;
            }
            checkMove(createState(board), board, move, "перебор=" + sample);
        }
    }

    @Test
    @DisplayName("Случайные последовательности ходов сохраняют сумму и накапливают правильный score")
    void generatedSequencesPreserveMergeProperties() {
        Random random = new Random(SEED);
        List<Move> moves = List.of(Move.values());
        for (int attempt = 0; attempt < 100; attempt++) {
            int size = 2 + random.nextInt(7);
            int[][] board = randomBoard(random, size);
            GameState state = createState(board);
            int validMoves = 0;
            for (int step = 0; step < 50; step++) {
                Move move = moves.get(random.nextInt(moves.size()));
                boolean changed =
                        checkMove(state, board, move, "seed=" + SEED + ", партия=" + attempt + ", шаг=" + step);
                if (changed) {
                    state.moves();
                    validMoves++;
                }
                assertEquals(validMoves, state.getMovesUsed());
                board = readBoard(state, size);
            }
        }
    }

    private boolean checkMove(GameState state, int[][] before, Move move, String context) {
        String details = context + ", направление=" + move + ", поле=" + Arrays.deepToString(before);
        int scoreBefore = state.getScore();
        int movesBefore = state.getMovesUsed();
        int[][] expected = referenceMove(before, move);
        boolean expectedChange = !Arrays.deepEquals(before, expected);

        assertEquals(expectedChange, state.canMove(move), details);
        assertBoardEquals(before, readBoard(state, before.length), details);
        assertEquals(scoreBefore, state.getScore(), details);
        assertEquals(movesBefore, state.getMovesUsed(), details);

        boolean changed = state.applyMove(move);
        int[][] after = readBoard(state, before.length);

        assertEquals(expectedChange, changed, details);
        assertBoardEquals(expected, after, details);
        assertEquals(sum(before), sum(after), details);
        assertTrue(tileCount(after) <= tileCount(before), details);
        assertEquals(scoreBefore + potential(after) - potential(before), (long) state.getScore(), details);
        assertCompacted(after, move, details);
        return changed;
    }

    private int[][] referenceMove(int[][] board, Move move) {
        int size = board.length;
        int[][] result = new int[size][size];
        for (int line = 0; line < size; line++) {
            List<Integer> tiles = new ArrayList<>();
            for (int offset = 0; offset < size; offset++) {
                Position position = position(move, line, offset, size);
                int value = board[position.x()][position.y()];
                if (value != 0) {
                    tiles.add(value);
                }
            }
            List<Integer> merged = mergePairs(tiles);
            for (int offset = 0; offset < merged.size(); offset++) {
                Position position = position(move, line, offset, size);
                result[position.x()][position.y()] = merged.get(offset);
            }
        }
        return result;
    }

    private List<Integer> mergePairs(List<Integer> tiles) {
        List<Integer> merged = new ArrayList<>();
        int index = 0;
        while (index < tiles.size()) {
            int value = tiles.get(index);
            if (index + 1 < tiles.size() && value == tiles.get(index + 1)) {
                merged.add(value * 2);
                index += 2;
            } else {
                merged.add(value);
                index++;
            }
        }
        return merged;
    }

    private void assertCompacted(int[][] board, Move move, String details) {
        for (int line = 0; line < board.length; line++) {
            boolean emptySeen = false;
            for (int offset = 0; offset < board.length; offset++) {
                Position position = position(move, line, offset, board.length);
                int value = board[position.x()][position.y()];
                assertTrue(!emptySeen || value == 0, details);
                assertTrue(value == 0 || (value & (value - 1)) == 0, details);
                emptySeen |= value == 0;
            }
        }
    }

    private Position position(Move move, int line, int offset, int size) {
        return switch (move) {
            case LEFT -> new Position(line, offset);
            case RIGHT -> new Position(line, size - 1 - offset);
            case UP -> new Position(offset, line);
            case DOWN -> new Position(size - 1 - offset, line);
        };
    }

    private int[][] randomBoard(Random random, int size) {
        int[][] board = new int[size][size];
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                board[row][column] = VALUES[random.nextInt(VALUES.length)];
            }
        }
        return board;
    }

    private GameState createState(int[][] board) {
        GameState state = new GameState(board.length, 100);
        for (int row = 0; row < board.length; row++) {
            for (int column = 0; column < board.length; column++) {
                if (board[row][column] != 0) {
                    state.placeTitle(new Position(row, column), board[row][column]);
                }
            }
        }
        return state;
    }

    private int[][] readBoard(GameState state, int size) {
        int[][] board = new int[size][size];
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                board[row][column] = state.getTileValue(new Position(row, column));
            }
        }
        return board;
    }

    private void assertBoardEquals(int[][] expected, int[][] actual, String details) {
        for (int row = 0; row < expected.length; row++) {
            assertArrayEquals(expected[row], actual[row], details + ", строка=" + row);
        }
    }

    private long sum(int[][] board) {
        return Arrays.stream(board).flatMapToInt(Arrays::stream).asLongStream().sum();
    }

    private long tileCount(int[][] board) {
        return Arrays.stream(board)
                .flatMapToInt(Arrays::stream)
                .filter(value -> value != 0)
                .count();
    }

    private long potential(int[][] board) {
        return Arrays.stream(board)
                .flatMapToInt(Arrays::stream)
                .filter(value -> value != 0)
                .mapToLong(value -> (long) value * Integer.numberOfTrailingZeros(value))
                .sum();
    }
}
