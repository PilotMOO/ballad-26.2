package mod.pilot.birch_n_bees.entity.mob;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import mod.pilot.birch_n_bees.entity.BirchEntities;
import mod.pilot.birch_n_bees.entity.projectiles.SplinterProjectileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class SplinteringEntity extends Monster implements GeoEntity {
    public SplinteringEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes(){
        return SplinteringEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 16D)
                .add(Attributes.ARMOR, 1)
                .add(Attributes.FOLLOW_RANGE, 32)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0D)
                .add(Attributes.ATTACK_SPEED, 2D);
    }

    public static final EntityDataAccessor<Integer> SWELL = SynchedEntityData.defineId(SplinteringEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IGNITED = SynchedEntityData.defineId(SplinteringEntity.class, EntityDataSerializers.BOOLEAN);
    public int getSwell(){return entityData.get(SWELL);}
    public void setSwell(int count) {
        entityData.set(SWELL, count);
    }
    public void syncSwell(){
        setSwell(local_swell);
    }
    public boolean getIgnited(){return entityData.get(IGNITED);}
    public void setIgnited(boolean flag) {
        entityData.set(IGNITED, flag);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("swell", getSwell());
        tag.putBoolean("ignited", getIgnited());
    }
    @Override
    public void readAdditionalSaveData(@NotNull ValueInput tag) {
        super.readAdditionalSaveData(tag);
        setSwell(tag.getInt("swell").orElse(0));
        setIgnited(tag.getBooleanOr("ignited", false));
    }
    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SWELL, 0);
        builder.define(IGNITED, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Ocelot.class, 6.0F, 1.0, 1.2));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Cat.class, 6.0F, 1.0, 1.2));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));

        this.goalSelector.addGoal(2, new SplinteringSwellGoal(this));
    }

    public int local_swell, oldSwell;
    public static final int maxSwell = 30;
    

    public void tick() {
        if (this.isAlive()) {
            this.oldSwell = this.local_swell;
            if (this.getIgnited() || getSwell() != 0) {
                int i = this.getSwell();
                if (local_swell == 0) local_swell = i;
                if (i > 0 && this.local_swell == 1) {
                    this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 1.0F);
                    this.gameEvent(GameEvent.PRIME_FUSE);
                }

                this.local_swell++;
                syncSwell();
            }

            if (this.local_swell >= maxSwell) {
                this.local_swell = maxSwell;
                this.explodeSplintering();
            }
        }

        super.tick();
    }


    public void explodeSplintering(){
        setSwell(local_swell = 0);
        this.dead = true;
        if (this.level() instanceof ServerLevel server){
            server.explode(this, getX(), getY(), getZ(),
                    .5f, Level.ExplosionInteraction.BLOCK);
            RandomSource random = server.getRandom();

            float xRot, yRot;
            for (int loop = 0; loop < 2; loop++) {
                xRot = -80;
                yRot = 0;
                for (int i = 0; i < 15; i++) {
                    xRot += 9f;
                    for (int j = 0; j < 8; j++) {
                        yRot += 11.25f; //90f/8
                        if (random.nextBoolean()) getSplinter(server, this,
                                xRot + random.nextInt(-50, 50) / 10f,
                                yRot + random.nextInt(-100, 100) / 10f);
                    }
                }
            }
            this.triggerOnDeathMobEffects(server, RemovalReason.KILLED);
            this.discard();
        }

    }

    public static SplinterProjectileEntity getSplinter(ServerLevel server, Entity from, float xRot, float yRot){
        SplinterProjectileEntity splinter = new SplinterProjectileEntity(BirchEntities.SPLINTER_PROJECTILE.get(), server);
        splinter.copyPosition(from);
        splinter.move(MoverType.SELF, new Vec3(0, from.getEyeHeight(), 0));
        splinter.setNoPickup(server.getRandom().nextDouble() > 0.05);
        splinter.setOwner(from);
        splinter.setDamage(3f);
        splinter.setIgnoreIFrames();
        splinter.shootFromRotation(from, xRot, yRot, 0.0F, 1.0F, 5.0F);
        server.addFreshEntity(splinter);
        return splinter;
    }

    public boolean isMoving(){
        Vec3 delta = getDeltaMovement();
        return delta.x != 0 || delta.z != 0;
    }
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("splintering", 2, event -> {
            if (local_swell != 0){
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("explode"));
            }
            else if (isMoving()){
                return event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            }
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return SoundEvents.CREAKING_DEATH;
    }
    @Override
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.AXE_STRIP;
    }
    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.CREAKING_AMBIENT;
    }

    public static boolean splinteringSpawnCheck(EntityType<? extends Monster> entityType, ServerLevelAccessor level,
                                                EntitySpawnReason spawnReason, BlockPos pos, RandomSource random){
        return level.getBiome(pos).is(Tags.Biomes.IS_BIRCH_FOREST) && Monster.checkMonsterSpawnRules(entityType, level, spawnReason, pos, random);
    }

    public static class SplinteringSwellGoal extends Goal {
        public final SplinteringEntity splintering;
        public SplinteringSwellGoal(SplinteringEntity splintering) {
            this.splintering = splintering;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }
        public boolean canUse() {
            LivingEntity livingentity = this.splintering.getTarget();
            return this.splintering.getSwell() > 0 || livingentity != null && this.splintering.distanceToSqr(livingentity) < (double)7.0F;
        }
        public void start() {
            splintering.setIgnited(true);
        }
    }
}
