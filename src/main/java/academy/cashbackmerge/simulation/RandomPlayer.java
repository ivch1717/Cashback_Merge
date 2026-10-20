package academy.cashbackmerge.simulation;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.game.Move;
import java.util.List;

public class RandomPlayer extends Player {

    public RandomPlayer(CampaignConfig config, long seed) {
        super(config, seed);
    }

    @Override
    public ResponseSession play() {
        while (true) {
            List<Move> moves = super.session.getAvailableMoves();
            if (moves.isEmpty()) {
                return session.getReward();
            }
            Move move = moves.get(random.nextInt(moves.size()));
            session.applyMove(move);
            super.moves.add(move);
        }
    }
}
