package mod.pilot.birch_n_bees.items.unique;

import mod.pilot.birch_n_bees.util.BirchTags;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class CrudeCobblestonePickaxeItem extends Item {
    public CrudeCobblestonePickaxeItem(Properties properties) {
        super(properties.pickaxe(getMaterial(), 1f, -2.8f));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
                                @NotNull TooltipFlag flag) {
        tooltipAdder.accept(Component.translatable("item.birch_n_bees.crude_cobblestone_pickaxe.description"));
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
    }

    private static ToolMaterial getMaterial(){
        return new ToolMaterial(BlockTags.INCORRECT_FOR_WOODEN_TOOL, 64, 2.0F, 0.0F, 10, BirchTags.Items.COBBLE_TIER_3);
    }
}
