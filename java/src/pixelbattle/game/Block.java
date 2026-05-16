package pixelbattle.game;

public class Block {
    public BlockType type;
    public int x;
    public int y;
    public int variant;

    public Block(BlockType type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.variant = 0;
    }

    public Block(BlockType type, int x, int y, int variant) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.variant = variant;
    }
}
