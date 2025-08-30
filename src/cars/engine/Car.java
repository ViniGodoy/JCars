package cars.engine;

import static cars.engine.Vector2.*;
import java.awt.*;

public abstract class Car implements Cloneable {
    private final Color color;
    private Vector2 position;
    private Vector2 velocity;

    private final double mass;
    private final double maxForce;
    private final double maxSpeed;

    public Car(Color color, Vector2 position, double orientation, double mass, double maxForce, double maxSpeed) {
        this.color = color;
        this.position = position;
        this.velocity = Vector2.byAngle(orientation);
        this.mass = mass;
        this.maxForce = maxForce;
        this.maxSpeed = maxSpeed;
    }

    public Car(Color color, Vector2 position, double orientation, double mass) {
        this(color, position, orientation, mass, 350, 500);
    }

    public Car(Color color, Vector2 position, double orientation) {
        this(color, position, orientation, 1, 350, 500);
    }

    public Vector2 getPosition() {
        return position.clone();
    }

    public Vector2 getVelocity() {
        return velocity.clone();
    }

    public Vector2 getDirection() {
        return velocity.isZero() ?
            Vector2.byAngle(0) : Vector2.normalize(velocity);
    }

    public abstract Vector2 calculateSteering(World world);

    void update(World world) {
        //Super simplified pysics model from Reynolds modified to take the velocity into account
        final var steeringForce = truncate(calculateSteering(world), maxForce).multiply(world.getSecs());
        final var acceleration = divide(steeringForce, mass);
        velocity = truncate(add(velocity, acceleration), maxSpeed);
        position = multiply(velocity, world.getSecs()).add(position);

        //Circula pela tela
        final var w = 1024.0 / 2.0;
        final var h = 768.0 / 2.0f;
        if (position.x < -(w+20)) position.x = w;
        if (position.x > w+20) position.x = -w;
        if (position.y < -(h+20)) position.y = h;
        if (position.y > h+20) position. y = -h;
    }

    void draw(Graphics2D canvas) {
        final var g2d = (Graphics2D) canvas.create();
            g2d.translate(position.x, position.y);
            g2d.rotate(velocity.getAngle());

            g2d.setColor(this.color);
            g2d.fillRect(-20, -10, 40, 20);
            g2d.setColor(Color.YELLOW);
            g2d.fillRect(16, -7, 2, 4);
            g2d.fillRect(16,  3, 2, 4);
        g2d.dispose();
    }

    @Override
    public Car clone() {
        try {
            final var other = (Car) super.clone();
            other.position = position.clone();
            other.velocity = velocity.clone();
            return other;
        } catch (CloneNotSupportedException ignored) {
            return null;
        }
    }
}
