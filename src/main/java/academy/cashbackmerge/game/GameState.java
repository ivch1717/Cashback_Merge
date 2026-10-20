package academy.cashbackmerge.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameState {
    private final int[][] board;
    private int movesUsed;
    private final int movesLimit;
    private final int boardSize;
    private int score;

    public GameState(int boardSize, int movesLimit) {
        if (movesLimit <= 0) {
            throw new IllegalArgumentException("Лимит ходов должен быть > 0");
        }

        if (boardSize <= 1) {
            throw new IllegalArgumentException("Размер поля должен быть больше 1");
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
                        .repeat(" ", cellWidth - cell.length())
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

    private int[] mergeLine(int[] line) {
        int l = 0;
        int i = 0;
        while (i < line.length) {
            if (l == i) {
                i++;
                continue;
            }
            if (line[i] != 0) {
                if (line[l] == 0) {
                    line[l] = line[i];
                    line[i] = 0;
                } else if (line[l] == line[i]) {
                    line[l] += line[i];
                    line[i] = 0;
                    score += line[l];
                    l++;
                } else {
                    l++;
                    continue;
                }
            }
            i++;
        }
        return line;
    }

    private int[] getLine(Move move, int i) {
        int[] oldList = new int[boardSize];
        for (int j = 0; j < boardSize; j++) {
            oldList[j] = switch (move) {
                case LEFT -> board[i][j];
                case RIGHT -> board[i][boardSize - 1 - j];
                case UP -> board[j][i];
                case DOWN -> board[boardSize - 1 - j][i];
            };
        }
        return oldList;
    }

    public boolean canMove(Move move) {
        if (hasMovesRemaining()) {
            return false;
        }
        for (int i = 0; i < boardSize; i++) {
            int[] line = getLine(move, i);
            for (int j = 0; j < line.length - 1; j++) {
                if (line[j + 1] != 0 && (line[j] == 0 || line[j] == line[j + 1])) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean applyMove(Move move) {
        if (movesUsed >= movesLimit) {
            throw new IllegalArgumentException("У вас больше нет ходов");
        }
        boolean isChange = false;
        for (int i = 0; i < boardSize; i++) {
            int[] oldList = getLine(move, i);

            int[] newList = mergeLine(oldList.clone());

            for (int j = 0; j < boardSize; j++) {
                if (newList[j] != oldList[j]) {
                    isChange = true;
                }
                switch (move) {
                    case LEFT -> board[i][j] = newList[j];
                    case RIGHT -> board[i][boardSize - 1 - j] = newList[j];
                    case UP -> board[j][i] = newList[j];
                    case DOWN -> board[boardSize - 1 - j][i] = newList[j];
                }
            }
        }

        return isChange;
    }

    public void moves() {
        if (movesUsed >= movesLimit) {
            throw new IllegalArgumentException("У вас больше нет ходов");
        }
        movesUsed += 1;
    }

    public int getScore() {
        return score;
    }

    public int getMovesUsed() {
        return movesUsed;
    }

    public boolean hasMovesRemaining() {
        return movesUsed >= movesLimit;
    }

    public int getTileValue(Position position) {
        return board[position.x()][position.y()];
    }
}
