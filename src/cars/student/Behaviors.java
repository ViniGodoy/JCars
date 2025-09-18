package cars.student;

import cars.engine.Car;
import cars.engine.Vector2;

import java.util.random.RandomGenerator;

import static cars.engine.Vector2.*;
import static java.lang.Math.acos;
import static java.lang.Math.toRadians;

/**
 * Gabarito com todos os steering behaviors implementados
 */
public class Behaviors {
    /**
     * Move-se em direção ao alvo, a máxima velocidade possível.
     * @param car Veículo que irá se mover
     * @param target Alvo.
     * @return Steering force
     */
    public static Vector2 seek(Car car, Vector2 target) {
        var desiredVelocity = subtract(target, car.getPosition())
                .resize(car.getMaxSpeed());

        return subtract(desiredVelocity, car.getVelocity());
    }

    /**
     * Foge da direção do alvo o mais rápido que puder, desde que este esteja no raio definido pela panicDistance..
     * @param car Veículo que irá fugir
     * @param target Alvo.
     * @param panicDistance Raio em que a fuga ocorre.
     * @return Steering force
     */
    public static Vector2 flee(Car car, Vector2 target, double panicDistance) {
        var desiredVelocity = subtract(car.getPosition(), target);
        var size = desiredVelocity.size();
        if (size > panicDistance) {
            return vec2();
        }

        //Não precisamos renormalizar com resize, já que já calculamos size
        desiredVelocity.multiply(car.getMaxForce() / size);
        return subtract(desiredVelocity, car.getVelocity());
    }

    /**
     * Foge da direção do alvo o mais rápido que puder.
     * @param car Veículo que irá fugir
     * @param target Alvo.
     * @return Steering force
     *
     * @see #flee(Car, Vector2, double)
     */
    public static Vector2 flee(Car car, Vector2 target) {
        return flee(car, target, Double.MAX_VALUE);
    }

    /**
     * Faz com que o carro desacelere até o alvo
     * @param car Quem realizará o arrive
     * @param target Destino
     * @param deceleration Fator de desaceleração
     * @param stopDistance Distância em que considerará que chegou.
     * @return Steering force
     */
    public static Vector2 arrive(Car car, Vector2 target, double deceleration, double stopDistance) {
        var toTarget = subtract(target, car.getPosition());
        double dist = toTarget.size();
        if (dist < stopDistance) {
            return vec2();
        }

        double speed = Math.min(dist / deceleration, car.getMaxSpeed());
        var desiredVelocity = toTarget.multiply(speed / dist);
        return subtract(desiredVelocity, car.getVelocity());
    }

    /**
     * Faz com que o carro desacelere até o alvo. Considera stopDistance como 5.
     * @param car Quem realizará o arrive
     * @param target Destino
     * @param deceleration Fator de desaceleração
     * @return Steering force
     */
    public static Vector2 arrive(Car car, Vector2 target, double deceleration) {
        return arrive(car, target, deceleration, 5);
    }

    /**
     * Faz com que o carro desacelere até o alvo. Considera stopDistance=5 e deceleration=1.
     * @param car Quem realizará o arrive
     * @param target Destino
     * @return Steering force
     */
    public static Vector2 arrive(Car car, Vector2 target) {
        return arrive(car, target,1);
    }

    /**
     * Tenta interceptar outro veículo, usando para isso sua posição futura.

     * @param pursuer Perseguidor
     * @param evader Veículo fugitivo
     * @return A steering force do perseguidor.
     */
    public static Vector2 pursuit(Car pursuer, Car evader) {
        var toEvader = subtract(evader.getPosition(), pursuer.getPosition());

        // Se o fugitivo está diretamente a frente vindo em nossa direção
        // Podemos só fazer um seek para cima dele
        var isAhead = toEvader.dot(pursuer.getDirection()) > 0;
        var isFacing = pursuer.getDirection().dot(evader.getDirection()) < acos(toRadians(18));
        if (isAhead && isFacing) {
            return seek(pursuer, evader.getPosition());
        }

        // Se ele está indo para outra direção, estimamos sua posição futura
        var lookAheadTime = toEvader.size() / (pursuer.getMaxSpeed() * evader.getSpeed());
        var futurePosition = add(evader.getPosition(), multiply(evader.getVelocity(), lookAheadTime));
        return seek(pursuer, futurePosition);
    }

    /**
     * Direciona o carro para um alvo posicionado a em um círculo a sua frente.
     * O alvo se mexe alguns graus sobre esse círculo para a esquerda ou direita a cada frame (jitter).
     */
    public static class Wander {
        private static final RandomGenerator RND = RandomGenerator.getDefault();
        private final Car car;
        private final double distance;
        private final double radius;
        private final double jitter;
        private double angle;

        /**
         * Calculates the wander steering force.
         *
         * @param car The car to wander
         * @param distance Wander distance
         * @param radius Wander radius
         * @param jitter Maximum target angle jitter, in degrees
         */
        public Wander(Car car, double distance, double radius, double jitter) {
            this.car = car;
            this.distance = distance;
            this.radius = Math.abs(radius);
            this.jitter = Math.abs(jitter);
            this.angle = RND.nextDouble(0, 2 * Math.PI);
        }

        /**
         * Calculates the wander steering force, with a jitter of 15 degrees.
         *
         * @param car The car to wander
         * @param distance Wander distance
         * @param radius Wander radius
         */
        public Wander(Car car, double distance, double radius) {
            this(car, distance, radius, 15);
        }

        /**
         * Calculates the wander steering force, with a 90 pixel radius and a jitter of 15 degrees.
         *
         * @param car The car to wander
         * @param distance Wander distance
         */
        public Wander(Car car, double distance) {
            this(car, distance, 90);
        }

        /**
         * Calculates the wander steering force, with a 120 pixel distance, 90 pixel radius and a jitter of 15 degrees.
         *
         * @param car The car to wander
         */
        public Wander(Car car) {
            this(car, 120);
        }

        /**
         * @return  the moving target. Randomly moves the target in every call.
         */
        public Vector2 target() {
            angle += Math.toRadians(RND.nextDouble(-jitter, jitter));
            return add(car.getPosition(), add(car.getDirection().multiply(distance), byAngleSize(angle, radius)));
        }

        /**
         * @return The calculated force, which is basically a seek in the target direction.
         * @see #target()
         * @see Behaviors#seek(Car, Vector2)
         */
        public Vector2 force() {
            return seek(car, target());
        }
    }
}
