package academy.cashbackmerge.simulation;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.game.Move;
import java.util.List;

public class GreedyScorePlayer extends Player {
    public GreedyScorePlayer(CampaignConfig config, long seed) {
        super(config, seed);
    }

    @Override
    public ResponseSession play() {
        while (true) {
            List<Move> moves = super.session.getAvailableMoves();
            if (moves.isEmpty()) {
                return session.getReward();
            }
            Move move = session.getMaxScoreMove(moves);
            session.applyMove(move);
            super.moves.add(move);
        }
    }
}
