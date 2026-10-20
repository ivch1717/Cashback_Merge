package academy.cashbackmerge.game;

public record Position(int x, int y) {
    public Position {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Координаты должны быть неотрицательными");
        }
    }
}
