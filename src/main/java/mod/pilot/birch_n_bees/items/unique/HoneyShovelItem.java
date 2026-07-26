package mod.pilot.birch_n_bees.items.unique;

import mod.pilot.birch_n_bees.util.BirchTags;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class HoneyShovelItem extends ShovelItem {
    public HoneyShovelItem(Properties properties) {
        super(getMaterial(), .5f, -2.8f, properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
                                @NotNull TooltipFlag flag) {
        tooltipAdder.accept(Component.translatable("item.birch_n_bees.honey_shovel.description"));
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
    }

    private static ToolMaterial getMaterial(){
        return new ToolMaterial(BlockTags.INCORRECT_FOR_WOODEN_TOOL, 32, 2.0F, 0.0F, 7, BirchTags.Items.HONEYCOMB_CRAFTING);
    }
}
