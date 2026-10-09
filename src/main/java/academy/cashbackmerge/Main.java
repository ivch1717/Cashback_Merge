package academy.cashbackmerge;

import java.io.IOException;
import java.nio.file.Path;

/** Точка входа в Cashback Merge. */
public final class Main {

    private static final long DEFAULT_SEED = 42L;

    private Main() {}

    public static void main(String[] args) throws IOException {
        long seed = args.length > 0 ? Long.parseLong(args[0]) : DEFAULT_SEED;
        Path configPath = Path.of(args.length > 1 ? args[1] : "config/campaign.example.json");
        CampaignConfig config = Load.loadConfig(configPath);

        GameSession session = new GameSession(config, seed);
        session.startGame();
        System.out.println(session.boardAsString());
    }
}
