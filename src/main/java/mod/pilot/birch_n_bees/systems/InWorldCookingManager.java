package mod.pilot.birch_n_bees.systems;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.items.BirchItems;
import mod.pilot.birch_n_bees.util.BirchAttachmentTypes;
import mod.pilot.birch_n_bees.util.BirchTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;
import java.util.function.Supplier;

@EventBusSubscriber(modid = ABOBAB.MOD_ID)
public class InWorldCookingManager {
    public static void init(IEventBus bus){
        registeredRecipes = new CookingPair[0];
        registeredComplexRecipes = new CookingPair.Complex[0];
        bus.addListener(InWorldCookingManager::registerNativeRecipes);
        bus.post(new RegisterCookingPairsEvent());
    }


    static class CookingPair{
        public final Supplier<ItemStack> original;
        public final int cookTime;
        public final Supplier<ItemStack> result;
        public CookingPair(Supplier<ItemStack> original, int cookTime, Supplier<ItemStack> result){
            this.original = original;
            this.cookTime = cookTime;
            this.result = result;
        }

        public boolean validate(ItemStack toCheck){
            ItemStack stack = original.get();
            return stack.is(toCheck.getItem()) && stack.count() <= toCheck.count();
        }
        static class Complex extends CookingPair{
            public final Predicate<ItemEntity> additionalValidation;
            public Complex(Supplier<ItemStack> original, int cookTime, Supplier<ItemStack> result,
                           Predicate<ItemEntity> additionalValidation) {
                super(original, cookTime, result);
                this.additionalValidation = additionalValidation;
            }
            public boolean validate(ItemEntity entity){
                return validate(entity.getItem()) && additionalValidation.test(entity);
            }
        }
    }
    private static CookingPair[] registeredRecipes;
    private static CookingPair.Complex[] registeredComplexRecipes;
    static int count = 0, countComplex = 0;

    private static @Nullable CookingPair getCookingPair(ItemEntity stack){
        for (CookingPair pair : registeredRecipes) {
            if (pair.validate(stack.getItem())) {
                return pair;
            }
        }
        for (CookingPair.Complex complex : registeredComplexRecipes) {
            if (complex.validate(stack)) {
                return complex;
            }
        }
        return null;
    }

    private static void register(CookingPair pair){
        if (count >= registeredRecipes.length){
            CookingPair[] newArray = new CookingPair[count + 1];
            System.arraycopy(registeredRecipes, 0, newArray, 0, count);
            registeredRecipes = newArray;
        }
        registeredRecipes[count++] = pair;
    }
    private static void register(CookingPair.Complex pair){
        if (countComplex >= registeredComplexRecipes.length){
            CookingPair.Complex[] newArray = new CookingPair.Complex[countComplex + 1];
            System.arraycopy(registeredComplexRecipes, 0, newArray, 0, countComplex);
            registeredComplexRecipes = newArray;
        }
        registeredComplexRecipes[countComplex++] = pair;
    }
    public static class RegisterCookingPairsEvent extends Event implements IModBusEvent{
        public void register(Supplier<ItemStack> original, int cookTime, Supplier<ItemStack> result){
            InWorldCookingManager.register(new CookingPair(original, cookTime, result));
        }
        public void register(Supplier<ItemStack> original, int cookTime, Supplier<ItemStack> result,
                             Predicate<ItemEntity> additionalChecks){
            InWorldCookingManager.register(new CookingPair.Complex(original, cookTime, result, additionalChecks));
        }
    }

    private static void registerNativeRecipes(RegisterCookingPairsEvent event){
        event.register(
                () -> new ItemStack(BirchItems.CLAY_BRICK.get()),
                4800,
                () -> new ItemStack(Items.BRICK));
        event.register(
                () -> new ItemStack(Items.BEEF),
                2400,
                () -> new ItemStack(BirchItems.CHARRED_BEEF.get()));
        event.register(
                () -> new ItemStack(Items.CHICKEN),
                2400,
                () -> new ItemStack(BirchItems.CHARRED_CHICKEN.get()));
        event.register(
                () -> new ItemStack(Items.MUTTON),
                2400,
                () -> new ItemStack(BirchItems.CHARRED_MUTTON.get()));
        event.register(
                () -> new ItemStack(Items.PORKCHOP),
                2400,
                () -> new ItemStack(BirchItems.CHARRED_PORKCHOP.get()));
        event.register(
                () -> new ItemStack(Items.RABBIT),
                2400,
                () -> new ItemStack(BirchItems.CHARRED_RABBIT.get()));
        event.register(
                () -> new ItemStack(Items.COD),
                2000,
                () -> new ItemStack(BirchItems.CHARRED_COD.get()));
        event.register(
                () -> new ItemStack(Items.SALMON),
                2000,
                () -> new ItemStack(BirchItems.CHARRED_SALMON.get()));
    }

