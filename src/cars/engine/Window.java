package cars.engine;

import cars.student.Setup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class Window extends JFrame {
    private List<Car> cars = new ArrayList<>();
    private Vector2 clickPos = null;
    private Vector2 mousePos = null;
    private static final Window instance = new Window();

    public static final Window getInstance() {
        return instance;
    }

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

    private void start() {
        createBufferStrategy(3);
        var gameLoop = new Thread(() -> {
            double prev = System.currentTimeMillis() - 1;
            try {
                System.out.println("Starting loop");
                var strategy = getBufferStrategy();
                while (true) {
                    double actual = System.currentTimeMillis();
                    double time = (actual - prev) / 1000.0;

                    var g2d = (Graphics2D) strategy.getDrawGraphics();
                    draw(g2d);
                    g2d.dispose();

                    update(time);
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

    private void update(double time) {
        for (var car : cars) {
            var world = new World(time, car, cars, mousePos, clickPos);
            car.update(world);
        }
    }

    public void draw(Graphics2D g2d) {
        g2d.setBackground(Color.WHITE);
        g2d.clearRect(0, 0, 1024, 768);
        g2d.translate(1024 / 2.0, 768 / 2.0);
        if (clickPos != null) {
            g2d.setColor(Color.RED);
            g2d.fillOval((int) clickPos.x-5, (int) clickPos.y-5, 10, 10);
        }
        for (var car : cars) {
            car.draw(g2d);
        }
        g2d.dispose();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> Window.getInstance().setVisible(true));
    }
}
