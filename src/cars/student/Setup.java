package cars.student;

import cars.engine.Car;
import cars.engine.Vector2;
import cars.engine.World;

import java.awt.*;
import java.util.List;

import static cars.engine.Vector2.vec2;
import static cars.student.Behaviors.*;

public class Setup {
    /**
     * Retorne uma lista com todos os carros que serão desenhados no exercício.
     */
    public List<Car> createCars() {
        // Wanders around. Ignores the pursuer
        var wanderer = new Car(Color.ORANGE, vec2()) {
            private final Wander wander = new Wander(this);

            @Override
            public Vector2 calculateSteering(World world) {
                return wander.force();
            }
        };

        // Pursuit the wanderer
        var pursuer = new Car(Color.RED, vec2(200, 200)) {
            @Override
            public Vector2 calculateSteering(World world) {
                return pursuit(this, wanderer);
            }
        };

        // Approaches the mouse click.
        var approacher = new Car(Color.BLUE, vec2(0, 0)) {
            @Override
            public Vector2 calculateSteering(World world) {
                if (world.getClickPos() == null) {
                    return vec2();
                }
                return arrive(this, world.getClickPos());
            }
        };

        // Flees from all cars closer than 300 pixels, otherwise wanders
        var coward = new Car(Color.GREEN, vec2(200, 200), 0, 1, 350, 200) {
            private final Wander wander = new Wander(this);

            @Override
            public Vector2 calculateSteering(World world) {
                var force = vec2();
                for (var car : world.getNeighbors(300)) {
                    force.add(flee(this, car.getPosition()));
                }
                return force.size() > 10 ? force : wander.force();
            }
        };


        return List.of(wanderer, pursuer, coward, approacher);
    }
}