    public static void validateThenCook(ItemEntity entity){
        //System.out.println("validating cookie thingy");
        ItemStack stack = entity.getItem();
        int cookTime = entity.getData(BirchAttachmentTypes.ITEM_STACK_COOK_TIME_ATTACHMENT);
        if (cookTime == -1) return;
        if (cookTime == 0) {
            if (getCookingPair(entity) == null) {
                //ToDo: rewrite this to invalidate the item stack ONLY if the item doesnt match the
                // expected item type
                entity.setData(BirchAttachmentTypes.ITEM_STACK_COOK_TIME_ATTACHMENT, 0);
                return;
            }
        }

        boolean flag = false;
        if (cookTime != 0 && entity.tickCount % 20 != 0) flag = true;
        else {
            final int radius = 2;
            int x1, y1, z1, x2, y2, z2;
            x1 = x2 = entity.getBlockX();
            y1 = y2 = entity.getBlockY();
            z1 = z2 = entity.getBlockZ();
            x1 -= radius;
            y1 -= radius;
            z1 -= radius;
            x2 += radius;
            y2 += radius;
            z2 += radius;
            Level level = entity.level();
            for (BlockPos bPos : BlockPos.betweenClosed(x1, y1, z1, x2, y2, z2)) {
                BlockState bState = level.getBlockState(bPos);
                if (bState.is(BirchTags.Blocks.COOKS_NEARBY_ITEMS)
                        || bState.getFluidState().is(BirchTags.Fluids.COOKS_NEARBY_ITEMS_FLUID)) {
                    flag = true;
                    break;
                }
            }
        }
        if (flag){
            CookingPair pair = getCookingPair(entity);
            if (pair == null){
                entity.setData(BirchAttachmentTypes.ITEM_STACK_COOK_TIME_ATTACHMENT, 0);
                return;
            }
            if (++cookTime >= pair.cookTime){
                int recipeCost = pair.original.get().count();
                ItemStack pairResult = pair.result.get();
                int entityStackCount = entity.getItem().count(), count = 0;
                do {
                    count += pairResult.count();
                } while ((entityStackCount -= recipeCost) >= recipeCost);

                Level level = entity.level();
                int maxStack = pairResult.getMaxStackSize();
                if (count <= maxStack) {
                    ItemStack result = pairResult.copyWithCount(count);
                    Vec3 pos = entity.position();
                    Vec3 delta = entity.getDeltaMovement();
                    ItemEntity newEntity = new ItemEntity(level, pos.x, pos.y, pos.z,
                            result, delta.x, delta.y, delta.z);
                    level.addFreshEntity(newEntity);
                    newEntity.lifespan *= 2;
                    //entity.discard();
                } else while (count > 0){
                    int cappedCount = Math.min(maxStack, count);
                    ItemStack result = pairResult.copyWithCount(cappedCount);
                    Vec3 pos = entity.position();
                    Vec3 delta = entity.getDeltaMovement();
                    ItemEntity newEntity = new ItemEntity(level, pos.x, pos.y, pos.z,
                            result, delta.x, delta.y, delta.z);
                    level.addFreshEntity(newEntity);
                    newEntity.lifespan *= 2;
                    count -= cappedCount;
                }
                if (entityStackCount == 0) entity.discard();
                smokeLarge(entity, (ServerLevel)level);
                entity.setData(BirchAttachmentTypes.ITEM_STACK_COOK_TIME_ATTACHMENT, -1);
                entity.playSound(SoundEvents.FIRE_EXTINGUISH);
            } else{
                entity.setData(BirchAttachmentTypes.ITEM_STACK_COOK_TIME_ATTACHMENT, cookTime);
                entity.lifespan++;
                if (entity.tickCount % 30 == 0) {
                    smoke(entity, (ServerLevel)entity.level());
                    entity.playSound(SoundEvents.FURNACE_FIRE_CRACKLE);
                }
            }
        } //else entity.setData(BirchAttachmentTypes.ITEM_STACK_COOK_TIME_ATTACHMENT, Math.min(0, cookTime - 4));
    }

    private static void smoke(ItemEntity entity, ServerLevel server) {
        Vec3 pos = entity.position();
        server.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 4, 0, 0.5, 0, 0.01f);
    }
    private static void smokeLarge(ItemEntity entity, ServerLevel server) {
        Vec3 pos = entity.position();
        server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.x, pos.y, pos.z, 4, 0, 0.5, 0, 0.2f);
    }

    @SubscribeEvent
    public static void itemEntityTick(EntityTickEvent.Post event){
        if (event.getEntity() instanceof ItemEntity entity && !entity.level().isClientSide())
            validateThenCook(entity);
    }
}
