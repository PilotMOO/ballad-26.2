package mod.pilot.birch_n_bees.blocks.block_entities;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import mod.pilot.birch_n_bees.blocks.BirchBlocks;
import mod.pilot.birch_n_bees.blocks.unique.HivekilnBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.IModBusEvent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;

public class HivekilnBlockEntity extends BlockEntity implements GeoBlockEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(HivekilnBlockEntity.class);

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

        //Do we need to recompute this??? Because it's not like the block pos can change...?
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
            RecipeHub.RecipeInstance recipe;
            int burnTime;
            if (entity.heldStack.isEmpty() && (recipe = RecipeHub.recipeFor(stack)) != null){
                ItemStack toInsert = recipe.grabRecipeAmountIfPossible(stack);
                if (toInsert != null) entity.updateItemStack(toInsert, level,
                        itemEntity.getOwner() instanceof LivingEntity le ? le : null);
                RandomSource random = level.getRandom();
                level.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT, SoundSource.BLOCKS,
                        0.75f + ((random.nextFloat() - 0.5f) / 2f), 1.25f);
                level.sendParticles(ParticleTypes.COMPOSTER,
                        collection.minX + (collection.getXsize() / 2),
                        collection.maxY + 0.1d,
                        collection.minZ + (collection.getZsize() / 2),
                        6,
                        0.2d, 0.2d, 0.2d,
                        0.3d);
            }
            else if ((burnTime = itemEntity.getItem().getBurnTime(null, level.fuelValues())) > 0){
                int itemFuelValue = burnTime / 100;
                if (itemFuelValue > 0){
                    int kilnFuel = state.getValue(HivekilnBlock.FUEL);
                    if (kilnFuel == HivekilnBlock.MAX_FUEL){
                        if (entity.heldStack != null) break;
                        else continue;
                    }
                    int fuelDelta = HivekilnBlock.MAX_FUEL - kilnFuel;
                    int maxAdditions = (fuelDelta / itemFuelValue);
                    if (fuelDelta % itemFuelValue != 0) maxAdditions++;
                    if (maxAdditions > 0){
                        int toShrink = Math.min(maxAdditions, stack.count());
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
                        if (kilnFuel == HivekilnBlock.MAX_FUEL && entity.heldStack != null) break;
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

    public void updateItemStack(ItemStack stack, ServerLevel level, @Nullable LivingEntity entity) {
        heldStack = stack;
        level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(entity, this.getBlockState()));
        setChanged();
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }
    public ItemStack heldStack = ItemStack.EMPTY;

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("openFlag", animateOpen);
        output.store("heldStack", ItemStack.OPTIONAL_CODEC, heldStack);
    }
    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        animateOpen = input.getBooleanOr("openFlag", getBlockState().getValue(HivekilnBlock.OPEN));
        heldStack = input.read("heldStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            output.store("heldStack", ItemStack.OPTIONAL_CODEC, heldStack);
            return output.buildResult();
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (hasLevel() && !heldStack.isEmpty()) Block.popResource(getLevel(), pos, heldStack);
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

    public static class RecipeHub {
        public static boolean hasValidRecipe(ItemStack stack){
            for (RecipeInstance r : RECIPES) if (r.validForRecipe(stack)) return true;
            return false;
        }
        public static @Nullable RecipeInstance recipeFor(ItemStack stack){
            for (RecipeInstance r : RECIPES) if (r.validForRecipe(stack)) return r;
            return null;
        }
        public static ItemStack attemptRecipeResult(ItemStack original){
            RecipeInstance recipe = null;
            for (RecipeInstance r1 : RECIPES){
                if (r1.validForRecipe(original)){
                    recipe = r1;
                    break;
                }
            }
            return recipe != null ? recipe.craftResult(original) : ItemStack.EMPTY;
        }

        public static int NATIVE_RECIPES = 1;
        public static void init(IEventBus bus){
            RECIPES = new RecipeInstance[NATIVE_RECIPES];
            bus.addListener(RecipeHub::registerNativeRecipes);
            bus.post(new RegisterRecipesEvent());
        }

        @OnlyIn(Dist.DEDICATED_SERVER)
        public record RecipeInstance(LazyAccessor<ItemStack> base, int duration, LazyAccessor<ItemStack> result){
            public ItemStack grabRecipeAmountIfPossible(ItemStack from){
                if (validForRecipe(from)) return from.split(base.get().count());
                else return ItemStack.EMPTY;
            }
            public ItemStack craftResult(ItemStack from){
                if (!validForRecipe(from)) return ItemStack.EMPTY;
                ItemStack base = this.base.get();
                from.shrink(base.count());
                return result.get();
            }
            public boolean validForRecipe(ItemStack item){
                ItemStack base = this.base.get();
                return (item.is(base.getItem()) && item.count() >= base.count());
            }
        }
        private static RecipeInstance[] RECIPES;
        static int count = 0;
        private static void register(RecipeInstance instance){
            if (count >= RECIPES.length){
                RecipeInstance[] newArray = new RecipeInstance[count + 1];
                System.arraycopy(RECIPES, 0, newArray, 0, count);
                RECIPES = newArray;
            }
            RECIPES[count++] = instance;
        }

        public static class RegisterRecipesEvent extends Event implements IModBusEvent{
            public void register(Supplier<ItemStack> base, int duration, Supplier<ItemStack> result){
                RecipeHub.register(new RecipeInstance(
                        new LazyAccessor<>(base),
                        duration,
                        new LazyAccessor<>(result))
                );
            }
        }
        private static void registerNativeRecipes(RegisterRecipesEvent event){
            event.register(
                    () -> new ItemStack(Items.HONEYCOMB),
                    600,
                    () -> new ItemStack(Items.GOLD_INGOT)
            );
        }
    }
}
