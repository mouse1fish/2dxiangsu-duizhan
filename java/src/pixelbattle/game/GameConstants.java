package pixelbattle.game;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class GameConstants {
    public static final int TILE_SIZE = 16;
    public static final int CHUNK_SIZE = 32;
    public static final int PLAYER_SIZE = 32;
    public static final int PLAYER_COLLISION_WIDTH = 14;
    public static final int PLAYER_COLLISION_HEIGHT = 28;
    public static final double GRAVITY = 0.6;
    public static final double JUMP_FORCE = 8.5;
    public static final double BASE_SPEED = 3;
    public static final int MAX_HEALTH = 100;
    public static final int MAX_SHIELD = 50;
    public static final double DAY_CYCLE_SECONDS = 120;
    public static final double LAVA_DAMAGE = 5;
    public static final int WORLD_HEIGHT = 256;
    public static final int SURFACE_LEVEL = 64;
    public static final int ENEMY_SPAWN_MIN_DISTANCE = 500;
    public static final int ENEMY_SPAWN_MAX_DISTANCE = 900;
    public static final int MAX_ENEMIES = 8;
    public static final double ENEMY_SPAWN_INTERVAL = 8.0;
    public static final double MINING_RANGE = 80;
    public static final double ENEMY_DETECT_RANGE = 300;
    public static final double ENEMY_ATTACK_RANGE = 40;
    public static final double AI_PLAYER_DETECT_RANGE = 400;

    public static class BlockProps {
        public Color color;
        public double hardness;
        public ItemType drop;
        public boolean solid;
        public BlockProps(Color color, double hardness, boolean solid) {
            this.color = color; this.hardness = hardness; this.drop = null; this.solid = solid;
        }
        public BlockProps(Color color, double hardness, boolean solid, ItemType drop) {
            this.color = color; this.hardness = hardness; this.drop = drop; this.solid = solid;
        }
    }

    public static final Map<BlockType, BlockProps> BLOCK_PROPERTIES = new HashMap<>();
    static {
        BLOCK_PROPERTIES.put(BlockType.AIR, new BlockProps(new Color(0,0,0,0), 0, false));
        BLOCK_PROPERTIES.put(BlockType.GRASS, new BlockProps(new Color(76,153,0), 0.4, true, ItemType.GRASS_BLOCK));
        BLOCK_PROPERTIES.put(BlockType.DIRT, new BlockProps(new Color(139,69,19), 0.5, true, ItemType.DIRT_BLOCK));
        BLOCK_PROPERTIES.put(BlockType.STONE, new BlockProps(new Color(128,128,128), 1.5, true, ItemType.STONE_BLOCK));
        BLOCK_PROPERTIES.put(BlockType.IRON_ORE, new BlockProps(new Color(169,169,169), 3, true, ItemType.IRON_ORE));
        BLOCK_PROPERTIES.put(BlockType.GOLD_ORE, new BlockProps(new Color(255,215,0), 5, true, ItemType.GOLD_ORE));
        BLOCK_PROPERTIES.put(BlockType.DIAMOND_ORE, new BlockProps(new Color(0,255,255), 8, true, ItemType.DIAMOND));
        BLOCK_PROPERTIES.put(BlockType.WOOD, new BlockProps(new Color(160,82,45), 1, true, ItemType.WOOD));
        BLOCK_PROPERTIES.put(BlockType.LEAVES, new BlockProps(new Color(34,139,34), 0.3, false, ItemType.LEAVES));
        BLOCK_PROPERTIES.put(BlockType.WATER, new BlockProps(new Color(65,105,225), 5, false));
        BLOCK_PROPERTIES.put(BlockType.LAVA, new BlockProps(new Color(255,69,0), 8, false));
        BLOCK_PROPERTIES.put(BlockType.CHEST, new BlockProps(new Color(218,165,32), 0.3, true));
        BLOCK_PROPERTIES.put(BlockType.WORKBENCH, new BlockProps(new Color(222,184,135), 2, true));
        BLOCK_PROPERTIES.put(BlockType.FURNACE, new BlockProps(new Color(85,85,85), 3, true));
        BLOCK_PROPERTIES.put(BlockType.BEDROCK, new BlockProps(new Color(51,51,51), 10, true));
    }

    public static class ItemProps {
        public String name;
        public Color color;
        public String icon;
        public Double attackPower;
        public Double defensePower;
        public Double speedBonus;
        public Double miningSpeedBonus;
        public Double axeBonus;
        public ItemProps(String name, Color color, String icon) {
            this.name = name; this.color = color; this.icon = icon;
            this.attackPower = null; this.defensePower = null;
            this.speedBonus = null; this.miningSpeedBonus = null; this.axeBonus = null;
        }
    }

    public static final Map<ItemType, ItemProps> ITEM_PROPERTIES = new HashMap<>();
    static {
        ITEM_PROPERTIES.put(ItemType.DIRT_BLOCK, new ItemProps("泥土", new Color(139,69,19), "block"));
        ITEM_PROPERTIES.put(ItemType.GRASS_BLOCK, new ItemProps("草方块", new Color(76,153,0), "block"));
        ITEM_PROPERTIES.put(ItemType.STONE_BLOCK, new ItemProps("石头", new Color(128,128,128), "block"));
        ITEM_PROPERTIES.put(ItemType.IRON_ORE, new ItemProps("铁矿石", new Color(169,169,169), "ore"));
        ITEM_PROPERTIES.put(ItemType.GOLD_ORE, new ItemProps("金矿石", new Color(255,215,0), "ore"));
        ITEM_PROPERTIES.put(ItemType.DIAMOND, new ItemProps("钻石", new Color(0,255,255), "gem"));
        ITEM_PROPERTIES.put(ItemType.WOOD, new ItemProps("木材", new Color(160,82,45), "block"));
        ITEM_PROPERTIES.put(ItemType.LEAVES, new ItemProps("树叶", new Color(34,139,34), "block"));
        ITEM_PROPERTIES.put(ItemType.IRON_INGOT, new ItemProps("铁锭", new Color(192,192,192), "ingot"));
        ITEM_PROPERTIES.put(ItemType.GOLD_INGOT, new ItemProps("金锭", new Color(255,215,0), "ingot"));

        ItemProps wp = new ItemProps("木镐", new Color(160,82,45), "pickaxe_wood"); wp.miningSpeedBonus = 0.5;
        ITEM_PROPERTIES.put(ItemType.WOODEN_PICKAXE, wp);
        ItemProps ip = new ItemProps("铁镐", new Color(192,192,192), "pickaxe_iron"); ip.miningSpeedBonus = 1.5;
        ITEM_PROPERTIES.put(ItemType.IRON_PICKAXE, ip);
        ItemProps gp = new ItemProps("金镐", new Color(255,215,0), "pickaxe_gold"); gp.miningSpeedBonus = 2.0;
        ITEM_PROPERTIES.put(ItemType.GOLD_PICKAXE, gp);
        ItemProps dp = new ItemProps("钻石镐", new Color(0,255,255), "pickaxe_diamond"); dp.miningSpeedBonus = 3.0;
        ITEM_PROPERTIES.put(ItemType.DIAMOND_PICKAXE, dp);

        ItemProps wa = new ItemProps("木斧", new Color(160,82,45), "axe_wood"); wa.axeBonus = 2.0;
        ITEM_PROPERTIES.put(ItemType.WOODEN_AXE, wa);
        ItemProps ia = new ItemProps("铁斧", new Color(192,192,192), "axe_iron"); ia.axeBonus = 4.0;
        ITEM_PROPERTIES.put(ItemType.IRON_AXE, ia);
        ItemProps ga = new ItemProps("金斧", new Color(255,215,0), "axe_gold"); ga.axeBonus = 6.0;
        ITEM_PROPERTIES.put(ItemType.GOLD_AXE, ga);
        ItemProps da = new ItemProps("钻石斧", new Color(0,255,255), "axe_diamond"); da.axeBonus = 9.0;
        ITEM_PROPERTIES.put(ItemType.DIAMOND_AXE, da);

        ItemProps ws = new ItemProps("木剑", new Color(160,82,45), "sword_wood"); ws.attackPower = 5.0;
        ITEM_PROPERTIES.put(ItemType.WOODEN_SWORD, ws);
        ItemProps is = new ItemProps("铁剑", new Color(192,192,192), "sword_iron"); is.attackPower = 10.0;
        ITEM_PROPERTIES.put(ItemType.IRON_SWORD, is);
        ItemProps ls = new ItemProps("激光剑", new Color(0,255,136), "sword_laser"); ls.attackPower = 25.0;
        ITEM_PROPERTIES.put(ItemType.LASER_SWORD, ls);

        ItemProps bw = new ItemProps("弓箭", new Color(139,69,19), "bow"); bw.attackPower = 8.0;
        ITEM_PROPERTIES.put(ItemType.BOW, bw);
        ITEM_PROPERTIES.put(ItemType.ARROW, new ItemProps("箭矢", new Color(192,192,192), "arrow"));
        ITEM_PROPERTIES.put(ItemType.GRAPPLING_HOOK, new ItemProps("钩爪", new Color(192,192,192), "hook"));
        ItemProps sh = new ItemProps("盾牌", new Color(128,128,128), "shield"); sh.defensePower = 0.3;
        ITEM_PROPERTIES.put(ItemType.SHIELD, sh);

        ITEM_PROPERTIES.put(ItemType.HEALTH_POTION, new ItemProps("生命药水", new Color(255,107,107), "potion_health"));
        ITEM_PROPERTIES.put(ItemType.STRENGTH_POTION, new ItemProps("力量药水", new Color(255,217,61), "potion_strength"));
        ITEM_PROPERTIES.put(ItemType.SPEED_POTION, new ItemProps("速度药水", new Color(107,203,119), "potion_speed"));
        ITEM_PROPERTIES.put(ItemType.SHIELD_POTION, new ItemProps("护盾药水", new Color(77,150,255), "potion_shield"));

        ITEM_PROPERTIES.put(ItemType.WORKBENCH, new ItemProps("工作台", new Color(222,184,135), "workbench"));
        ITEM_PROPERTIES.put(ItemType.FURNACE, new ItemProps("熔炉", new Color(85,85,85), "furnace"));
        ITEM_PROPERTIES.put(ItemType.CIRCUIT_BOARD, new ItemProps("电路板", new Color(144,238,144), "circuit"));
        ITEM_PROPERTIES.put(ItemType.ENERGY_CORE, new ItemProps("能量核心", new Color(255,215,0), "core"));

        ItemProps lg = new ItemProps("激光炮", new Color(0,255,136), "gun_laser"); lg.attackPower = 40.0;
        ITEM_PROPERTIES.put(ItemType.LASER_GUN, lg);
        ITEM_PROPERTIES.put(ItemType.SPACESHIP, new ItemProps("飞船", new Color(65,105,225), "ship"));
        ITEM_PROPERTIES.put(ItemType.ROBOT, new ItemProps("机器人", new Color(128,128,128), "robot"));
        ITEM_PROPERTIES.put(ItemType.STRING, new ItemProps("线", new Color(255,255,255), "string"));
        ITEM_PROPERTIES.put(ItemType.BULLET, new ItemProps("子弹", new Color(255,200,50), "bullet"));
    }

    public static final CraftingRecipe[] CRAFTING_RECIPES = {
        new CraftingRecipe(ItemType.WORKBENCH, 1, new ItemType[]{ItemType.WOOD}, new int[]{10}),
        new CraftingRecipe(ItemType.FURNACE, 1, new ItemType[]{ItemType.STONE_BLOCK}, new int[]{8}),
        new CraftingRecipe(ItemType.WOODEN_PICKAXE, 1, new ItemType[]{ItemType.WOOD}, new int[]{3}),
        new CraftingRecipe(ItemType.WOODEN_AXE, 1, new ItemType[]{ItemType.WOOD}, new int[]{3}),
        new CraftingRecipe(ItemType.WOODEN_SWORD, 1, new ItemType[]{ItemType.WOOD}, new int[]{2}),
        new CraftingRecipe(ItemType.IRON_PICKAXE, 1, new ItemType[]{ItemType.IRON_INGOT}, new int[]{3}),
        new CraftingRecipe(ItemType.IRON_AXE, 1, new ItemType[]{ItemType.IRON_INGOT}, new int[]{3}),
        new CraftingRecipe(ItemType.IRON_SWORD, 1, new ItemType[]{ItemType.IRON_INGOT}, new int[]{5}),
        new CraftingRecipe(ItemType.GOLD_PICKAXE, 1, new ItemType[]{ItemType.GOLD_INGOT}, new int[]{3}),
        new CraftingRecipe(ItemType.GOLD_AXE, 1, new ItemType[]{ItemType.GOLD_INGOT}, new int[]{3}),
        new CraftingRecipe(ItemType.DIAMOND_PICKAXE, 1, new ItemType[]{ItemType.DIAMOND}, new int[]{3}),
        new CraftingRecipe(ItemType.DIAMOND_AXE, 1, new ItemType[]{ItemType.DIAMOND}, new int[]{3}),
        new CraftingRecipe(ItemType.BOW, 1, new ItemType[]{ItemType.WOOD, ItemType.STRING}, new int[]{3, 2}),
        new CraftingRecipe(ItemType.ARROW, 5, new ItemType[]{ItemType.WOOD, ItemType.STRING}, new int[]{1, 1}),
        new CraftingRecipe(ItemType.GRAPPLING_HOOK, 1, new ItemType[]{ItemType.IRON_INGOT}, new int[]{3}),
        new CraftingRecipe(ItemType.SHIELD, 1, new ItemType[]{ItemType.IRON_INGOT}, new int[]{4}),
        new CraftingRecipe(ItemType.IRON_INGOT, 1, new ItemType[]{ItemType.IRON_ORE}, new int[]{1}),
        new CraftingRecipe(ItemType.GOLD_INGOT, 1, new ItemType[]{ItemType.GOLD_ORE}, new int[]{1}),
        new CraftingRecipe(ItemType.CIRCUIT_BOARD, 1, new ItemType[]{ItemType.IRON_INGOT, ItemType.GOLD_ORE}, new int[]{2, 1}),
        new CraftingRecipe(ItemType.ENERGY_CORE, 1, new ItemType[]{ItemType.DIAMOND, ItemType.GOLD_INGOT}, new int[]{1, 2}),
        new CraftingRecipe(ItemType.LASER_SWORD, 1, new ItemType[]{ItemType.DIAMOND, ItemType.CIRCUIT_BOARD}, new int[]{2, 1}),
        new CraftingRecipe(ItemType.LASER_GUN, 1, new ItemType[]{ItemType.DIAMOND, ItemType.ENERGY_CORE}, new int[]{3, 1}),
        new CraftingRecipe(ItemType.SPACESHIP, 1, new ItemType[]{ItemType.CIRCUIT_BOARD, ItemType.ENERGY_CORE}, new int[]{5, 3}),
        new CraftingRecipe(ItemType.ROBOT, 1, new ItemType[]{ItemType.CIRCUIT_BOARD, ItemType.DIAMOND}, new int[]{10, 5}),
    };

    public static class SpawnableEnemy {
        public EnemyType type;
        public double health;
        public double damage;
        public double speed;
        public int weight;
        public SpawnableEnemy(EnemyType type, double health, double damage, double speed, int weight) {
            this.type = type; this.health = health; this.damage = damage; this.speed = speed; this.weight = weight;
        }
    }

    public static final SpawnableEnemy[] SPAWNABLE_ENEMIES = {
        new SpawnableEnemy(EnemyType.SLIME, 20, 5, 1.5, 50),
        new SpawnableEnemy(EnemyType.SKELETON, 40, 10, 2, 30),
        new SpawnableEnemy(EnemyType.GOBLIN, 30, 8, 2.5, 20),
    };

    public static final Color COLOR_SKY = new Color(135,206,235);
    public static final Color COLOR_SKY_NIGHT = new Color(15,15,50);
}
