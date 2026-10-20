package academy.cashbackmerge;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.game.GameSession;
import academy.cashbackmerge.game.Replay;
import academy.cashbackmerge.game.ReplayData;
import academy.cashbackmerge.simulation.Simulator;
import academy.cashbackmerge.simulation.SimulatorResponse;
import academy.cashbackmerge.simulation.Strategy;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import picocli.CommandLine;
import picocli.CommandLine.Option;

/** Точка входа в Cashback Merge. */
public final class Main {

    private static final long DEFAULT_SEED = 42L;
    private static final int DEFAULT_PLAYERS = 10000;
    private static final int REPLAY_ARGUMENT_COUNT = 3;

    private Main() {}

    public static void main(String[] args) throws IOException {
        if (args.length > 0 && args[0].equals("compare")) {
            compare(args);
            return;
        }
        if (args.length > 0 && args[0].equals("replay")) {
            replay(args);
            return;
        }
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

    private static void replay(String[] args) throws IOException {
        Path input = Path.of("replay.json");
        if (args.length > 1) {
            if (args.length != REPLAY_ARGUMENT_COUNT || !args[1].equals("--input")) {
                throw new IllegalArgumentException("Запуск: replay --input replay.json");
            }
            input = Path.of(args[2]);
        }
        ReplayData data = Load.loadReplay(input);
        Replay.replay(data.seed(), data.config(), data.moves());
    }

    private static void simulate(String[] args) throws IOException {
        SimulationOptions options = new SimulationOptions();
        new CommandLine(options).parseArgs(Arrays.copyOfRange(args, 1, args.length));

        CampaignConfig config = Load.loadConfig(options.config);
        Strategy strategy = Strategy.valueOf(options.strategy.replace('-', '_').toUpperCase(Locale.ROOT));
        SimulatorResponse report =
                new Simulator(options.seed).simulate(config, options.players, strategy, options.replayPlayer);
        Load.writeReport(options.output, report);
        System.out.println("Отчёт сохранён: " + options.output);
    }

    private static void compare(String[] args) throws IOException {
        SimulationOptions options = new SimulationOptions();
        new CommandLine(options).parseArgs(Arrays.copyOfRange(args, 1, args.length));
        Strategy strategy = Strategy.valueOf(options.strategy.replace('-', '_').toUpperCase(Locale.ROOT));
        Map<String, SimulatorResponse> reports =
                new Simulator(options.seed).compareCampaigns(options.configs, options.players, strategy);
        Load.writeComparisonReport(options.output, reports);
        System.out.println("Сравнение сохранено: " + options.output);
    }

    private static final class SimulationOptions {
        @Option(names = "--config")
        private Path config = Path.of("config/campaign.example.json");

        @Option(names = "--configs", split = ",")
        private List<Path> configs = List.of(
                Path.of("config/campaign.conservative.json"),
                Path.of("config/campaign.balanced.json"),
                Path.of("config/campaign.generous.json"));

        @Option(names = "--players")
        private int players = DEFAULT_PLAYERS;

        @Option(names = "--strategy")
        private String strategy = "random";

        @Option(names = "--seed")
        private long seed = DEFAULT_SEED;

        @Option(names = "--output")
        private Path output = Path.of("report.json");

        @Option(names = "--replay-player")
        private int replayPlayer = -1;
    }
}
