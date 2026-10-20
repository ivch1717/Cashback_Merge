package academy.cashbackmerge.game;

import academy.cashbackmerge.config.CampaignConfig;
import java.util.List;

public record ReplayData(CampaignConfig config, long seed, List<Move> moves) {
    public ReplayData {
        moves = List.copyOf(moves);
    }
}
