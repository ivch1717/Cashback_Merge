package academy.cashbackmerge.mandatory.mr2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import academy.cashbackmerge.GameState;
import academy.cashbackmerge.Move;
import academy.cashbackmerge.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Обязательные тесты: правила слияния плиток. */
@DisplayName("MR2. Слияние плиток")
class MergeRulesTest {

    @ParameterizedTest
    @CsvSource({
        "'1,1,.,.', '2,.,.,.'",
        "'1,1,1,.', '2,1,.,.'",
        "'1,1,1,1', '2,2,.,.'",
        "'2,2,4,4', '4,8,.,.'",
    })
    @DisplayName("Сдвиг влево: {0} -> {1}")
    void rowShiftsLeftAndMerges(String before, String expected) {
        GameState state = new GameState(4, 100);
        String[] values = before.split(",");
        for (int column = 0; column < values.length; column++) {
            if (!values[column].equals(".")) {
                state.placeTitle(new Position(0, column), Integer.parseInt(values[column]));
            }
        }

        assertTrue(state.applyMove(Move.LEFT));

        String[] expectedValues = expected.split(",");
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 4; column++) {
                int value =
                        row == 0 && !expectedValues[column].equals(".") ? Integer.parseInt(expectedValues[column]) : 0;
                assertEquals(value, state.getTileValue(new Position(row, column)));
            }
        }
    }

    @Test
    @DisplayName("Одна плитка не сливается дважды за ход: [1,1,2,.] -> [2,2,.,.]")
    void tileDoesNotMergeTwiceInOneMove() {
        rowShiftsLeftAndMerges("1,1,2,.", "2,2,.,.");
    }

    @Test
    @DisplayName("Слияние работает во всех четырёх направлениях")
    void mergeWorksInAllDirections() {
        for (Move move : Move.values()) {
            GameState state = new GameState(4, 100);
            boolean horizontal = move == Move.LEFT || move == Move.RIGHT;
            for (int i = 0; i < 3; i++) {
                Position position = horizontal ? new Position(1, i) : new Position(i, 1);
                state.placeTitle(position, 1);
            }
            int[] expected = move == Move.LEFT || move == Move.UP ? new int[] {2, 1, 0, 0} : new int[] {0, 0, 1, 2};

            assertTrue(state.applyMove(move));

            for (int row = 0; row < 4; row++) {
                for (int column = 0; column < 4; column++) {
                    int value = horizontal ? (row == 1 ? expected[column] : 0) : (column == 1 ? expected[row] : 0);
                    assertEquals(value, state.getTileValue(new Position(row, column)), move.toString());
                }
            }
            assertEquals(2, state.getScore());
        }
    }

    @Test
    @DisplayName("Пустые клетки между одинаковыми плитками не мешают слиянию")
    void gapsDoNotPreventMerge() {
        rowShiftsLeftAndMerges(".,1,.,1", "2,.,.,.");
    }

    @Test
    @DisplayName("Разные плитки сдвигаются, но не сливаются")
    void differentTilesDoNotMerge() {
        rowShiftsLeftAndMerges(".,2,1,.", "2,1,.,.");
    }
}
