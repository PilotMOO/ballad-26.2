package mod.pilot.birch_n_bees.items;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.blocks.BirchBlocks;
import mod.pilot.birch_n_bees.data.InputReader;
import mod.pilot.birch_n_bees.entity.BirchEntities;
import mod.pilot.birch_n_bees.items.unique.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.DefaultDataComponentsBoundEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@EventBusSubscriber(modid = ABOBAB.MOD_ID) //Solely for the fillToolHeads method
public class BirchItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ABOBAB.MOD_ID);

    public static final DeferredItem<Item> WILDFLOWER_TWINE = ITEMS.registerItem("wildflower_twine",
            (properties) -> new Item(properties){
                @Override
                public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                            @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
                                            @NotNull TooltipFlag flag) {
                    tooltipAdder.accept(Component.translatable("item.birch_n_bees.wildflower_twine.description"));
                    super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
                }
            });
    public static final DeferredItem<Item> STRIPPED_WILDFLOWER_TWINE = ITEMS.registerItem("stripped_wildflower_twine",
            (properties) -> new Item(properties){
                @Override
                public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                            @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
                                            @NotNull TooltipFlag flag) {
                    tooltipAdder.accept(Component.translatable("item.birch_n_bees.stripped_wildflower_twine.description"));
                    super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
                }
            });
    public static final DeferredItem<Item> WILDTHREAD = ITEMS.registerItem("wildthread",
            (properties) -> new Item(properties){
                @Override
                public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                            @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
                                            @NotNull TooltipFlag flag) {
                    tooltipAdder.accept(Component.translatable("item.birch_n_bees.wildthread.description"));
                    super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
                }
            });
    public static final DeferredItem<WildthreadSpoolItem> WILDTHREAD_BUILDABLE = ITEMS.registerItem("wildthread_buildable",
            WildthreadSpoolItem::new);

    public static final DeferredItem<Item> WILDFLOWER_WICKER = ITEMS.registerItem("wildflower_wicker",
            (properties) -> new Item(properties){
                @Override
                public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                            @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
                                            @NotNull TooltipFlag flag) {
                    tooltipAdder.accept(Component.translatable("item.birch_n_bees.wildflower_wicker.description"));
                    super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
                }
            });
    public static final DeferredItem<WildflowerWickerItem> WILDFLOWER_WICKER_BUILDABLE = ITEMS.registerItem(
            "wildflower_wicker_buildable", WildflowerWickerItem::new);

    public static final DeferredItem<BundleItem> WILDFLOWER_SATCHEL = ITEMS.registerItem(
            "wildflower_satchel", (properties) -> new WildflowerSatchelItem(properties
                    .stacksTo(1)
                    .component(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY)));


    public static final DeferredItem<Item> BIRCH_BARK = ITEMS.registerItem("birch_bark",
            (properties) -> new Item(properties){
                @Override
                public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                            @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
                                            @NotNull TooltipFlag flag) {
                    tooltipAdder.accept(Component.translatable("item.birch_n_bees.birch_bark.description"));
                    super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
                }
            });
    public static final DeferredItem<Item> SOGGY_BIRCH_BARK = ITEMS.registerItem("soggy_birch_bark",
            (properties) -> new Item(properties.food(BirchFoodProperties.BARK_FOOD)){
                @Override
                public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                            @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
                                            @NotNull TooltipFlag flag) {
                    tooltipAdder.accept(Component.translatable("item.birch_n_bees.soggy_birch_bark.description"));
                    super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
                }
            });

    public static final DeferredItem<SplinterItem> SPLINTERS = ITEMS.registerItem("splinters", SplinterItem::new);
    public static final DeferredItem<Item> BUNDLE_OF_SPLINTERS = ITEMS.registerItem("bundle_of_splinters",
            (properties) -> new Item(properties.food(BirchFoodProperties.BARK_FOOD)){
                @Override
                public @NotNull ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
                    if (level instanceof ServerLevel server){
                        livingEntity.hurtServer(server, livingEntity.damageSources().cactus(), 5);
                        server.playSound(null, livingEntity.blockPosition(), SoundEvents.THORNS_HIT, SoundSource.PLAYERS);
                    }
                    return super.finishUsingItem(stack, level, livingEntity);
                }

                @Override
                public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                            @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
                                            @NotNull TooltipFlag flag) {
                    tooltipAdder.accept(Component.translatable("item.birch_n_bees.bundle_of_splinters.description"));
                    super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
                }
            });
    public static final DeferredItem<Item> CLAY_BRICK = ITEMS.registerItem("clay_brick",
            (properties) -> new Item(properties){
                @Override
                public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                            @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
                                            @NotNull TooltipFlag flag) {
                    tooltipAdder.accept(Component.translatable("item.birch_n_bees.clay_brick.description"));
                    super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
                }
            });

    public static final DeferredItem<WildflowerDressingItem> WILDFLOWER_DRESSING = ITEMS.registerItem("wildflower_dressing",
            (properties -> new WildflowerDressingItem(properties){
                @Override
                public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                            @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
                                            @NotNull TooltipFlag flag) {
                    tooltipAdder.accept(Component.translatable("item.birch_n_bees.wildflower_dressing.description"));
                    super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
                }
            }));
    public static final DeferredItem<WildflowerBandageItem> WILDFLOWER_BANDAGE = ITEMS.registerItem("wildflower_bandage",
            (properties -> new WildflowerBandageItem(properties){
                @Override
                public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                            @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
                                            @NotNull TooltipFlag flag) {
                    tooltipAdder.accept(Component.translatable("item.birch_n_bees.wildflower_bandage.description"));
                    super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
                }
            }));

    public static final DeferredItem<Item> THREE_SUGARCANE = ITEMS.registerItem("three_sugarcane",
            (properties) -> new Item(properties.stacksTo(7)){
                @Override
                public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                            @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
                                            @NotNull TooltipFlag flag) {
                    tooltipAdder.accept(Component.translatable("item.birch_n_bees.three_sugarcane.description"));
                    super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
                }
            });

    public static final DeferredItem<Item> STONE_PEBBLE = ITEMS.registerItem("stone_pebble", Item::new);
    public static final DeferredItem<Item> ANDESITE_PEBBLE = ITEMS.registerItem("andesite_pebble", Item::new);
    public static final DeferredItem<Item> DIORITE_PEBBLE = ITEMS.registerItem("diorite_pebble", Item::new);
    public static final DeferredItem<Item> GRANITE_PEBBLE = ITEMS.registerItem("granite_pebble", Item::new);
    public static final DeferredItem<Item> TUFF_PEBBLE = ITEMS.registerItem("tuff_pebble", Item::new);
    public static final DeferredItem<Item> DEEPSLATE_PEBBLE = ITEMS.registerItem("deepslate_pebble", Item::new);


    public static final DeferredItem<Item> HONEY_AXE_HEAD = ITEMS.registerItem("honey_axe_head", Item::new);
    public static final DeferredItem<HoneyAxeItem> HONEY_AXE = ITEMS.registerItem("honey_axe", HoneyAxeItem::new);
    public static final DeferredItem<Item> HONEY_SHOVEL_HEAD = ITEMS.registerItem("honey_shovel_head", Item::new);
    public static final DeferredItem<HoneyShovelItem> HONEY_SHOVEL = ITEMS.registerItem("honey_shovel", HoneyShovelItem::new);

    public static final DeferredItem<CrudeCobblestonePickaxeItem> CRUDE_COBBLESTONE_PICKAXE = ITEMS.registerItem(
            "crude_cobblestone_pickaxe", CrudeCobblestonePickaxeItem::new);
    public static final DeferredItem<Item> CRUDE_COBBLESTONE_PICKAXE_HEAD = ITEMS.registerItem("crude_cobblestone_pickaxe_head", Item::new);
    public static final DeferredItem<CrudeCobblestoneAxeItem> CRUDE_COBBLESTONE_AXE = ITEMS.registerItem(
            "crude_cobblestone_axe", CrudeCobblestoneAxeItem::new);
    public static final DeferredItem<Item> CRUDE_COBBLESTONE_AXE_HEAD = ITEMS.registerItem("crude_cobblestone_axe_head", Item::new);
    public static final DeferredItem<CrudeCobblestoneSwordItem> CRUDE_COBBLESTONE_SWORD = ITEMS.registerItem(
            "crude_cobblestone_sword", CrudeCobblestoneSwordItem::new);
    public static final DeferredItem<Item> CRUDE_COBBLESTONE_SWORD_HEAD = ITEMS.registerItem("crude_cobblestone_sword_head", Item::new);
    public static final DeferredItem<FlintKnifeItem> FLINT_KNIFE = ITEMS.registerItem(
            "flint_knife", FlintKnifeItem::new);
    public static final DeferredItem<Item> BIRCH_SHELL = ITEMS.registerItem("birch_shell",(properties) -> new Item(properties){
        @Override
        public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                    @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
                                    @NotNull TooltipFlag flag) {
            tooltipAdder.accept(Component.translatable("item.birch_n_bees.birch_shell.description"));
            super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        }
    });
    public static final DeferredItem<BirchShieldItem> BIRCH_SHIELD = ITEMS.registerItem(
            "birch_shield", BirchShieldItem::new);
    public static final DeferredItem<OvergrownBowItem> OVERGROWN_BOW = ITEMS.registerItem(
            "overgrown_bow", OvergrownBowItem::new);

    public static final DeferredItem<WildflowerPopperItem> WILDFLOWER_POPPER = ITEMS.registerItem(
            "wildflower_popper", WildflowerPopperItem::new);

    public static final DeferredItem<BlockItem> STICKY_PLANKS = ITEMS.registerSimpleBlockItem(BirchBlocks.STICKY_PLANKS);

    public static final DeferredItem<BlockItem> COBBLED_ANDESITE = ITEMS.registerSimpleBlockItem(BirchBlocks.COBBLED_ANDESITE);
    public static final DeferredItem<BlockItem> COBBLED_DIORITE = ITEMS.registerSimpleBlockItem(BirchBlocks.COBBLED_DIORITE);
    public static final DeferredItem<BlockItem> COBBLED_GRANITE = ITEMS.registerSimpleBlockItem(BirchBlocks.COBBLED_GRANITE);
    public static final DeferredItem<BlockItem> COBBLED_TUFF = ITEMS.registerSimpleBlockItem(BirchBlocks.COBBLED_TUFF);

    public static final DeferredItem<BlockItem> WILDFLOWER_BASKET = ITEMS.registerSimpleBlockItem(BirchBlocks.WILDFLOWER_BASKET);

    public static final DeferredItem<BlockItem> PREPARED_SUGARCANE = ITEMS.registerSimpleBlockItem(BirchBlocks.PREPARED_SUGARCANE);
    public static final DeferredItem<Item> RAW_SUGAR = ITEMS.registerItem("raw_sugar",
            (properties) -> new Item(properties){
                @Override
                public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                            @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder,
                                            @NotNull TooltipFlag flag) {
                    tooltipAdder.accept(Component.translatable("item.birch_n_bees.raw_sugar.description"));
                    super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
                }
            });

    public static final DeferredItem<SpawnEggItem> SPLINTERING_SPAWN = ITEMS.registerItem("splintering_spawn",
            (properties -> new SpawnEggItem(properties.spawnEgg(BirchEntities.SPLINTERING.get()))));
    public static final DeferredItem<SpawnEggItem> NESTHEAD_SPAWN = ITEMS.registerItem("nesthead_spawn",
            (properties -> new SpawnEggItem(properties.spawnEgg(BirchEntities.NESTHEAD.get()))));
    public static final DeferredItem<SpawnEggItem> BLOOMING_REMAINS_SPAWN = ITEMS.registerItem("blooming_remains_spawn",
            (properties -> new SpawnEggItem(properties.spawnEgg(BirchEntities.BLOOMING_REMAINS.get()))));

    public static final DeferredItem<TestingToolBase> TESTING_TOOL_BASE = ITEMS.registerItem("buildable_tool",
            TestingToolBase::new);
    public static final DeferredItem<BuildableToolBase> HONEY_TOOL_BASE = ITEMS.registerItem("honey_tool_base",
            (p) -> new BuildableToolBase(p, 400){
                @Override
                public void fillValidHeads() {
                    validHeads = new ToolHead[2];
                    validHeads[0] = new ToolHead(HONEY_AXE_HEAD.get(), new ItemStack(HONEY_AXE.get()));
                    validHeads[1] = new ToolHead(HONEY_SHOVEL_HEAD.get(), new ItemStack(HONEY_SHOVEL.get()));
                }
            });
    public static final DeferredItem<BuildableToolBase> WILDTHREAD_TOOL_BASE = ITEMS.registerItem("wildthread_tool_base",
            (p) -> new BuildableToolBase(p, 2400){
                @Override
                public void fillValidHeads() {
                    validHeads = new ToolHead[4];
                    validHeads[0] = new ToolHead(CRUDE_COBBLESTONE_PICKAXE_HEAD.get(),
                            new ItemStack(CRUDE_COBBLESTONE_PICKAXE.get()));
                    validHeads[1] = new ToolHead(CRUDE_COBBLESTONE_AXE_HEAD.get(),
                            new ItemStack(CRUDE_COBBLESTONE_AXE.get()));
                    validHeads[2] = new ToolHead(CRUDE_COBBLESTONE_SWORD_HEAD.get(),
                            new ItemStack(CRUDE_COBBLESTONE_SWORD.get()));
                    validHeads[3] = new ToolHead(BIRCH_SHELL.get(),
                            new ItemStack(BIRCH_SHIELD.get()));
                }
            });

    @SubscribeEvent
    public static void fillToolHeads(DefaultDataComponentsBoundEvent event){
        TESTING_TOOL_BASE.get().fillValidHeads();
        HONEY_TOOL_BASE.get().fillValidHeads();
        WILDTHREAD_TOOL_BASE.get().fillValidHeads();

        WILDTHREAD_BUILDABLE.get().fillValidHeads();
        WILDFLOWER_WICKER_BUILDABLE.get().fillValidHeads();
    }


    public static final DeferredItem<Item> CLAYFLINT = ITEMS.registerItem("clayflint", (Item::new));
    public static final DeferredItem<Item> MOON = ITEMS.registerItem("moon", (Item::new));
    public static final DeferredItem<Item> WITHER_HEART = ITEMS.registerItem("wither_heart", (Item::new));

    public static final DeferredItem<Item> OFFHAND_LOCKER = ITEMS.registerItem("offhand_locker", (OffhandLockingTestWand::new));
    public static final DeferredItem<Item> HOTBAR_LOCKER = ITEMS.registerItem("hotbar_locker", (HotbarLockingTestWand::new));
    public static final DeferredItem<Item> WAND_OF_BONE_PAIN = ITEMS.registerItem("wand_of_bone_pain", (WandOfBonePain::new));
}
