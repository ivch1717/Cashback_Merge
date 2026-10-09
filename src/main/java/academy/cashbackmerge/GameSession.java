package academy.cashbackmerge;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;

public class GameSession {
    private List<SpawnRule> rules;
    private Random random;
    private GameState state;

    public GameSession(CampaignConfig config, long seed) {
        random = new Random(seed);
        rules = config.spawnRules();
        state = new GameState(config.boardSize(), config.movesLimit());
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
}
