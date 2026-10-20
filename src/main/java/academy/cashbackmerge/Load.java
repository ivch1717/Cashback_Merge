package academy.cashbackmerge;

import academy.cashbackmerge.simulation.SimulatorResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Load {

    private static final JsonMapper MAPPER = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES)
            .build();

    private Load() {}

    public static String readFile(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    public static CampaignConfig loadConfig(Path path) throws IOException {
        return MAPPER.readValue(readFile(path), CampaignConfig.class);
    }

    public static void writeFile(Path path, String content) throws IOException {
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    public static void writeReport(Path path, SimulatorResponse report) throws IOException {
        String json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(report);
        writeFile(path, json);
    }
}
