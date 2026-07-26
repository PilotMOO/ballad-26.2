package mod.pilot.birch_n_bees.items.unique;

import mod.pilot.birch_n_bees.entity.BirchEntities;
import mod.pilot.birch_n_bees.entity.projectiles.SplinterProjectileEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class SplinterItem extends Item {
    public SplinterItem(Properties properties) {
        super(properties);
    }

    public @NotNull InteractionResult use(Level pLevel, Player pPlayer, @NotNull InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        pLevel.playSound((Player)null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL,
                0.5F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));

        if (!pLevel.isClientSide()) {
            SplinterProjectileEntity splinter = new SplinterProjectileEntity(BirchEntities.SPLINTER_PROJECTILE.get(), pLevel);
            splinter.setNoPickup(pPlayer.hasInfiniteMaterials());
            splinter.copyPosition(pPlayer);
            splinter.move(MoverType.SELF, new Vec3(0, pPlayer.getEyeHeight(), 0));
            splinter.setOwner(pPlayer);
            splinter.shootFromRotation(pPlayer, pPlayer.getXRot(), pPlayer.getYRot(), 0.0F, 1.5F, 2.0F);
            pLevel.addFreshEntity(splinter);
        }

        pPlayer.awardStat(Stats.ITEM_USED.get(this));
        if (!pPlayer.getAbilities().instabuild) {
            itemstack.shrink(1);
        }

        return InteractionResult.SUCCESS_SERVER;
    }
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
                                @NotNull TooltipFlag flag) {
        tooltipAdder.accept(Component.translatable("item.birch_n_bees.splinters.description"));
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
    }

}
