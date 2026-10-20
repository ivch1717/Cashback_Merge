package academy.cashbackmerge.simulation;

import academy.cashbackmerge.CampaignConfig;
import academy.cashbackmerge.game.GameSession;
import academy.cashbackmerge.game.Move;

import java.util.List;
import java.util.Random;

public class RandomPlayer {
    private final Random random;
    private final GameSession session;

    public RandomPlayer(CampaignConfig config, long seed){
        random = new Random(seed);
        session = new GameSession(config, seed);
        session.startGame();
    }

    public ResponseSession play(){
        while (true){
            List<Move> moves = session.getAvailableMoves();
            if (moves.isEmpty()){
                return session.getReward();
            }
            session.applyMove(moves.get(random.nextInt(moves.size())));
        }
    }
}
