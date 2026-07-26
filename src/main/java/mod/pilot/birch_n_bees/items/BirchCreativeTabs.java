package mod.pilot.birch_n_bees.items;

import mod.pilot.birch_n_bees.ABOBAB;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BirchCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ABOBAB.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BALLAD_TAB = TABS.register("ballad_tab",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 3).icon(() -> new ItemStack(BirchItems.HONEY_AXE.get()))
                    .title(Component.translatable("birch_n_bees.creative_tab.ballad_tab"))
                    .displayItems((something, register) ->{
                        register.accept(BirchItems.BIRCH_BARK.get());
                        register.accept(BirchItems.SOGGY_BIRCH_BARK.get());
                        register.accept(BirchItems.WILDFLOWER_TWINE.get());
                        register.accept(BirchItems.STRIPPED_WILDFLOWER_TWINE.get());
                        register.accept(BirchItems.WILDTHREAD_BUILDABLE.get());
                        register.accept(BirchItems.WILDTHREAD.get());
                        register.accept(BirchItems.SPLINTERS.get());
                        register.accept(BirchItems.BUNDLE_OF_SPLINTERS.get());
                        register.accept(BirchItems.CLAY_BRICK.get());

                        register.accept(BirchItems.STONE_PEBBLE.get());
                        register.accept(BirchItems.ANDESITE_PEBBLE.get());
                        register.accept(BirchItems.DIORITE_PEBBLE.get());
                        register.accept(BirchItems.GRANITE_PEBBLE.get());
                        register.accept(BirchItems.TUFF_PEBBLE.get());
                        register.accept(BirchItems.DEEPSLATE_PEBBLE.get());

                        register.accept(BirchItems.COBBLED_ANDESITE.get());
                        register.accept(BirchItems.COBBLED_DIORITE.get());
                        register.accept(BirchItems.COBBLED_GRANITE.get());
                        register.accept(BirchItems.COBBLED_TUFF.get());

                        register.accept(BirchItems.STICKY_PLANKS.get());

                        register.accept(BirchItems.PREPARED_SUGARCANE.get());
                        register.accept(BirchItems.RAW_SUGAR.get());

                        register.accept(BirchItems.HONEY_TOOL_BASE.get());
                        register.accept(BirchItems.HONEY_AXE.get());
                        register.accept(BirchItems.HONEY_SHOVEL.get());
                        register.accept(BirchItems.HONEY_AXE_HEAD.get());
                        register.accept(BirchItems.HONEY_SHOVEL_HEAD.get());
                        register.accept(BirchItems.FLINT_KNIFE.get());

                        register.accept(BirchItems.WILDTHREAD_TOOL_BASE.get());
                        register.accept(BirchItems.CRUDE_COBBLESTONE_PICKAXE.get());
                        register.accept(BirchItems.CRUDE_COBBLESTONE_AXE.get());
                        register.accept(BirchItems.CRUDE_COBBLESTONE_SWORD.get());
                        register.accept(BirchItems.BIRCH_SHIELD.get());
                        register.accept(BirchItems.CRUDE_COBBLESTONE_PICKAXE_HEAD.get());
                        register.accept(BirchItems.CRUDE_COBBLESTONE_AXE_HEAD.get());
                        register.accept(BirchItems.CRUDE_COBBLESTONE_SWORD_HEAD.get());
                        register.accept(BirchItems.BIRCH_SHELL.get());

                        register.accept(BirchItems.OVERGROWN_BOW.get());

                        register.accept(BirchItems.WILDFLOWER_DRESSING);
                        register.accept(BirchItems.WILDFLOWER_BANDAGE);
                        register.accept(BirchItems.WILDFLOWER_POPPER);

                        register.accept(BirchItems.THREE_SUGARCANE);

                        register.accept(BirchItems.SPLINTERING_SPAWN);
                        register.accept(BirchItems.NESTHEAD_SPAWN);
                        register.accept(BirchItems.BLOOMING_REMAINS_SPAWN);
                    })
                    .build());
}
