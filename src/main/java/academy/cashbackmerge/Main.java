package academy.cashbackmerge;

import academy.cashbackmerge.game.GameSession;
import academy.cashbackmerge.simulation.Simulator;
import academy.cashbackmerge.simulation.SimulatorResponse;
import academy.cashbackmerge.simulation.Strategy;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import picocli.CommandLine;
import picocli.CommandLine.Option;

/** Точка входа в Cashback Merge. */
public final class Main {

    private static final long DEFAULT_SEED = 42L;
    private static final int DEFAULT_PLAYERS = 10000;

    private Main() {}

    public static void main(String[] args) throws IOException {
        if (args.length > 0 && args[0].equals("simulate")) {
            simulate(args);
            return;
        }
        long seed = args.length > 0 ? Long.parseLong(args[0]) : DEFAULT_SEED;
        Path configPath = Path.of(args.length > 1 ? args[1] : "config/campaign.example.json");
        CampaignConfig config = Load.loadConfig(configPath);

        GameSession session = new GameSession(config, seed);
        session.startGame();
        System.out.println(session.boardAsString());
    }

    private static void simulate(String[] args) throws IOException {
        SimulationOptions options = new SimulationOptions();
        new CommandLine(options).parseArgs(Arrays.copyOfRange(args, 1, args.length));

        CampaignConfig config = Load.loadConfig(options.config);
        Strategy strategy = Strategy.valueOf(options.strategy.replace('-', '_').toUpperCase(Locale.ROOT));
        SimulatorResponse report = new Simulator(options.seed).simulate(config, options.players, strategy);
        Load.writeReport(options.output, report);
        System.out.println("Отчёт сохранён: " + options.output);
    }

    private static final class SimulationOptions {
        @Option(names = "--config")
        private Path config = Path.of("config/campaign.example.json");

        @Option(names = "--players")
        private int players = DEFAULT_PLAYERS;

        @Option(names = "--strategy")
        private String strategy = "random";

        @Option(names = "--seed")
        private long seed = DEFAULT_SEED;

        @Option(names = "--output")
        private Path output = Path.of("report.json");
    }
}
