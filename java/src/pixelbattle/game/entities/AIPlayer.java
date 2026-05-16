package pixelbattle.game.entities;

import pixelbattle.game.*;
import pixelbattle.game.engine.Game;
import pixelbattle.game.terrain.TerrainGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIPlayer {
    public double x, y;
    public double vx, vy;
    public double health;
    public double maxHealth;
    public boolean isDead;
    public boolean onGround;
    public boolean facingRight;
    public List<Item> inventory;
    public double attackCooldown;
    public double mineCooldown;
    public double decisionTimer;
    public double hurtTimer;
    public double invincibleTimer;

    public enum AIAction {
        IDLE, WANDER, MINE, CHASE, ATTACK, FLEE, BUILD
    }

    public AIAction currentAction;
    private Random random = new Random();
    private int targetTx;
    private int targetTy;
    private double targetX;
    private double moveDir;

    public AIPlayer(double x, double y) {
        this.x = x;
        this.y = y;
        this.vx = 0;
        this.vy = 0;
        this.health = 100;
        this.maxHealth = 100;
        this.isDead = false;
        this.onGround = false;
        this.facingRight = true;
        this.inventory = new ArrayList<>();
        this.attackCooldown = 0;
        this.mineCooldown = 0;
        this.decisionTimer = 0;
        this.hurtTimer = 0;
        this.invincibleTimer = 0;
        this.currentAction = AIAction.IDLE;
        this.targetTx = -1;
        this.targetTy = -1;
        this.targetX = x;
        this.moveDir = 1;

        inventory.add(new Item(ItemType.WOODEN_PICKAXE, 1));
        inventory.add(new Item(ItemType.WOODEN_SWORD, 1));
    }

    public void updateAI(double playerX, double playerY, Game game, double dt) {
        if (isDead) return;

        if (attackCooldown > 0) attackCooldown -= dt;
        if (mineCooldown > 0) mineCooldown -= dt;
        if (hurtTimer > 0) hurtTimer -= dt;
        if (invincibleTimer > 0) invincibleTimer -= dt;
        decisionTimer -= dt;

        double dx = playerX - x;
        double dist = Math.abs(dx);

        if (decisionTimer <= 0) {
            decisionTimer = 0.5 + random.nextDouble() * 1.0;

            if (health < 30) {
                currentAction = AIAction.FLEE;
            } else if (dist < 50) {
                currentAction = AIAction.ATTACK;
            } else if (dist < GameConstants.AI_PLAYER_DETECT_RANGE) {
                currentAction = AIAction.CHASE;
            } else {
                double r = random.nextDouble();
                if (r < 0.4) {
                    currentAction = AIAction.MINE;
                    findNearestMineableBlock(game);
                } else if (r < 0.7) {
                    currentAction = AIAction.IDLE;
                } else {
                    currentAction = AIAction.WANDER;
                    moveDir = random.nextDouble() > 0.5 ? 1 : -1;
                }
            }
        }

        switch (currentAction) {
            case IDLE:
                vx = 0;
                break;
            case WANDER:
                vx = moveDir * 2;
                facingRight = moveDir > 0;
                break;
            case MINE:
                if (targetTx >= 0 && targetTy >= 0) {
                    double blockWorldX = targetTx * GameConstants.TILE_SIZE;
                    double bDx = blockWorldX - x;
                    if (Math.abs(bDx) > 40) {
                        vx = bDx > 0 ? 2 : -2;
                        facingRight = bDx > 0;
                    } else {
                        vx = 0;
                        if (mineCooldown <= 0) {
                            TerrainGenerator tgen = game.getTerrainGen();
                            BlockType type = tgen.getBlockType(targetTx, targetTy);
                            GameConstants.BlockProps props = GameConstants.BLOCK_PROPERTIES.get(type);
                            if (props != null && props.drop != null) {
                                addItem(props.drop, 1);
                            }
                            tgen.setBlockType(targetTx, targetTy, BlockType.AIR);
                            findNearestMineableBlock(game);
                            mineCooldown = 0.5;
                        }
                    }
                } else {
                    currentAction = AIAction.IDLE;
                }
                break;
            case CHASE:
                vx = dx > 0 ? 3 : -3;
                facingRight = dx > 0;
                if (onGround) {
                    int aheadTx = (int)((facingRight ? x + 32 : x - 16) / GameConstants.TILE_SIZE);
                    int feetTy = (int)((y + 32) / GameConstants.TILE_SIZE);
                    if (game.isBlockSolid(aheadTx, feetTy)) {
                        vy = -8;
                        onGround = false;
                    }
                }
                break;
            case ATTACK:
                vx = 0;
                facingRight = dx > 0;
                break;
            case FLEE:
                vx = dx > 0 ? -3 : 3;
                facingRight = vx > 0;
                break;
        }

        if (!onGround) {
            vy += 0.6;
            if (vy > 12) vy = 12;
        }

        x += vx;
        y += vy;
    }

    private void findNearestMineableBlock(Game game) {
        TerrainGenerator tgen = game.getTerrainGen();
        int centerTx = (int)(x / GameConstants.TILE_SIZE);
        int centerTy = (int)(y / GameConstants.TILE_SIZE);
        double minDist = 200;
        targetTx = -1;
        targetTy = -1;

        for (int r = 1; r <= 12; r++) {
            for (int dtx = -r; dtx <= r; dtx++) {
                for (int dty = -r; dty <= r; dty++) {
                    if (Math.abs(dtx) != r && Math.abs(dty) != r) continue;
                    int tx = centerTx + dtx;
                    int ty = centerTy + dty;
                    BlockType type = tgen.getBlockType(tx, ty);
                    if (type == BlockType.DIRT || type == BlockType.STONE ||
                        type == BlockType.WOOD || type == BlockType.IRON_ORE ||
                        type == BlockType.GOLD_ORE) {
                        double d = Math.abs(dtx) + Math.abs(dty);
                        if (d < minDist) {
                            minDist = d;
                            targetTx = tx;
                            targetTy = ty;
                        }
                    }
                }
            }
            if (targetTx >= 0) return;
        }
    }

    public boolean canAttackPlayer() {
        return attackCooldown <= 0 && currentAction == AIAction.ATTACK;
    }

    public void doAttack() {
        attackCooldown = 1.0;
    }

    public void takeDamage(double amount) {
        if (invincibleTimer > 0) return;
        health -= amount;
        hurtTimer = 0.2;
        invincibleTimer = 0.3;
        if (health <= 0) {
            health = 0;
            isDead = true;
        }
    }

    public void addItem(ItemType type, int count) {
        for (Item item : inventory) {
            if (item.type == type) {
                item.count += count;
                return;
            }
        }
        inventory.add(new Item(type, count));
    }

    public double getAttackPower() {
        return 5.0;
    }
}
