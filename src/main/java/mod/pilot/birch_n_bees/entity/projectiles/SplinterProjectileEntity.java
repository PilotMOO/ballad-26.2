package mod.pilot.birch_n_bees.entity.projectiles;

import mod.pilot.birch_n_bees.items.BirchItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

public class SplinterProjectileEntity extends AbstractArrow implements ItemSupplier {
    public SplinterProjectileEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        pickup = Pickup.ALLOWED;
    }

    public static EntityDataAccessor<Float> DAMAGE =
            SynchedEntityData.defineId(SplinterProjectileEntity.class, EntityDataSerializers.FLOAT);
    public float getDamage(){return entityData.get(DAMAGE);}
    public void setDamage(float damage){entityData.set(DAMAGE, damage);}

    public static EntityDataAccessor<Boolean> NO_PICKUP =
            SynchedEntityData.defineId(SplinterProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    public boolean allowPickup(){return !entityData.get(NO_PICKUP);}
    public void setNoPickup(boolean flag){entityData.set(NO_PICKUP, flag);}

    public static EntityDataAccessor<Boolean> IGNORE_I_FRAMES =
            SynchedEntityData.defineId(SplinterProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    public boolean shouldIgnoreIFrames(){return entityData.get(IGNORE_I_FRAMES);}
    public void setIgnoreIFrames(boolean flag){entityData.set(IGNORE_I_FRAMES, flag);}
    public void setIgnoreIFrames(){setIgnoreIFrames(true);}

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DAMAGE, 3f);
        builder.define(NO_PICKUP, false);
        builder.define(IGNORE_I_FRAMES, false);
    }

    @Override
    public void addAdditionalSaveData(@NotNull ValueOutput compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("DAMAGE", getDamage());
        compound.putBoolean("NO_PICKUP", entityData.get(NO_PICKUP));
        compound.putBoolean("I_FRAME", entityData.get(IGNORE_I_FRAMES));
    }
    @Override
    public void readAdditionalSaveData(@NotNull ValueInput compound) {
        super.readAdditionalSaveData(compound);
        setDamage(compound.getFloatOr("DAMAGE", 3f));
        setNoPickup(compound.getBooleanOr("NO_PICKUP", false));
        setNoPickup(compound.getBooleanOr("I_FRAME", false));
    }

    @Override protected @NotNull ItemStack getDefaultPickupItem() { return BirchItems.SPLINTERS.toStack(); }
    @Override public @NotNull ItemStack getItem() { return getDefaultPickupItem(); }
    @Override protected @NotNull ItemStack getPickupItem() { return getDefaultPickupItem(); }

    @Override
    protected boolean tryPickup(@NotNull Player player) {
        if (allowPickup()) return super.tryPickup(player);
        else return false;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        Entity entity1 = getOwner();
        DamageSource damagesource = this.damageSources().arrow(this, (Entity)(entity1 != null ? entity1 : this));

        int iFrame = -1;
        if (shouldIgnoreIFrames()){
            iFrame = entity.invulnerableTime;
            entity.invulnerableTime = 0;
        }
        if (entity.hurtOrSimulate(damagesource, getDamage())) {
            if (entity instanceof EnderMan) return;

            if (entity instanceof LivingEntity) {
                LivingEntity livingentity = (LivingEntity)entity;

                this.doKnockback(livingentity, damagesource);
                Level var13 = this.level();
                if (var13 instanceof ServerLevel) {
                    ServerLevel serverlevel1 = (ServerLevel)var13;
                    EnchantmentHelper.doPostAttackEffectsWithItemSource(serverlevel1, livingentity, damagesource, this.getWeaponItem());
                }

                this.doPostHurtEffects(livingentity);
                if (livingentity instanceof Player && entity1 instanceof ServerPlayer) {
                    ServerPlayer serverplayer = (ServerPlayer)entity1;
                    if (!this.isSilent() && livingentity != serverplayer) {
                        serverplayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.PLAY_ARROW_HIT_SOUND, 0.0F));
                    }
                }

            }
            if (iFrame != -1) entity.invulnerableTime = iFrame;

            this.playSound(SoundEvents.ARROW_HIT, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            this.discard();
        } else {
            this.deflect(ProjectileDeflection.REVERSE, entity, this.owner, false);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.2));
            Level var21 = this.level();
            if (var21 instanceof ServerLevel serverlevel2) {
                if (this.getDeltaMovement().lengthSqr() < 1.0E-7) {
                    if (this.pickup == AbstractArrow.Pickup.ALLOWED) {
                        this.spawnAtLocation(serverlevel2, this.getPickupItem(), 0.1F);
                    }

                    this.discard();
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        int life = 1500;
        if (getDeltaMovement().lengthSqr() < 1.0E-7 && !allowPickup()) life /= 10;
        if (tickCount >= life) discard();
    }
}
