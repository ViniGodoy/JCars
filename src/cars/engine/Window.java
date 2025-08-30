package cars.engine;

import cars.student.Setup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class Window extends JFrame {
    private static final Window instance = new Window();
    private final List<Car> cars;
    private Vector2 clickPos = null;
    private Vector2 mousePos = null;

    private Window() {
        super("Steering behaviors");
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIgnoreRepaint(true);
        setResizable(false);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                start();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
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

    public static Window getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> Window.getInstance().setVisible(true));
    }

    private void start() {
        createBufferStrategy(3);
        var gameLoop = new Thread(() -> {
            double prev = System.currentTimeMillis() - 1;
            try {
                System.out.println("Starting loop");
                var strategy = getBufferStrategy();
                while (true) {
                    double actual = System.currentTimeMillis();
                    var g2d = (Graphics2D) strategy.getDrawGraphics();

                    draw(g2d);
                    update((actual - prev) / 1000.0);

                    g2d.dispose();
                    Thread.sleep(1);
                    prev = actual;
                    strategy.show();
                }
            } catch (InterruptedException e) {
                System.err.println("Interrupted");
            }
            System.exit(0);
        }, "game-loop");
        gameLoop.setDaemon(true);
        gameLoop.start();
    }

    public void update(final double time) {
        cars.forEach(car -> car.update(new World(time, car, cars, mousePos, clickPos)));
    }

    public void draw(Graphics2D g2d) {
        // Setup for quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setBackground(new Color(220, 220, 220));
        g2d.clearRect(0, 0, getWidth(), getHeight());
        g2d.translate(getWidth() / 2.0, getHeight() / 2.0);
        if (clickPos != null) {
            g2d.setColor(Color.GRAY);
            g2d.fillOval((int) clickPos.x - 4, (int) clickPos.y - 4, 8, 8);
        }
        cars.forEach(car -> car.draw(g2d));
        g2d.dispose();
    }
}
