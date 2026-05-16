package pixelbattle.game;

public class Projectile {
    public double x;
    public double y;
    public double vx;
    public double vy;
    public double damage;
    public ProjectileType type;

    public Projectile(double x, double y, double vx, double vy, double damage, ProjectileType type) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.damage = damage;
        this.type = type;
    }
}
