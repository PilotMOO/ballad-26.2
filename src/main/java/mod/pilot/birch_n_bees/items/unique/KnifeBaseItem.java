package mod.pilot.birch_n_bees.items.unique;

import mod.pilot.birch_n_bees.items.BirchItems;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KnifeBaseItem extends Item {
    public KnifeTier tier;

    public KnifeBaseItem(Properties properties, KnifeTier tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        ItemStack offhand = entity.getOffhandItem();
        if (stack == offhand) offhand = entity.getMainHandItem();
        return getCutItem(offhand) != null ? 40 : 0;
    }

    @Override
    public @NotNull ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BUNDLE;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack offhand;
        if (hand == InteractionHand.MAIN_HAND) offhand = player.getOffhandItem();
        else offhand = player.getMainHandItem();
        if (getCutItem(offhand) != null){
            player.startUsingItem(hand);
            player.playSound(SoundEvents.SCAFFOLDING_PLACE, 1.0F, .5F);
            return InteractionResult.SUCCESS;
        } else{
            player.stopUsingItem();
            player.playSound(SoundEvents.BUBBLE_POP, 1.0F, .5F);
            player.sendOverlayMessage(Component.translatable(
                            offhand.isEmpty() ? "birch_n_bees.message.nothing_to_cut" : "birch_n_bees.message.not_cuttable"));
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 5);
            return InteractionResult.FAIL;
        }
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, LivingEntity entity) {
        ItemStack offhand = entity.getOffhandItem();
        boolean fromOffhand = false;
        if (stack.equals(offhand)){
            offhand = entity.getMainHandItem();
            fromOffhand = true;
        }
        ItemStack result;
        if ((result = getCutItem(offhand)) != null) {
            offhand.shrink(1);
            entity.playSound(SoundEvents.BAMBOO_BREAK, .75f, 1.25f);
            stack.hurtAndBreak(5, entity, fromOffhand ? EquipmentSlot.OFFHAND :  EquipmentSlot.MAINHAND);
            if (entity instanceof Player p){
                p.addItem(result);
            } else {
                Vec3 pos = entity.position();
                ItemEntity itemEntity = new ItemEntity(level,pos.x, pos.y, pos.z, result);
                level.addFreshEntity(itemEntity);
            }
        }
        return stack;
    }

    private ItemStack getCutItem(ItemStack stack) {
        return tier.getCutItem(stack);
    }

    public abstract static class KnifeTier{
        public abstract @Nullable ItemStack getCutItem(ItemStack stack);

        public static class Flint extends KnifeTier{
            @Override
            public @Nullable ItemStack getCutItem(ItemStack stack) {
                if (stack.is(Items.SUGAR_CANE)) return new ItemStack(BirchItems.PREPARED_SUGARCANE.get());
                if (stack.is(BirchItems.WILDFLOWER_TWINE)) return new ItemStack(BirchItems.STRIPPED_WILDFLOWER_TWINE.get());
                return null;
            }
        }
    }
}
