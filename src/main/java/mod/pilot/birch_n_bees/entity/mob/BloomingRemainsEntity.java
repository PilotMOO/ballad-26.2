package mod.pilot.birch_n_bees.entity.mob;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import mod.pilot.birch_n_bees.entity.projectiles.OvergrownArrowEntity;
import mod.pilot.birch_n_bees.items.BirchItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class BloomingRemainsEntity extends AbstractSkeleton implements GeoEntity {
    public BloomingRemainsEntity(EntityType<? extends AbstractSkeleton> p_32133_, Level p_32134_) {
        super(p_32133_, p_32134_);
    }

    public static final float FLEE_DISTANCE = 16f;

    public static AttributeSupplier.@NotNull Builder createAttributes(){
        return BloomingRemainsEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 16D)
                .add(Attributes.ARMOR, 1)
                .add(Attributes.FOLLOW_RANGE, 32)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 3D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.1D)
                .add(Attributes.ATTACK_SPEED, 2D);
    }

    private static final int POLLEN_COOLDOWN_MAX = 240;
    private static final EntityDataAccessor<Boolean> SCARED = SynchedEntityData.defineId(BloomingRemainsEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> POLLEN_COOLDOWN = SynchedEntityData.defineId(BloomingRemainsEntity.class, EntityDataSerializers.INT);
    public boolean isScared(){return entityData.get(SCARED);}
    public void setScared(boolean flag) {
    entityData.set(SCARED, flag);
    }
    public int getPollenCooldown(){return entityData.get(POLLEN_COOLDOWN);}
    public void setPollenCooldown(int i){
        entityData.set(POLLEN_COOLDOWN,i);
    }
    public void incCooldown(){
        int cooldown = getPollenCooldown();
        if (cooldown > 0) setPollenCooldown(cooldown-1);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("scared", isScared());
        output.putInt("pollen", getPollenCooldown());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setScared(input.getBooleanOr("scared", false));
        setPollenCooldown(input.getInt("pollen").orElse(0));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SCARED, false);
        builder.define(POLLEN_COOLDOWN, 0);
    }

    public int hurtTimestamp = -81;

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(4,  new RangedBowAttackGoal<BloomingRemainsEntity>(this,
                1.0F, 15, 15.0F){
            @Override
            public boolean canContinueToUse() {
                return !BloomingRemainsEntity.this.isScared() && super.canContinueToUse();
            }

            @Override
            public void stop() {
                super.stop();
                setAggressive(false);
            }
        });
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, LivingEntity.class,
                (entity) -> entity == getTarget() || entity == getLastHurtByMob(),
                FLEE_DISTANCE, 1.25D, 1.5D, (a) -> true){
            @Override
            public boolean canUse() {
                if (tickCount - hurtTimestamp < 80 || !mob.isHolding((is) -> is.getItem() instanceof BowItem)) {
                    return super.canUse();
                } else return false;
            }

            @Override
            public void start() {
                super.start();
                BloomingRemainsEntity.this.setScared(true);
                setAggressive(false);

                if (getPollenCooldown() == 0){
                    for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(6),
                            (entity) -> !(entity instanceof BloomingRemainsEntity))){
                        entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60));
                        entity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 160));
                        entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80));
                        entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20));
                    }
                    playSound(SoundEvents.WOOL_PLACE, 2f, 1.5f);
                    playSound(SoundEvents.BONE_MEAL_USE, 1.25f, 0.75f);
                    if (level() instanceof ServerLevel server){
                        RandomSource random = mob.getRandom();
                        for(int i = 0; i < 15; ++i) {
                            server.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                                    this.mob.getRandomX(2F),
                                    this.mob.getRandomY() + (double)1.5F,
                                    this.mob.getRandomZ(2F), 3,
                                    0.3, 0.3, 0.3,
                                    random.nextGaussian() * 0.25);
                        }
                    }
                    setPollenCooldown(POLLEN_COOLDOWN_MAX);
                }
                //Add pollen blinding effect
            }
            @Override
            public void stop() {
                super.stop();
                BloomingRemainsEntity.this.setScared(false);
            }
        });

        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Wolf.class, 6.0F, 1.0F, 1.2));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0F));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    @Override protected int getAttackInterval() {return 30;}
    @Override protected int getHardAttackInterval() {return 15;}


    //We ***MIGHT*** have to recode this to respect the overgrown bow's projectile...
    @Override
    public void performRangedAttack(@NotNull LivingEntity target, float distanceFactor) {
        ItemStack weapon = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, (item) -> item instanceof BowItem));
        ItemStack itemstack1 = this.getProjectile(weapon);
        AbstractArrow abstractarrow = this.getArrow(itemstack1, distanceFactor, weapon);
        Item var7 = weapon.getItem();
        if (var7 instanceof ProjectileWeaponItem weaponItem) {
            abstractarrow = weaponItem.customArrow(abstractarrow, itemstack1, weapon);
        }

        double d0 = target.getX() - this.getX();
        double d1 = target.getY(0.3333333333333333) - abstractarrow.getY();
        double d2 = target.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        Level var15 = this.level();
        if (var15 instanceof ServerLevel serverlevel) {
            Projectile.spawnProjectileUsingShoot(abstractarrow, serverlevel, itemstack1, d0, d1 + d3 * (double)0.2F, d2, 1.2F /*<-- WE CHANGED ARROW VELOCITY HERE*/, (float)(14 - serverlevel.getDifficulty().getId() * 4));
        }

        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }
    @Override
    protected @NotNull AbstractArrow getArrow(ItemStack arrow, float velocity, @Nullable ItemStack weapon) {
        if (weapon != null && weapon.is(BirchItems.OVERGROWN_BOW.get())){
            AbstractArrow overgrown = new OvergrownArrowEntity(this, level(), arrow.copyWithCount(1), weapon);
            overgrown.setBaseDamageFromMob(velocity / 1.5f);
            return overgrown;
        }
        return super.getArrow(arrow, velocity, weapon);
    }

    @Override
    protected void markHurt() {
        super.markHurt();
        if (getTarget() != null) hurtTimestamp = tickCount;
    }

    @Override
    public void aiStep() {
        if (tickCount % 60 == 0 && getHealth() <= getMaxHealth() - 1 && isInSunlight()) {
            this.heal(1f);
            playSound(SoundEvents.BONE_MEAL_USE, 0.5f, 1.5f);
            if (level().isClientSide()){
                for(int i = 0; i < 5; ++i) {
                    this.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                            this.getRandomX(1.0F),
                            this.getRandomY() + (double)1.0F,
                            this.getRandomZ(1.0F),
                            this.random.nextGaussian() * 0.02,
                            this.random.nextGaussian() * 0.02,
                            this.random.nextGaussian() * 0.02);
                }
            }
        }
        incCooldown();
        if (isScared() && tickCount % 20 == 0 && tickCount - hurtTimestamp > 300) setScared(false);
        super.aiStep();
    }

    public boolean isInSunlight(){
        //Similar to the isSunBurnTick() method, but we don't care about being in water
        return !level().isDarkOutside() && !level().isRaining() &&
                level().getBrightness(LightLayer.SKY, BlockPos.containing(position())) > 12;
    }

    /*@Override
    protected boolean isSunBurnTick() {
        return false; //We don't want to be burning in the sun
    }*/

    @Override
    protected EquipmentSlot sunProtectionSlot() {
        return super.sunProtectionSlot();
    }

    @Override
    public void reassessWeaponGoal() {
        //We don't enter melee mode, so we don't need this
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random, difficulty);
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(BirchItems.OVERGROWN_BOW.get()));
        setDropChance(EquipmentSlot.MAINHAND, 0.05f);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel p_348683_, DamageSource p_21385_, boolean p_21387_) {
        super.dropCustomDeathLoot(p_348683_, p_21385_, p_21387_);
    }

    @Override
    protected @NotNull SoundEvent getStepSound() {
        return SoundEvents.MUD_STEP;
    }
    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.BOGGED_AMBIENT;
    }
    @Override
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.BOGGED_HURT;
    }
    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return SoundEvents.BOGGED_DEATH;
    }

    public static boolean bloomingRemainsSpawnCheck(EntityType<? extends Monster> entityType, ServerLevelAccessor level,
                                             EntitySpawnReason spawnReason, BlockPos pos, RandomSource random){
        return level.getBiome(pos).is(Tags.Biomes.IS_BIRCH_FOREST) && Monster.checkMonsterSpawnRules(entityType, level, spawnReason, pos, random);
    }

    //GECKOLIB STUFF
    public boolean isMoving(){
        Vec3 delta = getDeltaMovement();
        return Math.abs(delta.x) > 0.01 || Math.abs(delta.z) > 0.01;
    }
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("blooming_remains", 2, event -> {
            //I can worry about a death animation later...
            /*if (isDeadOrDying()){
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("die"));
            }
            else */if (isMoving()){
                return event.setAndContinue(RawAnimation.begin().thenLoop(isScared() ? "run" : "walk_base"));
            }
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
        controllers.add(new AnimationController<>("blooming_arm_movement", 2, event -> {
            if (!isAggressive() && !isScared() && isMoving()){
                return event.setAndContinue(RawAnimation.begin().thenLoop("walk_arms"));
            } else return PlayState.STOP;
        }));
    }
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    @Override
    public @NonNull AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
