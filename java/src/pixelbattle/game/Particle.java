package pixelbattle.game;

import java.awt.Color;

public class Particle {
    public double x;
    public double y;
    public double vx;
    public double vy;
    public double life;
    public double maxLife;
    public Color color;
    public double size;

    public Particle(double x, double y, double vx, double vy, Color color, double life) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.life = life;
        this.maxLife = life;
        this.color = color;
        this.size = 3;
    }
}
