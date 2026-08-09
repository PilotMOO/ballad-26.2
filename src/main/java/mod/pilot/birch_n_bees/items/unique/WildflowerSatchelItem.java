package mod.pilot.birch_n_bees.items.unique;

import mod.pilot.birch_n_bees.data.InputReader;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class WildflowerSatchelItem extends BundleItem {
    public WildflowerSatchelItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(@NonNull ItemStack itemStack, @NonNull ServerLevel level,
                              @NonNull Entity owner, @Nullable EquipmentSlot slot) {
        float chance = 0f;
        boolean sprinting = owner.isSprinting();
        if (Math.abs(owner.getDeltaMovement().y) > 0.5){
            chance = 0.2f;
            if (sprinting) chance *= 2f;
        } else if (sprinting && owner.tickCount % 20 == 0) chance = 0.025f;

        if (chance != 0f){
            RandomSource random = owner.getRandom();
            float rand = random.nextFloat();
            if (chance < rand)return;

            BundleContents contents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
            if (contents.isEmpty()) return;
            BundleContents.Mutable mutable = new BundleContents.Mutable(contents);

            int randomIndex = random.nextInt(contents.size());
            mutable.toggleSelectedItem(randomIndex);
            ItemStack dropped = mutable.removeOne();
            if (dropped == null) return;
            int count = random.nextInt(0,4);
            if (count == 0) return;
            ItemStack delta = dropped.split(count);

            if (owner instanceof LivingEntity le) le.drop(delta, true, true);
            else {
                ItemEntity itemEntity = new ItemEntity(level, owner.getX(), owner.getY(), owner.getZ(), delta);
                level.addFreshEntity(itemEntity);
            }
            mutable.tryInsert(dropped);
            itemStack.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
            level.playSound(null, owner.blockPosition(), SoundEvents.BUNDLE_REMOVE_ONE, SoundSource.PLAYERS,
                    1f, 0.8F + owner.level().getRandom().nextFloat() * 0.6F);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
                                @NotNull TooltipFlag flag) {
        if (InputReader.leftControl()){
            tooltipAdder.accept(Component.translatable("item.birch_n_bees.wildflower_satchel.description.advanced"));
        }
        else tooltipAdder.accept(Component.translatable("item.birch_n_bees.wildflower_satchel.description"));
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
    }
}
