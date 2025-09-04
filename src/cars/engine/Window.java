package cars.engine;

import cars.student.Setup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class Window extends JFrame implements Runnable {
    private final List<Car> cars;
    private Vector2 clickPos = null;  // relative to center
    private Vector2 mousePos = null;  // absolute window coords

    private Window() {
        super("Steering behaviors");
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIgnoreRepaint(true);
        setResizable(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                start();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // click position relative to the centered origin
                clickPos = new Vector2(
                    e.getX() - getWidth() / 2.0,
                    e.getY() - getHeight() / 2.0
                );
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePos = new Vector2(e.getX(), e.getY());
            }
        });

        this.cars = new Setup().createCars();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new Window().setVisible(true));
    }

    private void start() {
        createBufferStrategy(3);
        final var gameLoop = new Thread(this, "game-loop");
        gameLoop.setDaemon(true);
        gameLoop.start();
    }

    @Override
    public void run() {
        double prev = System.currentTimeMillis() - 1;
        try {
            final var strategy = getBufferStrategy();
            while (true) {
                final double now = System.currentTimeMillis();
                final var g2d = (Graphics2D) strategy.getDrawGraphics();

                draw(g2d);
                update((now - prev) / 1000.0);

                g2d.dispose();
                Toolkit.getDefaultToolkit().sync(); // helps on some systems
                prev = now;
                strategy.show();

                Thread.sleep(1);
            }
        } catch (InterruptedException e) {
            System.err.println("Interrupted");
        }
        System.exit(0);
    }

    private void update(final double dt) {
        final var w = getWidth();
        final var h = getHeight();
        cars.forEach(car ->
            car.update(new World(dt, car, cars, mousePos, clickPos, w, h))
        );
    }

    private void draw(Graphics2D g2d) {
        // Quality hints
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Clear
        g2d.setBackground(new Color(220, 220, 220));
        g2d.clearRect(0, 0, getWidth(), getHeight());

        // Center the world origin
        g2d.translate(getWidth() / 2.0, getHeight() / 2.0);

        // Click marker (relative to center)
        if (clickPos != null) {
            g2d.setColor(Color.GRAY);
            g2d.fillOval((int) clickPos.x - 4, (int) clickPos.y - 4, 8, 8);
        }

        // Draw cars
        cars.forEach(car -> car.draw(g2d));
    }
}
