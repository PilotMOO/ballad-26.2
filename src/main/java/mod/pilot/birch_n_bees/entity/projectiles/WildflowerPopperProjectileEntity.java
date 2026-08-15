package mod.pilot.birch_n_bees.entity.projectiles;

import mod.pilot.birch_n_bees.events.BalladGameEvents;
import mod.pilot.birch_n_bees.items.BirchItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class WildflowerPopperProjectileEntity extends Projectile implements ItemSupplier {
    public WildflowerPopperProjectileEntity(EntityType<? extends Projectile> p_37248_, Level p_37249_) {
        super(p_37248_, p_37249_);
    }
    public WildflowerPopperProjectileEntity(Level level, LivingEntity owner, ItemStack item) {
        super(EntityTypes.SNOWBALL, level);
        setOwner(owner);
    }

    public static final int MAX_FUSE = 60;
    public static final EntityDataAccessor<Integer> FUSE =
            SynchedEntityData.defineId(WildflowerPopperProjectileEntity.class, EntityDataSerializers.INT);
    public int getFuse(){
        return entityData.get(FUSE);
    }
    public void setFuse(int value){
        entityData.set(FUSE, value);
    }
    public boolean tickFuse(){
        int fuse = getFuse();
        boolean flag = ++fuse >= MAX_FUSE;
        setFuse(fuse);
        return flag;
    }
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FUSE, 0);
    }
    @Override
    protected void addAdditionalSaveData(@NotNull ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("FUSE", getFuse());
    }
    @Override
    protected void readAdditionalSaveData(@NotNull ValueInput tag) {
        super.readAdditionalSaveData(tag);
        setFuse(tag.getIntOr("FUSE", 0));
    }

    @Override
    public void tick() {
        positionManagement();
        super.tick();

        if (tickFuse()) popFuse();
        else if (tickCount % 2 == 0 && level().isClientSide()){
            Vec3 pos = position();
            RandomSource rand = getRandom();
            level().addParticle(ParticleTypes.SMOKE,
                    pos.x, pos.y + .3, pos.z,
                    (rand.nextBoolean() ? 1 : -1) * (rand.nextDouble() / 75),
                    0.05,
                    (rand.nextBoolean() ? 1 : -1) * (rand.nextDouble() / 75));
        }
    }

    private void popFuse() {
        if (level() instanceof ServerLevel server){
            Vec3 pos = position();
            server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y, pos.z,
                    2,
                    .25, .25, .25, 0);
            server.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.75f, 2f);

            Level level = level();
            AABB aabb = AABB.ofSize(pos, 2.5, 2.5, 2.5);
            AtomicBoolean foundStone = new AtomicBoolean(false);
            AtomicBoolean poppedStone = new AtomicBoolean(false);
            for (BlockPos bPos : BlockPos.betweenClosed(aabb)){
                BlockState bState = level.getBlockState(bPos);

                Vec3 to = pos.subtract(new Vec3(bPos.getX() + 0.5, bPos.getY() + 0.5, bPos.getZ() + 0.5)).normalize();
                Direction face = Direction.getApproximateNearest(to);

                AtomicBoolean breakIf = new AtomicBoolean();
                Direction.stream().forEach((direction) -> {
                    if (!breakIf.get()) {
                        BlockPos bPos1 = bPos.relative(direction);
                        if (level.getBlockState(bPos1).isAir()) {
                            BalladGameEvents.Stones stone;
                            if ((stone = BalladGameEvents.Stones.fromBlock(bState)) != null) {
                                foundStone.set(true);
                                if (stone.tier <= 1) {
                                    poppedStone.set(true);
                                    if (level.getRandom().nextInt(2) == 0) BalladGameEvents.popStone(face, level, bPos, stone);
                                }
                                breakIf.set(true);
                            }
                        }
                    }
                });
            }
            if (foundStone.get() && !poppedStone.get()){
                Player player = server.getNearestPlayer(this, -1);
                if (player != null) player.sendSystemMessage(Component.translatable("birch_n_bees.message.stone_too_hard"));
            }
            for (LivingEntity le : level.getEntitiesOfClass(LivingEntity.class, aabb.inflate(2))){
                float dmg = 10;
                double dist = le.distanceTo(this);
                double aabbDistHalf = aabb.getSize() / 2;
                if (dist > aabbDistHalf) dmg *= 1f / (float)(dist - aabbDistHalf);
                le.hurtServer(server, damageSources().explosion(this, getOwner()), dmg);
            }
        }
        discard();
    }

    private void positionManagement(){
        boolean flag = horizontalCollision || verticalCollision;
        if (!flag) {
            applyGravity();
            this.applyInertia();
        } else setDeltaMovement(Vec3.ZERO);
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);

        Vec3 vec3;
        if (hitresult.getType() != HitResult.Type.MISS && !net.neoforged.neoforge.event.EventHooks.onProjectileImpact(this, hitresult)) {
            vec3 = hitresult.getLocation();
            setDeltaMovement(Vec3.ZERO);
        } else if (flag){
            vec3 = position();
        } else {
            vec3 = this.position().add(this.getDeltaMovement());
        }
        this.setPos(vec3);

        this.updateRotation();
    }

    @Override
    protected double getDefaultGravity() {
        return horizontalCollision || verticalCollision ? 0 : 0.03;
    }

    //Ported from ThrowableProjectile
    private void applyInertia() {
        Vec3 vec3 = this.getDeltaMovement();
        Vec3 vec31 = this.position();
        float f;
        if (this.isInWater()) {
            for (int i = 0; i < 4; i++) {
                this.level()
                        .addParticle(ParticleTypes.BUBBLE, vec31.x - vec3.x * 0.25, vec31.y - vec3.y * 0.25, vec31.z - vec3.z * 0.25, vec3.x, vec3.y, vec3.z);
            }

            f = 0.8F;
        } else {
            f = 0.99F;
        }

        this.setDeltaMovement(vec3.scale(f));
    }

    @Override
    public @NotNull ItemStack getItem() {
        return BirchItems.WILDFLOWER_POPPER.get().getDefaultInstance();
    }
}
