package pixelbattle.game;

import java.util.Random;

public class Enemy {
    public double x, y;
    public double vx, vy;
    public double health;
    public double maxHealth;
    public double damage;
    public double speed;
    public EnemyType type;
    public boolean isDead;
    public boolean facingRight;
    public double attackCooldown;
    public double wanderTimer;
    public double wanderDir;
    public double jumpCooldown;
    public boolean onGround;
    public double hurtTimer;

    private static final Random random = new Random();

    public enum AIState {
        IDLE, WANDER, CHASE, ATTACK, RETREAT
    }

    public AIState aiState;

    public Enemy(EnemyType type, double x, double y, double health, double damage, double speed) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.vx = 0;
        this.vy = 0;
        this.health = health;
        this.maxHealth = health;
        this.damage = damage;
        this.speed = speed;
        this.isDead = false;
        this.facingRight = true;
        this.attackCooldown = 0;
        this.wanderTimer = 0;
        this.wanderDir = random.nextDouble() > 0.5 ? 1 : -1;
        this.jumpCooldown = 0;
        this.onGround = false;
        this.hurtTimer = 0;
        this.aiState = AIState.IDLE;
    }

    public void updateAI(double playerX, double playerY, double dt) {
        if (isDead) return;

        double dx = playerX - x;
        double dy = playerY - y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (attackCooldown > 0) attackCooldown -= dt;
        if (jumpCooldown > 0) jumpCooldown -= dt;
        if (hurtTimer > 0) hurtTimer -= dt;

        if (dist < GameConstants.ENEMY_ATTACK_RANGE) {
            aiState = AIState.ATTACK;
        } else if (dist < GameConstants.ENEMY_DETECT_RANGE) {
            aiState = AIState.CHASE;
        } else {
            wanderTimer -= dt;
            if (wanderTimer <= 0) {
                if (aiState == AIState.IDLE) {
                    aiState = AIState.WANDER;
                    wanderDir = random.nextDouble() > 0.5 ? 1 : -1;
                    wanderTimer = 2 + random.nextDouble() * 3;
                } else {
                    aiState = AIState.IDLE;
                    wanderTimer = 1 + random.nextDouble() * 2;
                }
            }
        }

        switch (aiState) {
            case IDLE:
                vx = 0;
                break;
            case WANDER:
                vx = wanderDir * speed * 0.5;
                facingRight = wanderDir > 0;
                break;
            case CHASE:
                double dir = dx > 0 ? 1 : -1;
                vx = dir * speed;
                facingRight = dir > 0;
                if (dy < -GameConstants.TILE_SIZE * 2 && onGround && jumpCooldown <= 0) {
                    vy = -8;
                    jumpCooldown = 1.0;
                    onGround = false;
                }
                break;
            case ATTACK:
                vx = 0;
                facingRight = dx > 0;
                break;
            case RETREAT:
                double retDir = dx > 0 ? -1 : 1;
                vx = retDir * speed * 0.7;
                facingRight = retDir > 0;
                break;
        }

        if (!onGround) {
            vy += 0.6;
            if (vy > 12) vy = 12;
        }

        x += vx;
        y += vy;
    }

    public void takeDamage(double amount) {
        health -= amount;
        hurtTimer = 0.2;
        if (health <= 0) {
            health = 0;
            isDead = true;
        }
    }

    public boolean canAttack() {
        return attackCooldown <= 0 && aiState == AIState.ATTACK;
    }

    public void doAttack() {
        attackCooldown = 1.0;
    }

    public int getWidth() {
        switch (type) {
            case SLIME: return 24;
            case SKELETON: return 24;
            case GOBLIN: return 28;
            default: return 24;
        }
    }

    public int getHeight() {
        switch (type) {
            case SLIME: return 20;
            case SKELETON: return 32;
            case GOBLIN: return 30;
            default: return 24;
        }
    }
}
