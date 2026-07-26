package mod.pilot.birch_n_bees.items.unique;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class WildflowerDressingItem extends Item {
    public WildflowerDressingItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 10;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        if (player.hasEffect(MobEffects.POISON)){
            player.startUsingItem(hand);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity livingEntity, @NotNull ItemStack stack, int remainingUseDuration) {
        if (remainingUseDuration % 6 == 0) level.playLocalSound(livingEntity, SoundEvents.HONEY_DRINK.value(), SoundSource.PLAYERS, 1f, 1.25f);
    }
    @Override
    public void onStopUsing(@NotNull ItemStack stack, LivingEntity entity, int count) {
        if (count != 0) return;
        if (entity.hasEffect(MobEffects.POISON)){
            entity.removeEffect(MobEffects.POISON);
            stack.shrink(1);
            if (entity instanceof Player player) player.getCooldowns().addCooldown(stack, 10);
        }
    }

    @Override
    public @NotNull ItemUseAnimation getUseAnimation(@NotNull ItemStack stack) {
        return ItemUseAnimation.BRUSH;
    }
}
