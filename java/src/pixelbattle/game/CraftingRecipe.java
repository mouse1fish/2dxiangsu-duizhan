package pixelbattle.game;

public class CraftingRecipe {
    public ItemType result;
    public int resultCount;
    public ItemType[] ingredients;
    public int[] amounts;

    public CraftingRecipe(ItemType result, int resultCount, ItemType[] ingredients, int[] amounts) {
        this.result = result;
        this.resultCount = resultCount;
        this.ingredients = ingredients;
        this.amounts = amounts;
    }
}
