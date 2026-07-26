package mod.pilot.birch_n_bees.items.unique;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class BirchShieldItem extends ShieldItem {
    public BirchShieldItem(Properties properties) {
        super(properties
                .durability(128)
                .repairable(ItemTags.WOODEN_TOOL_MATERIALS)
                .equippableUnswappable(EquipmentSlot.OFFHAND)
                .delayedComponent(
                        DataComponents.BLOCKS_ATTACKS,
                        context -> new BlocksAttacks(
                                .5F,
                                0.75F,
                                List.of(new BlocksAttacks.DamageReduction(75.0F, Optional.empty(), 0.0F, 1.0F)),
                                new BlocksAttacks.ItemDamageFunction(1.0F, 1.0F, 1.5F),
                                Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                                Optional.of(SoundEvents.SHIELD_BLOCK),
                                Optional.of(SoundEvents.SHIELD_BREAK)
                        )
                )
                .component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK));
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        if (entity instanceof Player player) player.getCooldowns().addCooldown(stack, 40);
        super.onStopUsing(stack, entity, count);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
                                @NotNull TooltipFlag flag) {
        tooltipAdder.accept(Component.translatable("item.birch_n_bees.birch_shield.description"));
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
    }

    @Override
    public @NotNull ItemUseAnimation getUseAnimation(@NotNull ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }
}
