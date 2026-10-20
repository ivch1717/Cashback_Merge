package academy.cashbackmerge.simulation;

import java.util.Map;

public final class HtmlReport {

    private HtmlReport() {}

    public static String renderComparison(Map<String, SimulatorResponse> reports) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset=\"UTF-8\"><title>Сравнение кампаний</title></head><body>");
        html.append("<h1>Сравнение кампаний Cashback Merge</h1>");
        html.append("<table border=\"1\"><tr><th>Конфиг</th><th>Игроков</th><th>Лимит ходов</th>")
                .append("<th>Seed</th><th>Стратегия</th><th>Средний score</th><th>Максимальный score</th>")
                .append("<th>Доля максимальной награды</th><th>Статус</th></tr>");
        for (var entry : reports.entrySet()) {
            SimulatorResponse report = entry.getValue();
            String name =
                    entry.getKey().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
            html.append("<tr><td>")
                    .append(name)
                    .append("</td><td>")
                    .append(report.countPlayers())
                    .append("</td><td>")
                    .append(report.movesLimit())
                    .append("</td><td>")
                    .append(report.seed())
                    .append("</td><td>")
                    .append(report.strategy())
                    .append("</td><td>")
                    .append(report.averageScore())
                    .append("</td><td>")
                    .append(report.maxScore())
                    .append("</td><td>")
                    .append(report.maxRewardRate()
                            .movePointRight(2)
                            .stripTrailingZeros()
                            .toPlainString())
                    .append("%</td><td>")
                    .append(report.simulationStatus())
                    .append("</td></tr>");
        }
        html.append("</table></body></html>");
        return html.toString();
    }

    public static String render(SimulatorResponse report) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset=\"UTF-8\"><title>Отчёт</title></head><body>");
        html.append("<h1>Cashback Merge</h1>");
        html.append("<p>Игроков: ").append(report.countPlayers()).append("</p>");
        html.append("<p>Лимит ходов: ").append(report.movesLimit()).append("</p>");
        html.append("<p>Seed: ").append(report.seed()).append("</p>");
        html.append("<p>Стратегия: ").append(report.strategy()).append("</p>");
        html.append("<p>Средний score: ").append(report.averageScore()).append("</p>");
        html.append("<p>Максимальный score: ").append(report.maxScore()).append("</p>");
        html.append("<p>Доля максимальной награды: ")
                .append(report.maxRewardRate()
                        .movePointRight(2)
                        .stripTrailingZeros()
                        .toPlainString())
                .append("%</p>");
        html.append("<p>Статус: ").append(report.simulationStatus()).append("</p>");

        html.append("<h2>Максимальные плитки</h2>");
        html.append("<table border=\"1\"><tr><th>Плитка</th><th>Игроков</th></tr>");
        for (var entry : report.maxTileDistribution().entrySet()) {
            html.append("<tr><td>")
                    .append(entry.getKey())
                    .append("%</td><td>")
                    .append(entry.getValue())
                    .append("</td></tr>");
        }
        html.append("</table>");

        html.append("<h2>Награды</h2>");
        html.append("<table border=\"1\"><tr><th>Награда</th><th>Игроков</th></tr>");
        for (var entry : report.rewardDistribution().entrySet()) {
            html.append("<tr><td>")
                    .append(entry.getKey().toPlainString())
                    .append("</td><td>")
                    .append(entry.getValue())
                    .append("</td></tr>");
        }
        html.append("</table></body></html>");
        return html.toString();
    }
}
