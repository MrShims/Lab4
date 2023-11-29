package com.mrshim.inputservice;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;

class Vector {
    public double x, y;

    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector add(Vector v) {
        return new Vector(this.x + v.x, this.y + v.y);
    }

    public Vector subtract(Vector v) {
        return new Vector(this.x - v.x, this.y - v.y);
    }

    public Vector scale(double scaleFactor) {
        return new Vector(this.x * scaleFactor, this.y * scaleFactor);
    }

    public double distanceTo(Vector v) {
        double dx = this.x - v.x;
        double dy = this.y - v.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public Vector normalize() {
        double magnitude = Math.sqrt(x * x + y * y);
        return new Vector(x / magnitude, y / magnitude);
    }
}

class Planet {
    public LinkedList<Vector> path = new LinkedList<>();
    public Vector position;
    public Vector velocity;
    public double mass;
    public Color color;

    public Planet(Vector position, Vector velocity, double mass, Color color) {
        this.position = position;
        this.velocity = velocity;
        this.mass = mass;
        this.color = color;
    }

    public void updatePosition(Vector acceleration, double timestep) {
        velocity = velocity.add(acceleration.scale(timestep));
        position = position.add(velocity.scale(timestep));
        path.add(new Vector(position.x, position.y));
        if (path.size() > 1000) {
            path.removeFirst();
        }
    }
}

class SolarSystemPanel extends JPanel {
    private ArrayList<Planet> planets;

    public SolarSystemPanel(ArrayList<Planet> planets) {
        this.planets = planets;
        setPreferredSize(new Dimension(800, 800));
        setBackground(Color.BLACK);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        double scale = 1e-9;
        g.setColor(Color.YELLOW);
        g.fillOval(centerX - 10, centerY - 10, 20, 20);
        for (Planet p : planets) {
            int x = centerX + (int) (p.position.x * scale);
            int y = centerY + (int) (p.position.y * scale);
            g.setColor(p.color);
            g.fillOval(x - 5, y - 5, 10, 10);
            Point prevPoint = null;
            for (Vector v : p.path) {
                Point currentPoint = new Point(centerX + (int) (v.x * scale), centerY + (int) (v.y * scale));
                if (prevPoint != null) {
                    g.drawLine(prevPoint.x, prevPoint.y, currentPoint.x, currentPoint.y);
                }
                prevPoint = currentPoint;
            }

        }
    }
}

public class SolarSystemSimulator extends JFrame implements ActionListener {
    private SolarSystemPanel panel;
    private ArrayList<Planet> planets;
    private final double G = 6.67430e-11;  // Гравитационная постоянная
    private Timer timer;

    public SolarSystemSimulator() {
        planets = new ArrayList<>();
        planets.add(new Planet(new Vector(0, 0), new Vector(0, 0), 1.989e30, Color.YELLOW)); // Солнце
        planets.add(new Planet(new Vector(0, 5.79e10), new Vector(47.4e3, 0), 3.30e23, Color.GRAY)); // Меркурий
        planets.add(new Planet(new Vector(0, 1.082e11), new Vector(35.0e3, 0), 4.87e24, Color.ORANGE)); // Венера
        planets.add(new Planet(new Vector(0, 1.496e11), new Vector(29.78e3, 0), 5.972e24, Color.BLUE)); // Земля
        planets.add(new Planet(new Vector(0, 2.279e11), new Vector(24.077e3, 0), 6.39e23, Color.RED));

        panel = new SolarSystemPanel(planets);
        add(panel);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        timer = new Timer(100, this);
        timer.start();
    }

    public static void main(String[] args) {
        new SolarSystemSimulator();
    }

    public void actionPerformed(ActionEvent e) {
        for (Planet p1 : planets) {
            Vector totalAcceleration = new Vector(0, 0);
            for (Planet p2 : planets) {
                if (p1 != p2) {
                    Vector distance = p2.position.subtract(p1.position);
                    double dist = distance.distanceTo(new Vector(0, 0));
                    double forceMagnitude = (G * p1.mass * p2.mass) / (dist * dist);
                    Vector force = distance.normalize().scale(forceMagnitude);
                    Vector acceleration = force.scale(1 / p1.mass);
                    totalAcceleration = totalAcceleration.add(acceleration);
                }
            }
            p1.updatePosition(totalAcceleration, 86400); // шаг времени - один день
        }
        panel.repaint();
    }
}