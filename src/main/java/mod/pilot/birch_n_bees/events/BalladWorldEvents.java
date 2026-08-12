package mod.pilot.birch_n_bees.events;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.achievements.BirchCriteriaRegistries;
import mod.pilot.birch_n_bees.blocks.BirchBlocks;
import mod.pilot.birch_n_bees.data.BirchDataHelper;
import mod.pilot.birch_n_bees.effects.BirchEffects;
import mod.pilot.birch_n_bees.entity.ai.HostileFishGoal;
import mod.pilot.birch_n_bees.items.BirchItems;
import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.DynamicInventoryToken;
import mod.pilot.birch_n_bees.systems.extended_health.HealthToken;
import mod.pilot.birch_n_bees.util.BirchTags;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.animal.fish.Salmon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingSwapItemsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.ArrayList;

import static net.minecraft.world.level.block.BeehiveBlock.HONEY_LEVEL;

@EventBusSubscriber(modid = ABOBAB.MOD_ID)
public class BalladWorldEvents {
    @SubscribeEvent
    public static void injectEntityAI(EntityJoinLevelEvent event){
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity){
            if (entity instanceof Bee bee){
                bee.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(bee, Player.class, true));
            }
            else if (entity instanceof Salmon salmon){
                salmon.goalSelector.addGoal(1, new HostileFishGoal(salmon,
                        2f, 48d, 0d, 0.3d, 20,
                        HostileFishGoal.SALMON_HOSTILE_PREDICATE));
            }
        }
    }

    @SubscribeEvent
    public static void hardWood(BreakBlockEvent event){
        BlockState bState = event.getState();
        Player player = event.getPlayer();
        if (bState.is(BlockTags.LOGS) &&
                !player.getItemInHand(InteractionHand.MAIN_HAND).is(ItemTags.AXES)) {
            event.setCanceled(true);
            if (player.level() instanceof ServerLevel server) {
                BlockPos bPos = event.getPos();
                BirchDataHelper.popBees(server, new Vec3(bPos.getX() + 0.5, bPos.getY() + 0.5, bPos.getZ() + 0.5),
                        4, player);
                BirchCriteriaRegistries.BREAK_BIRCH.get().trigger(player);

                player.playSound(SoundEvents.ENDER_DRAGON_GROWL, 1f, 0.5f);
                player.sendSystemMessage(Component.translatable("birch_n_bees.message.wood_too_hard"));
            }
        }
    }

    @SubscribeEvent
    public static void gatherHoney(PlayerInteractEvent.LeftClickBlock event){
        Level level = event.getLevel();
        BlockPos bPos = event.getPos();
        BlockState bState = level.getBlockState(bPos);
        if (event.getItemStack().isEmpty() && bState.is(Blocks.BEE_NEST)){
            BeehiveBlockEntity hive = (BeehiveBlockEntity)level.getBlockEntity(bPos);
            if (hive != null && bState.getValue(HONEY_LEVEL) >= 3){
                if (!hive.isSedated()) hive.emptyAllLivingFromHive(event.getEntity(), bState, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
                level.setBlock(bPos, bState.setValue(HONEY_LEVEL, 0), 3);
                Block.popResource(level, bPos, new ItemStack(Items.HONEYCOMB));
            }
        }
    }

    @SubscribeEvent
    public static void gatherBark(PlayerInteractEvent.RightClickBlock event){
        Level level = event.getLevel();
        BlockPos bPos = event.getPos();
        BlockState bState = level.getBlockState(bPos);
        if (event.getItemStack().is(ItemTags.AXES) && bState.is(Blocks.BIRCH_LOG)
                && !level.isClientSide() && level.getRandom().nextBoolean()){
            popBirch.add(new Pair<>(bPos, event.getFace()));
        }
    }
    public static final ArrayList<Pair<BlockPos, Direction>> popBirch = new ArrayList<>();
    @SubscribeEvent
    public static void gatherBarkPart2(ServerTickEvent.Post event){
        if (popBirch.isEmpty()) return;
        for (Pair<BlockPos, Direction> birch : popBirch) {
            BlockPos bPos = birch.getA();
            Direction direction = birch.getB();
            ServerLevel level = event.getServer().overworld();
            if (level.getBlockState(bPos).is(Blocks.STRIPPED_BIRCH_LOG)) {
                if (direction == null) direction = Direction.UP;
                Vec3 pos = new Vec3(bPos.getX() + 0.5, bPos.getY() + 0.5, bPos.getZ() + 0.5);
                ItemEntity itemEntity = new ItemEntity(level,
                        pos.x + direction.getStepX() * 0.6f,
                        pos.y + direction.getStepY() * 0.6f,
                        pos.z + direction.getStepZ() * 0.6f,
                        new ItemStack(BirchItems.BIRCH_BARK.get()));
                level.addFreshEntity(itemEntity);
            }
        }
        popBirch.clear();
    }

    @SubscribeEvent
    public static void smashStones(PlayerInteractEvent.RightClickBlock event){
        Level level = event.getLevel();
        if (!(level instanceof ServerLevel server)) return;
        BlockPos bPos = event.getPos();
        BlockState bState = level.getBlockState(bPos);
        ItemStack item = event.getItemStack();
        Stones stone;
        if (item.is(BirchTags.Items.ROCK_CHIPPING) && (stone = Stones.fromBlock(bState)) != null){
            if (getTierOfPebble(item) >= stone.tier) {
                popStone(event.getFace(), level, bPos, stone);
            } else {
                server.playSound(null, bPos, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, .5f, 1.5f);
                event.getEntity().sendSystemMessage(Component.translatable("birch_n_bees.message.stone_too_hard"));
            }
            event.getItemStack().shrink(1);
        }
    }
    public static void popStone(Direction direction, Level level, BlockPos bPos, Stones stone){
        if (level instanceof ServerLevel server) {
            if (direction == null) direction = Direction.UP;
            Vec3 pos = new Vec3(bPos.getX() + 0.5, bPos.getY() + 0.5, bPos.getZ() + 0.5);
            ItemEntity itemEntity = new ItemEntity(level,
                    pos.x + direction.getStepX() / 2f,
                    pos.y + direction.getStepY() / 2f,
                    pos.z + direction.getStepZ() / 2f,
                    stone.getItemStack());
            level.addFreshEntity(itemEntity);

            BlockState cobbled;
            if ((cobbled = stone.getCobbledState()) != null) server.setBlock(bPos, cobbled, 3);

            server.playSound(null, bPos, SoundEvents.STONE_BREAK, SoundSource.PLAYERS, .5f, 0.75f);
        }
    }

    public enum Stones{
        STONE(BirchItems.STONE_PEBBLE, Blocks.COBBLESTONE, 3),
        ANDESITE(BirchItems.ANDESITE_PEBBLE, BirchBlocks.COBBLED_ANDESITE.get(), 1),
        DIORITE(BirchItems.DIORITE_PEBBLE, BirchBlocks.COBBLED_DIORITE.get(), 2),
        GRANITE(BirchItems.GRANITE_PEBBLE, BirchBlocks.COBBLED_GRANITE.get(), 2),
        TUFF(BirchItems.TUFF_PEBBLE, BirchBlocks.COBBLED_TUFF.get(), 1),
        DEEPSLATE(BirchItems.DEEPSLATE_PEBBLE, Blocks.COBBLED_DEEPSLATE, 3);

        private final Holder<Item> pebble;
        private final Block cobbled;
        public final int tier;
        public ItemStack getItemStack(){
            return new ItemStack(pebble.value());
        }
        public BlockState getCobbledState(){
            if (cobbled == null) return null;
            return cobbled.defaultBlockState();
        }
        Stones(Holder<Item> pebble, Block block, int tier){
            this.pebble = pebble;
            this.cobbled = block;
            this.tier = tier;
        }
        public static @Nullable Stones fromBlock(BlockState blockState) {return fromBlock(blockState.getBlock());}
        public static @Nullable Stones fromBlock(Block block){
            if (block.equals(Blocks.STONE)) return STONE;
            if (block.equals(Blocks.ANDESITE)) return ANDESITE;
            if (block.equals(Blocks.DIORITE)) return DIORITE;
            if (block.equals(Blocks.GRANITE)) return GRANITE;
            if (block.equals(Blocks.TUFF)) return TUFF;
            if (block.equals(Blocks.DEEPSLATE)) return DEEPSLATE;
            return null;
        }
    }
    private static int getTierOfPebble(ItemStack item){
        if (item.is(BirchTags.Items.ROCK_TIER_1)) return 2;
        if (item.is(BirchTags.Items.ROCK_TIER_2)) return 3;
        if (item.is(BirchTags.Items.ROCK_TIER_3)) return 4;
        if (item.is(BirchTags.Items.ROCK_CHIPPING)) return 1;
        return -1;
    }

    @SubscribeEvent
    public static void breakHiveForHoney(BreakBlockEvent event){
        BlockState bState = event.getState();
        if (bState.is(Blocks.BEE_NEST)){
            Player player = event.getPlayer();
            BeehiveBlockEntity hive = (BeehiveBlockEntity)player.level().getBlockEntity(event.getPos());
            if (hive != null){
                hive.emptyAllLivingFromHive(player, bState, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
            }
            Block.popResource(player.level(), event.getPos(), new ItemStack(Items.HONEYCOMB));
        }
    }

    @SubscribeEvent
    public static void playerPretick(PlayerTickEvent.Pre event){
        Player player = event.getEntity();
        if (HealthToken.has(player)){
            HealthToken token = HealthToken.get(player);
            if (player instanceof ServerPlayer sPlayer) token.tickInstanceServer(sPlayer);
            else if (player instanceof AbstractClientPlayer cPlayer) token.tickInstanceClient(cPlayer);
        }

        ItemStack item = player.getMainHandItem();
        if (BirchDataHelper.contraband(item) && player.level() instanceof ServerLevel server){
            if (item.isDamageableItem()) item.hurtAndBreak(5, server, player, (item1) -> {});
            else item.shrink(1);
            if (!player.getCooldowns().isOnCooldown(item)) {
                player.playSound(SoundEvents.ENDER_DRAGON_GROWL, 1f, 0.25f);
                player.sendSystemMessage(Component.translatable("birch_n_bees.message.contraband"));
                player.getCooldowns().addCooldown(item, 500);
                BirchCriteriaRegistries.CONTRABAND.get().trigger(player);
            }

            RandomSource random = server.getRandom();
            for (int i = 0; i < 4; i++) {
                Bee bee = new Bee(EntityTypes.BEE, server);
                bee.copyPosition(player);
                bee.setTarget(player);
                bee.invulnerableTime = 20;

                Vec3 delta = new Vec3(1, 0, 1);
                delta = delta.yRot((float)Math.toRadians(random.nextIntBetweenInclusive(-180, 180)));
                bee.setDeltaMovement(delta.scale(0.5));

                server.addFreshEntity(bee);
            }
        }
    }

    @SubscribeEvent
    public static void tickBirchBarkItem(EntityTickEvent.Post event){
        if (event.getEntity() instanceof ItemEntity itemEntity
                && itemEntity.isInWater()
                && itemEntity.getItem().is(BirchItems.BIRCH_BARK)){
            itemEntity.setItem(BirchItems.SOGGY_BIRCH_BARK.toStack(itemEntity.getItem().getCount()));
            if (itemEntity.level() instanceof ServerLevel server){
                server.playSound(itemEntity, itemEntity.getOnPos(), SoundEvents.PLAYER_SPLASH, SoundSource.NEUTRAL, 1f, .75f);
            }
        }
    }

    @SubscribeEvent
    public static void unlawfulKilling(LivingDeathEvent event){
        if (event.getEntity() instanceof Bee) return;
        if (event.getEntity() instanceof Animal animal
                && animal.level() instanceof ServerLevel server
                && event.getSource().getEntity() instanceof Player player){
            player.playSound(SoundEvents.ENDER_DRAGON_GROWL, 1.5f, 0.75f);
            player.sendSystemMessage(Component.translatable("birch_n_bees.message.unlawful_animal_killing"));
            BirchCriteriaRegistries.ILLEGAL_HUNTING.get().trigger(player);

            BirchDataHelper.popKillerBees(server, animal.position(), 5, player);
        }
    }

    @SubscribeEvent
    public static void nightWatch(ServerTickEvent.Post event){
        if ((float)(event.getServer().overworld().getOverworldClockTime() % 24000L) / 24000L >= .5f){
            event.getServer().overworld().players().forEach((player) -> BirchCriteriaRegistries.NIGHT.get().trigger(player));
        }
    }

    @SubscribeEvent
    public static void youArePanicking(LivingEntityUseItemEvent.Start event){
        LivingEntity entity = event.getEntity();
        MobEffectInstance effect = entity.getEffect(BirchEffects.FEAR_EFFECT);
        if (effect != null){
            int level = effect.getAmplifier() + 1;
            float chance = 1f / level;
            //chance = 0f; //temp
            if (entity.getRandom().nextFloat() > chance){
                event.setCanceled(true);
                if (entity instanceof Player p){
                    p.sendOverlayMessage(Component.translatable("birch_n_bees.panicking"));
                }
            }
        }
    }
    @SubscribeEvent
    public static void onBlockPanicking(UseItemOnBlockEvent event){
        Player entity = event.getPlayer();
        assert entity != null;
        MobEffectInstance effect = entity.getEffect(BirchEffects.FEAR_EFFECT);
        if (effect != null){
            int level = effect.getAmplifier() + 2;
            float chance = 1f / level;
            //chance = 0f; //temp
            if (entity.getRandom().nextFloat() > chance){
                event.setCanceled(true);
                entity.sendOverlayMessage(Component.translatable("birch_n_bees.panicking"));
            }
        }
    }

    @SubscribeEvent
    public static void mining(PlayerEvent.BreakSpeed event){
        BlockState bState = event.getState();
        if (bState.is(Blocks.BIRCH_LOG) || bState.is(Blocks.STRIPPED_BIRCH_LOG)) {
            event.setNewSpeed(event.getOriginalSpeed() / 2f);
        } else if (bState.is(BirchTags.Blocks.WARN_IF_INCORRECT_TOOL_KNIFE)){
            Player p = event.getEntity();
            if (!p.isShiftKeyDown() && !p.getMainHandItem().is(BirchTags.Items.KNIFE_HARVESTING)){
                event.setCanceled(true);
                p.sendOverlayMessage(Component.translatable("birch_n_bees.message.incorrect_tool_knife_warning"));
            }
        }
        else if (!event.getEntity().getInventory().getSelectedItem()
                .isCorrectToolForDrops(bState)){
            event.setNewSpeed(event.getOriginalSpeed() / 10f);
        } else event.setNewSpeed(event.getOriginalSpeed() / 5);
    }

    @SubscribeEvent
    public static void reapplyTokenUponLogin(PlayerEvent.PlayerLoggedInEvent event){
        Player player = event.getEntity();
        DynamicInventoryToken.applyInFull(player);
        player.inventoryMenu.broadcastFullState();
    }

    @SubscribeEvent
    public static void preventSwappingIfInvalid(LivingSwapItemsEvent.Hands event){
        if (event.getEntity() instanceof Player player){
            DynamicInventoryToken token = DynamicInventoryToken.get(player);
            event.setCanceled(!token.offhand || token.hotbarSlots == 0);
        }
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingDamageEvent.Post event){
        if (event.getEntity() instanceof Player player && player.level() instanceof ServerLevel server){
            for (ItemStack item : player.getInventory()){
                if (item.is(BirchItems.WILDFLOWER_SATCHEL)){
                    BundleContents contents = item.get(DataComponents.BUNDLE_CONTENTS);
                    if (contents != null && !contents.isEmpty()){
                        System.out.println("All previous criteria has been met! trying to drop items...");
                        int toDrop = Math.min(64, (int)Math.floor(event.getInflictedDamage() * player.getRandom().nextInt(4)));
                        System.out.println("We took " + event.getInflictedDamage() +" damage! Equalling " + toDrop +" drops...");
                        BundleContents.Mutable mutable = new BundleContents.Mutable(contents);
                        boolean cycleFlag = true;
                        int countTracker = contents.size();
                        boolean soundFlag = false;
                        while (toDrop > 0 && countTracker > 0 && cycleFlag){
                            int index = player.getRandom().nextInt(countTracker);
                            System.out.println("Cycle, toDrop is " + toDrop + ", random index is " + index);
                            mutable.toggleSelectedItem(index);
                            ItemStack removed = mutable.removeOne();
                            System.out.println("Random itemstack is " + removed);
                            if (removed != null) {
                                ItemStack delta = removed.split(player.getRandom().nextInt(toDrop));
                                player.drop(delta, true, false);
                                soundFlag = true;
                                int tryInsert = mutable.tryInsert(removed);
                                toDrop -= tryInsert;
                                if (tryInsert == 0) countTracker--;
                                System.out.println("Removed was not null! we returned " + tryInsert + " to the stack, putting back into bundle");
                            } else {
                                System.out.println("Oops! It looks like we failed to retrieve an item from the bundle! toDrop: " + toDrop);
                                cycleFlag = false;
                            }
                            if (soundFlag) {
                                server.playSound(null, player.blockPosition(), SoundEvents.BUNDLE_REMOVE_ONE, SoundSource.PLAYERS,
                                        1.5F, 0.8F + server.getRandom().nextFloat() * 0.4F);
                            }
                        }
                        item.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
                    }
                }
            }
        }
    }
}
