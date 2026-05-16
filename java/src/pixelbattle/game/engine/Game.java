package pixelbattle.game.engine;

import pixelbattle.game.*;
import pixelbattle.game.entities.Player;
import pixelbattle.game.entities.AIPlayer;
import pixelbattle.game.terrain.TerrainGenerator;
import java.awt.Color;
import java.util.*;

public class Game {
    public enum GameMode { SURVIVAL, PVE, CREATIVE }
    public enum GameState { PLAYING, PAUSED, DEAD, WON }

    private GameMode gameMode;
    private GameState gameState;
    private Player player;
    private AIPlayer aiPlayer;
    private List<Enemy> enemies;
    private List<Projectile> projectiles;
    private List<Particle> particles;
    private Physics physics;
    private TerrainGenerator terrainGen;
    private Random random;

    private int worldHeightTiles;

    private double dayTime;
    private double enemySpawnTimer;
    private double gameTime;
    private int score;

    private int miningTargetTx;
    private int miningTargetTy;
    private double miningProgress;

    private double respawnX;
    private double respawnY;

    private boolean hookCharging;
    private double hookChargeTime;
    private double hookAimAngle;
    private Projectile activeHook;
    private boolean hookPulling;
    private double hookAnchorX;
    private double hookAnchorY;

    private Set<String> dismissedHints;

    public Game(GameMode mode) {
        this.gameMode = mode;
        this.gameState = GameState.PLAYING;
        this.physics = new Physics();
        this.random = new Random();
        this.enemies = new ArrayList<>();
        this.projectiles = new ArrayList<>();
        this.particles = new ArrayList<>();
        this.dayTime = 0;
        this.enemySpawnTimer = 0;
        this.gameTime = 0;
        this.score = 0;
        this.miningTargetTx = -1;
        this.miningTargetTy = -1;
        this.miningProgress = 0;
        this.hookCharging = false;
        this.hookChargeTime = 0;
        this.hookAimAngle = 0;
        this.activeHook = null;
        this.hookPulling = false;
        this.dismissedHints = new HashSet<>();

        long seed = System.currentTimeMillis();
        terrainGen = new TerrainGenerator(seed);
        int spawnX = 1600;
        terrainGen.generateInitial(spawnX);
        worldHeightTiles = terrainGen.getWorldHeightTiles();

        terrainGen.ensureChunksAround(spawnX, 120);
        int surfaceY = terrainGen.findSurfaceY(spawnX);
        respawnX = spawnX;
        respawnY = surfaceY - GameConstants.PLAYER_SIZE;

        player = new Player(respawnX, respawnY);

        if (mode == GameMode.CREATIVE) {
            player.isFlying = true;
        }

        if (mode == GameMode.PVE) {
            int aiSpawnX = spawnX + 400;
            terrainGen.ensureChunksAround(aiSpawnX, 20);
            int aiSurfaceY = terrainGen.findSurfaceY(aiSpawnX);
            aiPlayer = new AIPlayer(aiSpawnX, aiSurfaceY - GameConstants.PLAYER_SIZE);
        }
    }

    public void update(double dt) {
        if (gameState != GameState.PLAYING) return;

        gameTime += dt;
        dayTime += dt / GameConstants.DAY_CYCLE_SECONDS;
        if (dayTime > 1) dayTime -= 1;

        player.updateTimers(dt);

        if (player.isDead) {
            gameState = GameState.DEAD;
            return;
        }

        terrainGen.ensureChunksAround((int)player.x, 200);

        if (hookCharging) {
            hookChargeTime += dt;
            if (hookChargeTime > 2.0) hookChargeTime = 2.0;
        }

        if (hookPulling && activeHook == null) {
            double dx = hookAnchorX - player.x - GameConstants.PLAYER_SIZE / 2;
            double dy = hookAnchorY - player.y - GameConstants.PLAYER_SIZE / 2;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist > 8) {
                double pullSpeed = 6;
                player.vx = dx / dist * pullSpeed;
                player.vy = dy / dist * pullSpeed;
                player.onGround = false;
            } else {
                hookPulling = false;
                player.vx = 0;
                player.vy = 0;
            }
        }

