package academy.cashbackmerge.game;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.config.RewardTier;
import academy.cashbackmerge.config.SpawnRule;
import academy.cashbackmerge.simulation.ResponseSession;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameSession {
    private final List<SpawnRule> rules;
    private final Random random;
    private final GameState state;
    private final List<RewardTier> rewardTable;
    private final int boardSize;

    public GameSession(CampaignConfig config, long seed) {
        random = new Random(seed);
        rules = config.spawnRules();
        state = new GameState(config.boardSize(), config.movesLimit());
        rewardTable = config.rewardTable();
        boardSize = config.boardSize();
    }

    /** Создаёт партию из начального поля, не сохраняя ссылку на переданный массив. */
    public GameSession(CampaignConfig config, long seed, int[][] initialBoard) {
        this(config, seed);
        if (initialBoard.length != config.boardSize()) {
            throw new IllegalArgumentException("Размер начального поля не совпадает с конфигом");
        }
        for (int row = 0; row < initialBoard.length; row++) {
            if (initialBoard[row].length != config.boardSize()) {
                throw new IllegalArgumentException("Размер начального поля не совпадает с конфигом");
            }
            for (int column = 0; column < initialBoard[row].length; column++) {
                int value = initialBoard[row][column];
                if (value != 0) {
                    state.placeTitle(new Position(row, column), value);
                }
            }
        }
    }

    public void startGame() {
        nextStep();
        nextStep();
    }

    private int getRandomValue() {
        int scale = 0;
        for (SpawnRule rule : rules) {
            scale = Math.max(scale, rule.probability().scale());
        }

        BigInteger bound = BigInteger.TEN.pow(scale);
        BigInteger ticket;
        do {
            ticket = new BigInteger(bound.bitLength(), random);
        } while (ticket.compareTo(bound) >= 0);

        BigInteger cumulative = BigInteger.ZERO;
        for (SpawnRule rule : rules) {
            BigInteger weight = rule.probability().movePointRight(scale).toBigIntegerExact();
            cumulative = cumulative.add(weight);
            if (ticket.compareTo(cumulative) < 0) {
                return rule.cashback();
            }
        }
        throw new IllegalStateException("Не удалось выбрать значение плитки");
    }

    public void nextStep() {
        List<Position> positions = state.getFreePosition();
        if (positions.isEmpty()) {
            throw new IllegalArgumentException("Нет свободных клеток");
        }
        state.placeTitle(positions.get(random.nextInt(positions.size())), getRandomValue());
    }

    public String boardAsString() {
        return state.toString();
    }

    public int getScore() {
        return state.getScore();
    }

    public int getMovesUsed() {
        return state.getMovesUsed();
    }

    public int getTileValue(Position position) {
        return state.getTileValue(position);
    }

    public void applyMove(Move move) {
        if (!state.applyMove(move)) {
            throw new IllegalArgumentException("Некорректный ход, ничего не сдвинулось");
        }
        nextStep();
        state.moves();
        boardAsString();
    }

    public ResponseSession getReward() {
        int maxCashback = 0;
        BigDecimal reward = BigDecimal.ZERO;
        int maxField = 0;
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                int value = state.getTileValue(new Position(i, j));
                maxField = Integer.max(maxField, value);
                for (RewardTier tier : rewardTable) {
                    if (tier.cashback() <= value && tier.cashback() > maxCashback) {
                        maxCashback = tier.cashback();
                        reward = tier.reward();
                    }
                }
            }
        }

        return new ResponseSession(maxCashback, reward, state.getScore(), maxField);
    }

    public List<Move> getAvailableMoves() {
        if (state.hasMovesRemaining()) {
            return new ArrayList<>();
        }
        List<Move> moves = new ArrayList<>();
        for (Move move : Move.values()) {
            if (state.canMove(move)) {
                moves.add(move);
            }
        }
        return moves;
    }

    public Move getMaxScoreMove(List<Move> moves) {
        if (moves.isEmpty()) {
            throw new IllegalArgumentException("Список ходов не должен быть пустым");
        }
        Move bestMove = moves.getFirst();
        int bestScore = -1;
        for (Move move : moves) {
            if (!state.canMove(move)) {
                continue;
            }
            GameState trial = new GameState(boardSize, 1);
            for (int row = 0; row < boardSize; row++) {
                for (int column = 0; column < boardSize; column++) {
                    Position position = new Position(row, column);
                    int value = state.getTileValue(position);
                    if (value != 0) {
                        trial.placeTitle(position, value);
                    }
                }
            }
            trial.applyMove(move);
            if (trial.getScore() > bestScore) {
                bestScore = trial.getScore();
                bestMove = move;
            }
        }
        if (bestScore < 0) {
            throw new IllegalArgumentException("В списке нет доступных ходов");
        }
        return bestMove;
    }
}
