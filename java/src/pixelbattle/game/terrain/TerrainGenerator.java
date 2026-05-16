package pixelbattle.game.terrain;

import pixelbattle.game.*;
import pixelbattle.game.utils.NoiseGenerator;
import java.util.*;

public class TerrainGenerator {
    private final long seed;
    private final NoiseGenerator noise;
    private final int worldHeightTiles;
    private final int surfaceBaseTile;
    private final HashMap<Integer, BlockType[][]> chunks = new HashMap<>();
    private static final int CHUNK_WIDTH = GameConstants.CHUNK_SIZE;

    public TerrainGenerator(long seed) {
        this.seed = seed;
        this.noise = new NoiseGenerator(seed);
        this.worldHeightTiles = GameConstants.WORLD_HEIGHT;
        this.surfaceBaseTile = GameConstants.SURFACE_LEVEL;
    }

    public void generateInitial(int spawnX) {
        ensureChunksAround(spawnX, 120);
    }

    private BlockType[][] getOrGenerateChunk(int chunkX) {
        BlockType[][] chunk = chunks.get(chunkX);
        if (chunk != null) return chunk;

        chunk = new BlockType[CHUNK_WIDTH][worldHeightTiles];
        Random chunkRandom = new Random(seed * 31 + chunkX * 7);

        for (int lx = 0; lx < CHUNK_WIDTH; lx++) {
            int tx = chunkX * CHUNK_WIDTH + lx;
            int worldX = tx * GameConstants.TILE_SIZE;

            double baseNoise = noise.noise(worldX * 0.003);
            double detailNoise = noise.noise(worldX * 0.015) * 0.6;
            double mountainNoise = noise.noise(worldX * 0.001 + seed * 0.5) * 15;
            int surfaceTile = surfaceBaseTile + (int)(baseNoise * 8 + detailNoise * 5 + mountainNoise);
            surfaceTile = Math.max(8, Math.min(surfaceTile, worldHeightTiles - 8));

            for (int ty = 0; ty < worldHeightTiles; ty++) {
                int depth = ty - surfaceTile;
                BlockType type = BlockType.AIR;

                if (ty < surfaceTile) {
                    type = BlockType.AIR;
                } else if (depth == 0) {
                    type = BlockType.GRASS;
                } else if (depth >= 1 && depth < 5) {
                    type = BlockType.DIRT;
                } else if (depth >= 5) {
                    type = BlockType.STONE;

                    double oreNoise = noise.noise(worldX * 0.1 + seed, ty * GameConstants.TILE_SIZE * 0.1);
                    if (depth > 25 && oreNoise > 0.72) type = BlockType.IRON_ORE;
                    if (depth > 50 && oreNoise > 0.82) type = BlockType.GOLD_ORE;
                    if (depth > 90 && oreNoise > 0.89) type = BlockType.DIAMOND_ORE;

                    double caveNoise = noise.noise(worldX * 0.03 + seed * 2, ty * GameConstants.TILE_SIZE * 0.03);
                    if (caveNoise > 0.62 && depth > 6) type = BlockType.AIR;

                    if (depth > 170) {
                        double lavaNoise = noise.noise(worldX * 0.07 + seed * 3, ty * GameConstants.TILE_SIZE * 0.07);
                        if (lavaNoise > 0.58) type = BlockType.LAVA;
                    }
                }

                chunk[lx][ty] = type;
            }

            if (chunkRandom.nextDouble() < 0.04) {
                generateTreeInChunk(chunk, lx, surfaceTile, chunkX, chunkRandom);
            }

            int chestCount = 3 + chunkRandom.nextInt(5);
            for (int ci = 0; ci < chestCount; ci++) {
                if (chunkRandom.nextDouble() < 0.45) {
                    int chestDepth = 2 + chunkRandom.nextInt(160);
                    int chestTy = surfaceTile + chestDepth;
                    if (chestTy > 0 && chestTy < worldHeightTiles - 1) {
                        for (int searchTy = chestTy; searchTy < Math.min(chestTy + 12, worldHeightTiles - 1); searchTy++) {
                            if (chunk[lx][searchTy] == BlockType.AIR && searchTy + 1 < worldHeightTiles) {
                                BlockType below = chunk[lx][searchTy + 1];
                                GameConstants.BlockProps belowProps = GameConstants.BLOCK_PROPERTIES.get(below);
                                if (belowProps != null && belowProps.solid) {
                                    chunk[lx][searchTy] = BlockType.CHEST;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        for (int lx = 0; lx < CHUNK_WIDTH; lx++) {
            for (int ty = worldHeightTiles - 4; ty < worldHeightTiles; ty++) {
                chunk[lx][ty] = BlockType.STONE;
            }
        }

        chunks.put(chunkX, chunk);
        return chunk;
    }

    private void generateTreeInChunk(BlockType[][] chunk, int lx, int surfaceTile, int chunkX, Random rng) {
        int trunkHeight = 4 + rng.nextInt(4);
        for (int i = 1; i <= trunkHeight; i++) {
            int ty = surfaceTile - i;
            if (ty >= 0 && ty < worldHeightTiles) {
                chunk[lx][ty] = BlockType.WOOD;
            }
        }
        int topTile = surfaceTile - trunkHeight;
        for (int dy = -2; dy <= 0; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                if (dx == 0 && dy == 0) continue;
                int llx = lx + dx;
                int lty = topTile + dy;
                if (llx >= 0 && llx < CHUNK_WIDTH && lty >= 0 && lty < worldHeightTiles) {
                    if (chunk[llx][lty] == BlockType.AIR) {
                        chunk[llx][lty] = BlockType.LEAVES;
                    }
                } else if (llx < 0 || llx >= CHUNK_WIDTH) {
                    int neighborChunkX = chunkX + (llx < 0 ? -1 : 1);
                    int nlx = llx < 0 ? CHUNK_WIDTH + llx : llx - CHUNK_WIDTH;
                    int nty = lty;
                    if (nlx >= 0 && nlx < CHUNK_WIDTH && nty >= 0 && nty < worldHeightTiles) {
                        BlockType[][] neighborChunk = getOrGenerateChunk(neighborChunkX);
                        if (neighborChunk[nlx][nty] == BlockType.AIR) {
                            neighborChunk[nlx][nty] = BlockType.LEAVES;
                        }
                    }
                }
            }
        }
    }

    public int findSurfaceY(int worldX) {
        int tx = worldX / GameConstants.TILE_SIZE;
        if (worldX < 0 && worldX % GameConstants.TILE_SIZE != 0) tx--;
        for (int ty = 0; ty < worldHeightTiles; ty++) {
            BlockType bt = getBlockType(tx, ty);
            if (bt == BlockType.GRASS || bt == BlockType.DIRT || bt == BlockType.STONE) {
                return ty * GameConstants.TILE_SIZE;
            }
        }
        return surfaceBaseTile * GameConstants.TILE_SIZE;
    }

    public BlockType getBlockType(int tx, int ty) {
        if (ty < 0 || ty >= worldHeightTiles) return BlockType.AIR;
        int chunkX = tx >= 0 ? tx / CHUNK_WIDTH : (tx + 1) / CHUNK_WIDTH - 1;
        int lx = tx - chunkX * CHUNK_WIDTH;
        if (lx < 0) lx += CHUNK_WIDTH;
        BlockType[][] chunk = getOrGenerateChunk(chunkX);
        return chunk[lx][ty];
    }

    public void setBlockType(int tx, int ty, BlockType type) {
        if (ty < 0 || ty >= worldHeightTiles) return;
        int chunkX = tx >= 0 ? tx / CHUNK_WIDTH : (tx + 1) / CHUNK_WIDTH - 1;
        int lx = tx - chunkX * CHUNK_WIDTH;
        if (lx < 0) lx += CHUNK_WIDTH;
        BlockType[][] chunk = getOrGenerateChunk(chunkX);
        chunk[lx][ty] = type;
    }

    public int getWorldHeightTiles() { return worldHeightTiles; }

    public void ensureChunksAround(int worldX, int radius) {
        int centerTx = worldX / GameConstants.TILE_SIZE;
        int startTx = centerTx - radius;
        int endTx = centerTx + radius;
        int startChunk = startTx >= 0 ? startTx / CHUNK_WIDTH : (startTx + 1) / CHUNK_WIDTH - 1;
        int endChunk = endTx >= 0 ? endTx / CHUNK_WIDTH : (endTx + 1) / CHUNK_WIDTH - 1;
        for (int c = startChunk; c <= endChunk; c++) {
            getOrGenerateChunk(c);
        }
    }
}
