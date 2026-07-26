package mod.pilot.birch_n_bees.entity.mob;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import mod.pilot.birch_n_bees.data.BirchDataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class NestHeadEntity extends Zombie implements GeoEntity {
    public NestHeadEntity(EntityType<? extends Zombie> p_34271_, Level p_34272_) {
        super(p_34271_, p_34272_);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes(){
        return NestHeadEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 24D)
                .add(Attributes.ARMOR, 8)
                .add(Attributes.FOLLOW_RANGE, 32)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.ATTACK_DAMAGE, 4D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.1D)
                .add(Attributes.ATTACK_SPEED, 2D)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0D);
    }

    public static final int DEFAULT_BEES = 5;
    public static final EntityDataAccessor<Integer> BEES = SynchedEntityData.defineId(NestHeadEntity.class,
            EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> MAX_BEES = SynchedEntityData.defineId(NestHeadEntity.class,
            EntityDataSerializers.INT);
    public int getStoredBees() {return entityData.get(BEES);}
    public void setStoredBees(int count) {entityData.set(BEES, count);}
    public boolean maxBees(){ return getStoredBees() >= getMaxBees();}
    public void incBees() {
        int bees = getStoredBees();
        int max_bees = getMaxBees();
        if (++bees <= max_bees) setStoredBees(bees);
    }
    public void decBees() {
        int bees = getStoredBees();
        if (bees-- > 0) setStoredBees(bees);
    }

    public int getMaxBees(){return entityData.get(MAX_BEES);}
    public void setMaxBees(int count) {
        entityData.set(MAX_BEES, count);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("bees", getStoredBees());
        tag.putInt("max_bees", getMaxBees());
    }
    @Override
    public void readAdditionalSaveData(@NotNull ValueInput tag) {
        super.readAdditionalSaveData(tag);
        setStoredBees(tag.getInt("bees").orElse(DEFAULT_BEES));
        setMaxBees(tag.getInt("max_bees").orElse(DEFAULT_BEES));
    }
    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BEES, DEFAULT_BEES);
        builder.define(MAX_BEES, DEFAULT_BEES);
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();

        goalSelector.addGoal(4, new CollectBeesGoal(this));
    }

    @Override
    protected void tickDeath() {
        //Manually change the death timer from 1 sec (20 tick) to 2.5 sec (50 tick) to work with death animation
        ++this.deathTime;
        if (this.deathTime >= 50 && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte)60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        LivingEntity target;
        if (!(level() instanceof ServerLevel server) || (target = getTarget()) == null) return;
        if (tickCount % 100 == 0 && random.nextBoolean() && getStoredBees() > 0 && distanceTo(target) < 12) {
            decBees();
            BirchDataHelper.popBees(server, getEyePosition(), 1, target);
            playSound(SoundEvents.BEEHIVE_EXIT, 1f, .75f);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel server, DamageSource damageSource, float amount) {
        boolean flag = super.hurtServer(server, damageSource, amount);
        LivingEntity target;
        if (amount > 3 && getStoredBees() > 0 && (target = getTarget()) != null){
            if (amount > target.getMaxHealth() / 4 || random.nextDouble() < 0.25d) {
                int beeCount = Math.min(getStoredBees(), random.nextInt(4));
                BirchDataHelper.popBees(server, getEyePosition(), beeCount, target);
                setStoredBees(getStoredBees() - beeCount);
                playSound(SoundEvents.BEEHIVE_EXIT, 1f, .75f);
            }
        }
        return flag;
    }

    @Override
    public boolean doHurtTarget(@NotNull ServerLevel server, @NotNull Entity target) {
        boolean flag = super.doHurtTarget(server, target);
        if (flag && target instanceof LivingEntity living) {
            if (!living.hasEffect(MobEffects.SLOWNESS)) living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1));
        }
        return flag;
    }

    @Override
    protected void randomizeReinforcementsChance() {
        //I don't want to program reinforcement mechanics but if I leave this method untouched it will cause an error
    }

    public boolean isMoving(){
        Vec3 delta = getDeltaMovement();
        return delta.x != 0 || delta.z != 0;
    }
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("nesthead", 2, event -> {
            if (isDeadOrDying()){
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("die"));
            }
            else if (isMoving()){
                return event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            }
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    @Override
    public @NonNull AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_DEATH;
    }
    @Override
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.ZOMBIE_HURT;
    }
    @Override
    protected @NotNull SoundEvent getAmbientSound() {
        return SoundEvents.BEEHIVE_WORK;
    }
    @Override
    protected @NotNull SoundEvent getStepSound() {
        return SoundEvents.ZOMBIE_STEP;
    }

    public static boolean nestHeadSpawnCheck(EntityType<? extends Monster> entityType, ServerLevelAccessor level,
                                                EntitySpawnReason spawnReason, BlockPos pos, RandomSource random){
        return level.getBiome(pos).is(Tags.Biomes.IS_BIRCH_FOREST) && Monster.checkMonsterSpawnRules(entityType, level, spawnReason, pos, random);
    }

    public static class CollectBeesGoal extends Goal{
        public final NestHeadEntity entity;
        public Bee target;
        public BlockPos nest;
        public CollectBeesGoal(NestHeadEntity entity) {
            this.entity = entity;
        }

        @Override
        public boolean canUse() {
            return !entity.maxBees() && entity.getTarget() == null;
        }

        @Override
        public boolean canContinueToUse() {
            return (target != null || nest != null) && !entity.maxBees();
        }

        @Override
        public void start() {
            AABB area = entity.getBoundingBox().inflate(16d, 4d, 16d);
            List<Bee> bees = entity.level().getEntities(EntityTypes.BEE, area,
                    (bee) -> bee.getTarget() == null);
            if (bees.isEmpty()){
                BlockPos pos = entity.blockPosition();
                Level level = entity.level();
                ArrayList<BlockPos> nests = new ArrayList<>();
                for (int x = -16; x <= 16; x++) {
                    for (int y = -16; y <= 16; y++) {
                        for (int z = -16; z <= 16; z++) {
                            BlockPos bPos = pos.offset(x,y,z);
                            BlockState bState = level.getBlockState(bPos);
                            if (bState.is(Blocks.BEE_NEST)) {
                                BeehiveBlockEntity beehive = (BeehiveBlockEntity) level.getBlockEntity(bPos);
                                if (beehive != null && beehive.getOccupantCount() > 0) nests.add(bPos);
                            }
                        }
                    }
                }
                if (!nests.isEmpty()) {
                    BlockPos bPos = null;
                    double dist = Double.MAX_VALUE;
                    for (BlockPos bPos1 : nests) {
                        double dist1 = entity.distanceToSqr(
                                new Vec3(bPos1.getX() + 0.5, bPos1.getY() + 0.5, bPos1.getZ() + 0.5));
                        if (dist1 < dist) {
                            dist = dist1;
                            bPos = bPos1;
                        }
                    }
                    if ((nest = bPos) != null) {
                        entity.navigation.moveTo(nest.getX(), nest.getY(), nest.getZ(), 2, 1d);
                    }
                }
            }
            else{
                Bee bee = null;
                double dist = Double.MAX_VALUE;
                for (Bee bee1 : bees) {
                    double dist1 = entity.distanceTo(bee1);
                    if (dist1 < dist) {
                        dist = dist1;
                        bee = bee1;
                    }
                }
                if (bee != null) entity.navigation.moveTo(target = bee, 1d);
            }

        }

        @Override
        public void tick() {
            PathNavigation nav = entity.navigation;
            if (nav.isStuck()) {
                stop();
                return;
            }

            if (target != null){
                if (entity.distanceTo(target) < 2d){
                    entity.incBees();
                    target.discard();
                    entity.level().playSound(entity, entity, SoundEvents.BEEHIVE_ENTER, SoundSource.HOSTILE, 1f, 1.25f);
                    stop();
                } else if (nav.isDone()) nav.moveTo(target, 1d);
            } else if (nest != null){
                Vec3 nestCenter = new Vec3(nest.getX() + 0.5, nest.getY() + 0.5, nest.getZ() + 0.5);
                if (entity.distanceToSqr(nestCenter) < 6.5d){
                    Level level = entity.level();
                    BlockEntity bEntity = entity.level().getBlockEntity(nest);
                    if (bEntity instanceof BeehiveBlockEntity beehive) {
                        if (beehive.getOccupantCount() > 0) {
                            boolean collectFlag = false;
                            int toCollect = entity.getMaxBees() - entity.getStoredBees();
                            beehive.emptyAllLivingFromHive(null, level.getBlockState(nest), BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
                            for (Bee bee : level.getEntities(EntityTypes.BEE, AABB.ofSize(nestCenter, 2, 2,2), (b) -> true)){
                                if (--toCollect <= 0) break;
                                bee.discard();
                                entity.incBees();
                                collectFlag = true;
                            }
                            if (collectFlag) level.playSound(entity, nest, SoundEvents.BEEHIVE_ENTER, SoundSource.HOSTILE, 1f, 1.25f);
                            stop();
                        }
                    }
                }
            } else stop();
        }

        @Override
        public void stop() {
            target = null;
            nest = null;
            entity.navigation.stop();
        }
    }
}
