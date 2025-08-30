package cars.engine;

import java.util.List;

import static cars.engine.Vector2.distance;

public final class World {
    private final Car current;
    private final List<Car> cars;
    private final Vector2 mousePos;
    private final Vector2 clickPos;
    private final double secs;

    public World(double secs, Car current, List<Car> cars, Vector2 mousePos, Vector2 clickPos) {
        this.current = current;
        this.cars = cars;
        this.mousePos = mousePos;
        this.clickPos = clickPos;
        this.secs = secs;
    }

    public Vector2 getMousePos() {
        return mousePos.clone();
    }

    public Vector2 getClickPos() {
        return clickPos == null ? null : clickPos.clone();
    }

    public List<Car> getNeighbors() {
        return this.getNeighbors(Integer.MAX_VALUE);
    }

    public List<Car> getNeighbors(int radius) {
        return this.cars.stream()
            .filter(c -> c != current)
            .filter(c -> distance(current.getPosition(), c.getPosition()) <= radius)
            .toList();
    }

    public double getSecs() {
        return secs;
    }
}
