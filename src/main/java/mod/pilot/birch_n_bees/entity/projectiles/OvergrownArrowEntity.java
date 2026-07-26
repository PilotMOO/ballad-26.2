package mod.pilot.birch_n_bees.entity.projectiles;

import mod.pilot.birch_n_bees.entity.BirchEntities;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OvergrownArrowEntity extends AbstractArrow {
    public OvergrownArrowEntity(LivingEntity owner, Level level, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(BirchEntities.OVERGROWN_ARROW.get(), owner, level, pickupItemStack, firedFromWeapon);
    }
    public OvergrownArrowEntity(EntityType<? extends AbstractArrow> entityType, double x, double y, double z, Level level, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(entityType, x, y, z, level, pickupItemStack, firedFromWeapon);
    }
    public OvergrownArrowEntity(EntityType<? extends AbstractArrow> p_331098_, Level p_331626_) {
        super(p_331098_, p_331626_);
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        boolean flag = target.hasEffect(MobEffects.NAUSEA);
        if (!flag) {
            target.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 120));
        } else {
            MobEffectInstance effectInstance = target.getEffect(MobEffects.NAUSEA);
            int dur = effectInstance.getDuration();
            dur = Math.max(dur + 80, 200);
            target.addEffect(new MobEffectInstance(MobEffects.NAUSEA, dur));
        }
        boolean flag2 = target.hasEffect(MobEffects.POISON);
        if (!flag2) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 40));
        } else {
            MobEffectInstance effectInstance = target.getEffect(MobEffects.POISON);
            int dur = effectInstance.getDuration();
            dur = Math.max(dur + 30, 100);
            target.addEffect(new MobEffectInstance(MobEffects.POISON, dur));
        }
    }
}
