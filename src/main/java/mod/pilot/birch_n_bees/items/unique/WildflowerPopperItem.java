package mod.pilot.birch_n_bees.items.unique;

import mod.pilot.birch_n_bees.entity.BirchEntities;
import mod.pilot.birch_n_bees.entity.projectiles.WildflowerPopperProjectileEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class WildflowerPopperItem extends Item implements ProjectileItem {
    public WildflowerPopperItem(Properties properties) {
        super(properties);
    }

    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        level.playSound((Entity)null, player.getX(), player.getY(), player.getZ(), SoundEvents.TNT_PRIMED, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (level instanceof ServerLevel) {
            Projectile prj = asProjectile(level, player.getEyePosition(), player.getItemInHand(hand), player.getNearestViewDirection());
            prj.setOwner(player);
            prj.shootFromRotation(player, player.getXRot(), player.getYRot(), .0f, .5f, 1.0f);
            level.addFreshEntity(prj);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        itemstack.consume(1, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull Projectile asProjectile(@NotNull Level level, Position position,
                                            @NotNull ItemStack itemStack, @NotNull Direction direction) {
        WildflowerPopperProjectileEntity popper =
                new WildflowerPopperProjectileEntity(BirchEntities.WILDFLOWER_POPPER_PROJECTILE.get(), level);
        popper.setPos(position.x(), position.y(), position.z());
        return popper;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
                                @NotNull TooltipFlag flag) {
        tooltipAdder.accept(Component.translatable("item.birch_n_bees.wildflower_popper.description"));
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
    }
}
