package mod.pilot.birch_n_bees.items.unique;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TestingToolBase extends BuildableToolBase{
    public TestingToolBase(Properties properties) {
        super(properties, 80);
    }

    @Override
    public void fillValidHeads() {
        validHeads = new ToolHead[3];
        validHeads[0] = new ToolHead(Items.DIRT, new ItemStack(Items.DIRT, 2));
        validHeads[1] = new ToolHead(Items.GOLD_NUGGET, new ItemStack(Items.GOLD_INGOT));
        validHeads[2] = new ToolHead(Items.WHEAT, new ItemStack(Items.IRON_AXE));
    }
}
