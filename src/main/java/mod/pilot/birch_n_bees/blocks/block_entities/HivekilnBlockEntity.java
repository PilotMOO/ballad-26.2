package mod.pilot.birch_n_bees.blocks.block_entities;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import mod.pilot.birch_n_bees.blocks.BirchBlocks;
import mod.pilot.birch_n_bees.blocks.unique.HivekilnBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Supplier;

public class HivekilnBlockEntity extends BlockEntity implements GeoBlockEntity {
    public HivekilnBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BirchBlocks.HIVEKILN_BLOCK_ENTITY.get(), worldPosition, blockState);
    }

    public boolean clientOpenFlag = false;
    public boolean animateOpen = false;
    public void animateOpen(){
        animateOpen = true;
        getLevel().playLocalSound(worldPosition, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.BLOCKS,
                1.25f, 0.75f, false);
    }
    public void animateClose(){
        animateOpen = false;
        getLevel().playLocalSound(worldPosition, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS,
                1.25f, 1.25f, false);
    }

    public static AABB
            COLLECTION_NORTH = computeCollectionAreaByDirection(Direction.NORTH),
            COLLECTION_SOUTH = computeCollectionAreaByDirection(Direction.SOUTH),
            COLLECTION_EAST = computeCollectionAreaByDirection(Direction.EAST),
            COLLECTION_WEST = computeCollectionAreaByDirection(Direction.WEST);
    public LazyAccessor<AABB>
            collectionN = new LazyAccessor<>(() -> COLLECTION_NORTH.move(getBlockPos())),
            collectionS = new LazyAccessor<>(() -> COLLECTION_SOUTH.move(getBlockPos())),
            collectionE = new LazyAccessor<>(() -> COLLECTION_EAST.move(getBlockPos())),
            collectionW = new LazyAccessor<>(() -> COLLECTION_WEST.move(getBlockPos()));
    protected Direction lastEvaluatedFacing;
    public void invalidateAllCollectionAccessors(){
        collectionN.flagForRecompute();
        collectionS.flagForRecompute();
        collectionE.flagForRecompute();
        collectionW.flagForRecompute();
    }
    //Is this needed? No, but I already coded the collectionAreaAABB logic and I didn't want to hardcode it
    // so this was my compromise lmao. It does not need this much abstraction, but I genuinely don't care :)
    public static class LazyAccessor<T> {
        private T obj;
        private final Supplier<T> supplier;
        public LazyAccessor(Supplier<T> supplier){this.supplier = supplier;}
        public T get(){return obj != null ? obj : (obj = supplier.get());}
        public void flagForRecompute(){obj = null;}
    }
    private static AABB computeCollectionAreaByDirection(Direction d){
        int stepX = d.getStepX(), stepZ = d.getStepZ();
        double offZ = stepZ != 0 ? -0.3d * stepZ : 0.4 * stepX;
        double offX = stepX != 0 ? 0.3d * stepX : -0.4 * stepZ;
        return new AABB(
                -0.1875d + offX, 1d, -0.1875d + offZ,
                0.8125d + offX, 1.2d, 0.8125d + offZ
        );
    }
    public AABB collectionAreaByDirection(Direction d){
        return switch (d){
            case SOUTH -> collectionS.get();
            case WEST -> collectionE.get();
            case EAST -> collectionW.get();
            default -> collectionN.get();
        };
    }

    public static void serverTick(ServerLevel level, BlockPos pos,
                                  BlockState state, HivekilnBlockEntity entity) {
        if (!state.is(BirchBlocks.HIVEKILN) || !state.getValue(HivekilnBlock.OPEN)) return;

        Direction d = state.getValue(HorizontalDirectionalBlock.FACING);
        if (!d.equals(entity.lastEvaluatedFacing)){
            entity.invalidateAllCollectionAccessors();
            entity.lastEvaluatedFacing = d;
        }
        AABB collection = entity.collectionAreaByDirection(d);
        List<Entity> inCollection = level.getEntities((Entity)null, collection,
                (e) -> e instanceof ItemEntity);

        if (inCollection.isEmpty()) return;
        for (Entity value : inCollection){
            ItemEntity itemEntity = (ItemEntity)value;
            ItemStack stack = itemEntity.getItem();
            int burnTime;
            if ((burnTime = itemEntity.getItem().getBurnTime(null, level.fuelValues())) > 0){
                int itemFuelValue = burnTime / 100;
                if (itemFuelValue > 0){
                    int kilnFuel = state.getValue(HivekilnBlock.FUEL);
                    if (kilnFuel == HivekilnBlock.MAX_FUEL) break;
                    int fuelDelta = HivekilnBlock.MAX_FUEL - kilnFuel;
                    int maxAdditions = (fuelDelta / itemFuelValue);
                    if (fuelDelta % itemFuelValue != 0) maxAdditions++;
                    if (maxAdditions > 0){
                        int toShrink = Math.min(maxAdditions, stack.count());
                        int _test = kilnFuel;
                        kilnFuel = Math.min(HivekilnBlock.MAX_FUEL, kilnFuel + (toShrink * itemFuelValue));
                        stack.shrink(toShrink);
                        level.setBlock(pos, state.setValue(HivekilnBlock.FUEL, kilnFuel), 3);
                        RandomSource random = level.getRandom();
                        level.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT, SoundSource.BLOCKS,
                                0.75f + ((random.nextFloat() - 0.5f) / 2f), 0.75f);
                        level.sendParticles(ParticleTypes.COMPOSTER,
                                collection.minX + (collection.getXsize() / 2),
                                collection.maxY + 0.1d,
                                collection.minZ + (collection.getZsize() / 2),
                                6,
                                0.2d, 0.2d, 0.2d,
                                0.3d);
                        if (kilnFuel == HivekilnBlock.MAX_FUEL) break;
                    }
                }
            }
        }
    }
    public static void clientTick(Level level, BlockPos pos, BlockState state, HivekilnBlockEntity entity) {
        boolean flag = state.getValue(HivekilnBlock.OPEN);
        if (flag != entity.clientOpenFlag){
            if (flag) entity.animateOpen();
            else entity.animateClose();
            entity.clientOpenFlag = flag;
        }
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("openFlag", animateOpen);
    }
    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        animateOpen = input.getBooleanOr("openFlag", getBlockState().getValue(HivekilnBlock.OPEN));
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("hivekiln", 0, event ->
                event.setAndContinue(RawAnimation.begin().thenPlayAndHold(animateOpen ? "open" : "close"))
        ));
    }
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    @Override
    public @NonNull AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
