package academy.cashbackmerge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameState {
    private static final int BOARD_SIZE = 4;

    private int[][] board;
    private int movesUsed;
    private int movesLimit;
    private int boardSize;

    public GameState(int boardSize, int movesLimit) {
        if (movesLimit <= 0) {
            throw new IllegalArgumentException("Лимит ходов должен быть > 0");
        }

        if (boardSize != BOARD_SIZE) {
            throw new IllegalArgumentException("Размер поля должен быть 4");
        }

        movesUsed = 0;
        this.movesLimit = movesLimit;
        board = new int[boardSize][boardSize];
        this.boardSize = boardSize;
    }

    public void placeTitle(Position position, int value) {
        if (position.x() >= boardSize || position.y() >= boardSize) {
            throw new IllegalArgumentException("Координаты выходят за размер поля");
        }

        if (board[position.x()][position.y()] != 0) {
            throw new IllegalArgumentException("Клетка уже занята");
        }

        if (value <= 0) {
            throw new IllegalArgumentException("Значение должно быть положительным");
        }

        if (movesUsed >= movesLimit) {
            throw new IllegalArgumentException("У вас больше нет ходов");
        }

        board[position.x()][position.y()] = value;
    }

    public List<Position> getFreePosition() {
        List<Position> res = new ArrayList<>();
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j] == 0) {
                    res.add(new Position(i, j));
                }
            }
        }
        return res;
    }

    @Override
    public String toString() {
        int cellWidth = "32%".length();
        for (int[] row : board) {
            for (int value : row) {
                if (value != 0) {
                    cellWidth = Math.max(cellWidth, Integer.toString(value).length() + 1);
                }
            }
        }

        StringBuilder result = new StringBuilder();
        String horizontal = "─".repeat(cellWidth + 2);
        String newline = System.lineSeparator();
        result.append('┌')
                .append(String.join("┬", Collections.nCopies(board[0].length, horizontal)))
                .append('┐')
                .append(newline);
        for (int row = 0; row < board.length; row++) {
            if (row > 0) {
                result.append('├')
                        .append(String.join("┼", Collections.nCopies(board[row].length, horizontal)))
                        .append('┤')
                        .append(newline);
            }
            result.append('│');
            for (int value : board[row]) {
                String cell = value == 0 ? "" : value + "%";
                result.append(' ')
                        .append(" ".repeat(cellWidth - cell.length()))
                        .append(cell)
                        .append(" │");
            }
            result.append(newline);
        }
        result.append('└')
                .append(String.join("┴", Collections.nCopies(board[0].length, horizontal)))
                .append('┘');
        return result.toString();
    }
}
