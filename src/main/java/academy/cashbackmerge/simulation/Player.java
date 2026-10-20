package academy.cashbackmerge.simulation;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.game.GameSession;
import academy.cashbackmerge.game.Move;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Player {
    @SuppressWarnings("checkstyle:VisibilityModifier")
    final Random random;

    @SuppressWarnings("checkstyle:VisibilityModifier")
    final GameSession session;

    @SuppressWarnings("checkstyle:VisibilityModifier")
    final List<Move> moves;

    public Player(CampaignConfig config, long seed) {
        random = new Random(seed);
        session = new GameSession(config, seed);
        moves = new ArrayList<>();
        session.startGame();
    }

    public List<Move> getMoves() {
        return List.copyOf(moves);
    }

    public abstract ResponseSession play();
}