        if (!hookPulling) {
            physics.updatePlayer(player, this, dt);
        } else {
            player.x += player.vx;
            player.y += player.vy;
        }

        if (player.justLanded()) {
            double fallSpeed = player.vy;
            if (fallSpeed > 3) {
                int particleCount = Math.min(12, (int)(fallSpeed * 2));
                spawnLandingParticles(player.x + GameConstants.PLAYER_SIZE / 2, player.y + GameConstants.PLAYER_SIZE, particleCount);
            }
        }

        checkLavaDamage(dt);

        updateEnemies(dt);
        updateAIPlayer(dt);
        updateProjectiles(dt);
        updateParticles(dt);
        spawnEnemies(dt);

        if (player.y > GameConstants.WORLD_HEIGHT * GameConstants.TILE_SIZE + 100) {
            player.takeDamage(999);
        }

        if (gameMode == GameMode.PVE && aiPlayer != null && aiPlayer.isDead) {
            gameState = GameState.WON;
        }
    }

    public void startHookCharge(double screenX, double screenY) {
        if (!player.hasItem(ItemType.GRAPPLING_HOOK)) return;
        if (activeHook != null || hookPulling) return;
        hookCharging = true;
        hookChargeTime = 0;
        updateHookAim(screenX, screenY);
    }

    public void updateHookAim(double screenX, double screenY) {
        double worldX = screenX + cameraX;
        double worldY = screenY + cameraY;
        double playerCenterX = player.x + GameConstants.PLAYER_SIZE / 2;
        double playerCenterY = player.y + GameConstants.PLAYER_SIZE / 2;
        hookAimAngle = Math.atan2(worldY - playerCenterY, worldX - playerCenterX);
    }

    public void releaseHook() {
        if (!hookCharging) return;
        hookCharging = false;

        double power = 5 + hookChargeTime * 6;
        double vx = Math.cos(hookAimAngle) * power;
        double vy = Math.sin(hookAimAngle) * power;
        double playerCenterX = player.x + GameConstants.PLAYER_SIZE / 2;
        double playerCenterY = player.y + GameConstants.PLAYER_SIZE / 2;

        activeHook = new Projectile(playerCenterX, playerCenterY, vx, vy, 0, ProjectileType.HOOK);
        projectiles.add(activeHook);
    }

    public void retractHook() {
        if (activeHook != null) {
            projectiles.remove(activeHook);
            activeHook = null;
        }
        hookPulling = false;
        hookCharging = false;
    }

    public boolean isHookCharging() { return hookCharging; }
    public double getHookChargeTime() { return hookChargeTime; }
    public double getHookAimAngle() { return hookAimAngle; }
    public boolean isHookPulling() { return hookPulling; }
    public double getHookAnchorX() { return hookAnchorX; }
    public double getHookAnchorY() { return hookAnchorY; }
    public Projectile getActiveHook() { return activeHook; }

    public void dismissHint(String hint) {
        dismissedHints.add(hint);
    }

    public boolean isHintDismissed(String hint) {
        return dismissedHints.contains(hint);
    }

    public boolean craftItem(int recipeIndex) {
        CraftingRecipe[] recipes = GameConstants.CRAFTING_RECIPES;
        if (recipeIndex < 0 || recipeIndex >= recipes.length) return false;

        CraftingRecipe recipe = recipes[recipeIndex];
        for (int j = 0; j < recipe.ingredients.length; j++) {
            if (player.getItemCount(recipe.ingredients[j]) < recipe.amounts[j]) {
                return false;
            }
        }

        for (int j = 0; j < recipe.ingredients.length; j++) {
            player.removeItem(recipe.ingredients[j], recipe.amounts[j]);
        }
        player.addItem(recipe.result, recipe.resultCount);
        return true;
    }

    public void openChest(int tx, int ty) {
        BlockType type = terrainGen.getBlockType(tx, ty);
        if (type != BlockType.CHEST) return;

        terrainGen.setBlockType(tx, ty, BlockType.AIR);
        spawnParticles(tx * GameConstants.TILE_SIZE + GameConstants.TILE_SIZE / 2,
                       ty * GameConstants.TILE_SIZE + GameConstants.TILE_SIZE / 2,
                       new Color(218, 165, 32), 12);
        spawnParticles(tx * GameConstants.TILE_SIZE + GameConstants.TILE_SIZE / 2,
                       ty * GameConstants.TILE_SIZE + GameConstants.TILE_SIZE / 2,
                       new Color(255, 215, 0), 6);

        ItemType[] possibleDrops = {
            ItemType.IRON_INGOT, ItemType.GOLD_INGOT, ItemType.DIAMOND,
            ItemType.HEALTH_POTION, ItemType.STRENGTH_POTION, ItemType.SPEED_POTION, ItemType.SHIELD_POTION,
            ItemType.IRON_PICKAXE, ItemType.IRON_SWORD, ItemType.IRON_AXE,
            ItemType.GOLD_PICKAXE, ItemType.GOLD_AXE, ItemType.GOLD_INGOT,
            ItemType.BOW, ItemType.ARROW, ItemType.SHIELD,
            ItemType.GRAPPLING_HOOK, ItemType.STRING, ItemType.WOOD,
            ItemType.STONE_BLOCK, ItemType.DIRT_BLOCK, ItemType.LEAVES,
            ItemType.WOODEN_PICKAXE, ItemType.WOODEN_AXE, ItemType.WOODEN_SWORD
        };
        int[] dropCounts = {3, 3, 2, 3, 2, 2, 2, 1, 1, 1, 1, 1, 2, 1, 10, 1, 1, 5, 20, 20, 20, 10, 1, 1, 1};

        int numDrops = 3 + random.nextInt(4);
        for (int i = 0; i < numDrops; i++) {
            int idx = random.nextInt(possibleDrops.length);
            player.addItem(possibleDrops[idx], dropCounts[idx]);
        }
    }

    private void checkLavaDamage(double dt) {
        double playerCenterX = player.x + GameConstants.PLAYER_SIZE / 2;
        double playerBottomY = player.y + GameConstants.PLAYER_SIZE;
        BlockType belowType = getBlockTypeAtWorld(playerCenterX, playerBottomY);
        BlockType atType = getBlockTypeAtWorld(playerCenterX, player.y + GameConstants.PLAYER_SIZE / 2);

        if (belowType == BlockType.LAVA || atType == BlockType.LAVA) {
            if (player.lavaDamageTimer <= 0) {
                player.takeDamage(GameConstants.LAVA_DAMAGE);
                player.lavaDamageTimer = 0.5;
                spawnParticles(player.x + GameConstants.PLAYER_SIZE / 2, player.y + GameConstants.PLAYER_SIZE, new Color(255, 100, 0), 5);
            }
        }
    }

    private void spawnLandingParticles(double x, double y, int count) {
        for (int i = 0; i < count; i++) {
            double vx = (random.nextDouble() - 0.5) * 6;
            double vy = -random.nextDouble() * 4 - 1;
            Color dirtColor = new Color(139 + random.nextInt(30) - 15,
                                        69 + random.nextInt(20) - 10,
                                        19 + random.nextInt(15) - 7);
            particles.add(new Particle(x + (random.nextDouble() - 0.5) * 20, y, vx, vy, dirtColor, 0.4 + random.nextDouble() * 0.3));
        }
    }

    private void spawnParticles(double x, double y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            double vx = (random.nextDouble() - 0.5) * 4;
            double vy = -random.nextDouble() * 3;
            particles.add(new Particle(x, y, vx, vy, color, 0.5 + random.nextDouble() * 0.5));
        }
    }

    private void updateEnemies(double dt) {
        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy enemy = it.next();
            enemy.updateAI(player.x, player.y, dt);

            int etx = (int)(enemy.x + enemy.getWidth() / 2) / GameConstants.TILE_SIZE;
            int ety = (int)(enemy.y + enemy.getHeight()) / GameConstants.TILE_SIZE;
            BlockType below = terrainGen.getBlockType(etx, ety);
            GameConstants.BlockProps belowProps = GameConstants.BLOCK_PROPERTIES.get(below);
            if (belowProps != null && belowProps.solid && enemy.vy >= 0) {
                int blockTopY = ety * GameConstants.TILE_SIZE;
                if (enemy.y + enemy.getHeight() > blockTopY && enemy.y + enemy.getHeight() < blockTopY + GameConstants.TILE_SIZE + 2) {
                    enemy.y = blockTopY - enemy.getHeight();
                    enemy.vy = 0;
                    enemy.onGround = true;
                }
            }

            if (enemy.canAttack()) {
                double dx = player.x - enemy.x;
                double dy = player.y - enemy.y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < GameConstants.ENEMY_ATTACK_RANGE + GameConstants.PLAYER_SIZE) {
                    player.takeDamage(enemy.damage);
                    enemy.doAttack();
                    spawnParticles(player.x + GameConstants.PLAYER_SIZE / 2, player.y + GameConstants.PLAYER_SIZE / 2, new Color(255, 0, 0), 4);
                }
            }

            if (enemy.isDead) {
                score += 10;
                player.killCount++;
                spawnParticles(enemy.x + enemy.getWidth() / 2, enemy.y + enemy.getHeight() / 2, new Color(255, 0, 0), 8);
                it.remove();
                continue;
            }

            if (Math.abs(enemy.x - player.x) > 1500) {
                it.remove();
            }
        }
    }

    private void updateAIPlayer(double dt) {
        if (aiPlayer == null || aiPlayer.isDead) return;

        aiPlayer.updateAI(player.x, player.y, this, dt);

        int aitx = (int)(aiPlayer.x + 16) / GameConstants.TILE_SIZE;
        int aity = (int)(aiPlayer.y + 32) / GameConstants.TILE_SIZE;
        BlockType below = terrainGen.getBlockType(aitx, aity);
        GameConstants.BlockProps belowProps = GameConstants.BLOCK_PROPERTIES.get(below);
        if (belowProps != null && belowProps.solid && aiPlayer.vy >= 0) {
            int blockTopY = aity * GameConstants.TILE_SIZE;
            if (aiPlayer.y + 32 > blockTopY && aiPlayer.y + 32 < blockTopY + GameConstants.TILE_SIZE + 2) {
                aiPlayer.y = blockTopY - 32;
                aiPlayer.vy = 0;
                aiPlayer.onGround = true;
            }
        }

        if (aiPlayer.canAttackPlayer()) {
            double dx = player.x - aiPlayer.x;
            double dy = player.y - aiPlayer.y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < 60) {
                player.takeDamage(aiPlayer.getAttackPower());
                aiPlayer.doAttack();
                spawnParticles(player.x + GameConstants.PLAYER_SIZE / 2, player.y + GameConstants.PLAYER_SIZE / 2, new Color(255, 100, 0), 5);
            }
        }
    }

    private void updateProjectiles(double dt) {
        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile proj = it.next();

            if (proj.type == ProjectileType.HOOK && proj == activeHook) {
                proj.x += proj.vx;
                proj.y += proj.vy;

                int htx = (int)proj.x / GameConstants.TILE_SIZE;
                int hty = (int)proj.y / GameConstants.TILE_SIZE;
                BlockType hitBlock = terrainGen.getBlockType(htx, hty);
                if (hitBlock != BlockType.AIR && hitBlock != BlockType.WATER && hitBlock != BlockType.LAVA) {
                    GameConstants.BlockProps hitProps = GameConstants.BLOCK_PROPERTIES.get(hitBlock);
                    if (hitProps != null && hitProps.solid) {
                        hookAnchorX = proj.x;
                        hookAnchorY = proj.y;
                        hookPulling = true;
                        it.remove();
                        activeHook = null;
                        continue;
                    }
                }

                double dx = proj.x - (player.x + GameConstants.PLAYER_SIZE / 2);
                double dy = proj.y - (player.y + GameConstants.PLAYER_SIZE / 2);
                if (dx * dx + dy * dy > 600 * 600) {
                    it.remove();
                    activeHook = null;
                }
                continue;
            }

            proj.x += proj.vx;
            proj.y += proj.vy;
            proj.vy += 0.1;

            for (Enemy enemy : enemies) {
                if (proj.x > enemy.x && proj.x < enemy.x + enemy.getWidth() &&
                    proj.y > enemy.y && proj.y < enemy.y + enemy.getHeight()) {
                    enemy.takeDamage(proj.damage);
                    spawnParticles(proj.x, proj.y, new Color(255, 255, 0), 3);
                    it.remove();
                    continue;
                }
            }
            if (!it.hasNext()) break;

            BlockType blockType = getBlockTypeAtWorld(proj.x, proj.y);
            if (blockType != BlockType.AIR && blockType != BlockType.WATER && blockType != BlockType.LAVA) {
                it.remove();
                continue;
            }

            if (proj.y < -500 || proj.y > GameConstants.WORLD_HEIGHT * GameConstants.TILE_SIZE + 100) {
                it.remove();
            }
        }
    }

    private void updateParticles(double dt) {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.x += p.vx;
            p.y += p.vy;
            p.vy += 0.15;
            p.life -= dt;
            if (p.life <= 0) it.remove();
        }
    }

    private void spawnEnemies(double dt) {
        if (gameMode == GameMode.CREATIVE) return;
        if (enemies.size() >= GameConstants.MAX_ENEMIES) return;

        enemySpawnTimer += dt;
        if (enemySpawnTimer < GameConstants.ENEMY_SPAWN_INTERVAL) return;
        enemySpawnTimer = 0;

        double angle = random.nextDouble() * Math.PI * 2;
        double dist = GameConstants.ENEMY_SPAWN_MIN_DISTANCE + random.nextDouble() * (GameConstants.ENEMY_SPAWN_MAX_DISTANCE - GameConstants.ENEMY_SPAWN_MIN_DISTANCE);
        double ex = player.x + Math.cos(angle) * dist;
        double ey = player.y;

        terrainGen.ensureChunksAround((int)ex, 10);
        int surfaceY = terrainGen.findSurfaceY((int)ex);
        ey = surfaceY - 32;

        int totalWeight = 0;
        for (GameConstants.SpawnableEnemy se : GameConstants.SPAWNABLE_ENEMIES) {
            totalWeight += se.weight;
        }
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        GameConstants.SpawnableEnemy chosen = GameConstants.SPAWNABLE_ENEMIES[0];
        for (GameConstants.SpawnableEnemy se : GameConstants.SPAWNABLE_ENEMIES) {
            cumulative += se.weight;
            if (roll < cumulative) { chosen = se; break; }
        }

        Enemy enemy = new Enemy(chosen.type, ex, ey, chosen.health, chosen.damage, chosen.speed);
        enemies.add(enemy);
    }

    public BlockType getBlockTypeAtWorld(double worldX, double worldY) {
        int tx = (int)worldX / GameConstants.TILE_SIZE;
        int ty = (int)worldY / GameConstants.TILE_SIZE;
        return terrainGen.getBlockType(tx, ty);
    }

    public boolean isBlockSolid(int tx, int ty) {
        BlockType type = terrainGen.getBlockType(tx, ty);
        GameConstants.BlockProps props = GameConstants.BLOCK_PROPERTIES.get(type);
        return props != null && props.solid;
    }

    public int[] findBlockAtScreen(double screenX, double screenY, double cameraX, double cameraY) {
        double worldX = screenX + cameraX;
        double worldY = screenY + cameraY;
        double playerCenterX = player.x + GameConstants.PLAYER_SIZE / 2;
        double playerCenterY = player.y + GameConstants.PLAYER_SIZE / 2;
        double dist = Math.sqrt((worldX - playerCenterX) * (worldX - playerCenterX) + (worldY - playerCenterY) * (worldY - playerCenterY));
        if (dist > GameConstants.MINING_RANGE) return null;

        int tx = (int)worldX / GameConstants.TILE_SIZE;
        int ty = (int)worldY / GameConstants.TILE_SIZE;
        BlockType type = terrainGen.getBlockType(tx, ty);
        if (type != BlockType.AIR) {
            return new int[]{tx, ty};
        }
        return null;
    }

    public void mineBlock(int[] tilePos, double dt) {
        if (tilePos == null) return;
        int tx = tilePos[0];
        int ty = tilePos[1];
        BlockType type = terrainGen.getBlockType(tx, ty);

        if (type == BlockType.CHEST) {
            openChest(tx, ty);
            return;
        }

        GameConstants.BlockProps props = GameConstants.BLOCK_PROPERTIES.get(type);
        if (props == null || props.hardness <= 0) return;

        if (miningTargetTx != tx || miningTargetTy != ty) {
            miningTargetTx = tx;
            miningTargetTy = ty;
            miningProgress = 0;
        }

        double miningSpeed = player.getMiningSpeed();

        Item selectedItem = player.getSelectedItem();
        if (selectedItem != null) {
            GameConstants.ItemProps itemProps = GameConstants.ITEM_PROPERTIES.get(selectedItem.type);
            if (itemProps != null) {
                if (itemProps.miningSpeedBonus != null) {
                    miningSpeed += itemProps.miningSpeedBonus;
                }
                if (itemProps.axeBonus != null && (type == BlockType.WOOD || type == BlockType.LEAVES)) {
                    miningSpeed += itemProps.axeBonus;
                }
            }
        }

        miningProgress += dt * miningSpeed;
        if (miningProgress >= props.hardness) {
            if (props.drop != null) {
                player.addItem(props.drop, 1);
            }
            int worldX = tx * GameConstants.TILE_SIZE;
            int worldY = ty * GameConstants.TILE_SIZE;
            spawnParticles(worldX + GameConstants.TILE_SIZE / 2, worldY + GameConstants.TILE_SIZE / 2, props.color, 6);
            terrainGen.setBlockType(tx, ty, BlockType.AIR);
            miningTargetTx = -1;
            miningTargetTy = -1;
            miningProgress = 0;
        }
    }

    public void stopMining() {
        miningTargetTx = -1;
        miningTargetTy = -1;
        miningProgress = 0;
    }

    public void attackAtScreen(double screenX, double screenY, double cameraX, double cameraY) {
        double worldX = screenX + cameraX;
        double worldY = screenY + cameraY;
        double attackRange = 50;
        for (Enemy enemy : enemies) {
            double ex = enemy.x + enemy.getWidth() / 2;
            double ey = enemy.y + enemy.getHeight() / 2;
            double dx = worldX - ex;
            double dy = worldY - ey;
            if (Math.sqrt(dx * dx + dy * dy) < attackRange) {
                enemy.takeDamage(player.getAttackPower());
                spawnParticles(ex, ey, new Color(255, 255, 255), 4);
                break;
            }
        }
        if (aiPlayer != null && !aiPlayer.isDead) {
            double ax = aiPlayer.x + 16;
            double ay = aiPlayer.y + 16;
            double dx = worldX - ax;
            double dy = worldY - ay;
            if (Math.sqrt(dx * dx + dy * dy) < attackRange) {
                aiPlayer.takeDamage(player.getAttackPower());
                spawnParticles(ax, ay, new Color(255, 100, 0), 4);
            }
        }
    }

    public void shootProjectile(double screenX, double screenY, double cameraX, double cameraY) {
        double worldX = screenX + cameraX;
        double worldY = screenY + cameraY;
        double playerCenterX = player.x + GameConstants.PLAYER_SIZE / 2;
        double playerCenterY = player.y + GameConstants.PLAYER_SIZE / 2;
        double dx = worldX - playerCenterX;
        double dy = worldY - playerCenterY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < 1) return;
        double speed = 8;
        double vx = dx / dist * speed;
        double vy = dy / dist * speed;
        projectiles.add(new Projectile(playerCenterX, playerCenterY, vx, vy, player.getAttackPower(), ProjectileType.ARROW));
    }

    public void useItem(Item item) {
        if (item == null) return;
        switch (item.type) {
            case HEALTH_POTION:
                player.heal(30);
                player.removeItem(ItemType.HEALTH_POTION, 1);
                break;
            case STRENGTH_POTION:
                player.attackPower += 5;
                player.removeItem(ItemType.STRENGTH_POTION, 1);
                break;
            case SPEED_POTION:
                player.speed += 0.5;
                player.removeItem(ItemType.SPEED_POTION, 1);
                break;
            case SHIELD_POTION:
                player.shield = Math.min(GameConstants.MAX_SHIELD, player.shield + 25);
                player.removeItem(ItemType.SHIELD_POTION, 1);
                break;
            default:
                break;
        }
    }

    public void placeBlock(int screenX, int screenY, Item item) {
        double worldX = screenX + cameraX;
        double worldY = screenY + cameraY;
        int tx = (int)worldX / GameConstants.TILE_SIZE;
        int ty = (int)worldY / GameConstants.TILE_SIZE;

        double px = player.x + GameConstants.PLAYER_SIZE / 2;
        double py = player.y + GameConstants.PLAYER_SIZE / 2;
        int ptx = (int)px / GameConstants.TILE_SIZE;
        int pty = (int)py / GameConstants.TILE_SIZE;
        if (tx == ptx && (ty == pty || ty == pty + 1)) return;

        if (terrainGen.getBlockType(tx, ty) != BlockType.AIR) return;

        BlockType blockType = itemToBlock(item.type);
        if (blockType != null) {
            terrainGen.setBlockType(tx, ty, blockType);
            player.removeItem(item.type, 1);
        }
    }

    private double cameraX;
    private double cameraY;

    public void setCamera(double cx, double cy) { cameraX = cx; cameraY = cy; }

    private BlockType itemToBlock(ItemType t) {
        switch (t) {
            case DIRT_BLOCK: return BlockType.DIRT;
            case GRASS_BLOCK: return BlockType.GRASS;
            case STONE_BLOCK: return BlockType.STONE;
            case WOOD: return BlockType.WOOD;
            case WORKBENCH: return BlockType.WORKBENCH;
            case FURNACE: return BlockType.FURNACE;
            default: return null;
        }
    }

    public void respawnPlayer() {
        player.respawn(respawnX, respawnY);
        gameState = GameState.PLAYING;
    }

    public boolean isDay() {
        return dayTime < 0.5;
    }

    public double getDayBrightness() {
        if (dayTime < 0.25) return 1.0;
        if (dayTime < 0.35) return 1.0 - (dayTime - 0.25) / 0.1;
        if (dayTime < 0.65) return 0.0;
        if (dayTime < 0.75) return (dayTime - 0.65) / 0.1;
        return 1.0;
    }

    public Player getPlayer() { return player; }
    public AIPlayer getAIPlayer() { return aiPlayer; }
    public TerrainGenerator getTerrainGen() { return terrainGen; }
    public List<Enemy> getEnemies() { return enemies; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public List<Particle> getParticles() { return particles; }
    public GameMode getGameMode() { return gameMode; }
    public GameState getGameState() { return gameState; }
    public double getDayTime() { return dayTime; }
    public double getGameTime() { return gameTime; }
    public int getScore() { return score; }
    public int getMiningTargetTx() { return miningTargetTx; }
    public int getMiningTargetTy() { return miningTargetTy; }
    public double getMiningProgress() { return miningProgress; }
    public int getWorldHeightTiles() { return worldHeightTiles; }
    public void setGameState(GameState s) { gameState = s; }
}
