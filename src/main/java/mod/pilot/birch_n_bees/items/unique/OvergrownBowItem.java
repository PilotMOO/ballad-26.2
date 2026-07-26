package mod.pilot.birch_n_bees.items.unique;

import mod.pilot.birch_n_bees.entity.projectiles.OvergrownArrowEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class OvergrownBowItem extends BowItem {
    public OvergrownBowItem(Properties p_40660_) {
        super(p_40660_.durability(96));
    }

    @Override
    protected @NotNull Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo, boolean isCrit) {
        AbstractArrow abstractarrow = new OvergrownArrowEntity(shooter, level, ammo.copyWithCount(1), weapon);
        if (isCrit) {
            abstractarrow.setCritArrow(true);
        }
        return this.customArrow(abstractarrow, ammo, weapon);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
                                @NotNull TooltipFlag flag) {
        tooltipAdder.accept(Component.translatable("item.birch_n_bees.overgrown_bow.description"));
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
    }
}
