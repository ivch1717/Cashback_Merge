package academy.cashbackmerge.game;

import academy.cashbackmerge.config.CampaignConfig;
import java.util.List;

public final class Replay {
    private Replay() {}

    public static void replay(long seed, CampaignConfig config, List<Move> moves) {
        GameSession session = new GameSession(config, seed);
        session.startGame();
        System.out.println(session.boardAsString());
        for (Move m : moves) {
            session.applyMove(m);
            System.out.println(session.boardAsString());
        }
    }
}
