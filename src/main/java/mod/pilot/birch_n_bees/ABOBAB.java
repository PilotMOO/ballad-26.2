package mod.pilot.birch_n_bees;

import com.mojang.serialization.MapCodec;
import mod.pilot.birch_n_bees.achievements.BirchCriteriaRegistries;
import mod.pilot.birch_n_bees.blocks.BirchBlocks;
import mod.pilot.birch_n_bees.effects.BirchEffects;
import mod.pilot.birch_n_bees.entity.BirchEntities;
import mod.pilot.birch_n_bees.items.BirchCreativeTabs;
import mod.pilot.birch_n_bees.items.BirchItems;
import mod.pilot.birch_n_bees.systems.HotBrickWatcher;
import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.DynamicPlayerInventoryManager;
import mod.pilot.birch_n_bees.util.BirchAttachmentTypes;
import mod.pilot.birch_n_bees.util.BirchBiomeModification;
import mod.pilot.birch_n_bees.util.BirchDataComponents;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;

@Mod(ABOBAB.MOD_ID)
public class ABOBAB {
    public static final String MOD_ID = "birch_n_bees";

    public ABOBAB(IEventBus modEventBus, ModContainer modContainer)
    {
        modContainer.registerConfig(ModConfig.Type.STARTUP, Config.SERVER_SPEC, "ballad_config.toml");
        configLoaded = true;
        HotBrickWatcher.init(Config.SERVER.brickCookTime.get());
        DynamicPlayerInventoryManager.modInit(modEventBus);

        BirchAttachmentTypes.ATTACHMENT_TYPES.register(modEventBus);
        BirchBlocks.BLOCKS.register(modEventBus);
        BirchBlocks.BLOCK_ENTITIES.register(modEventBus);
        BirchDataComponents.DATA_COMPONENTS.register(modEventBus);
        BirchItems.ITEMS.register(modEventBus);
        BirchEffects.EFFECTS.register(modEventBus);
        BirchCreativeTabs.TABS.register(modEventBus);
        BirchEntities.ENTITIES.register(modEventBus);
        BirchCriteriaRegistries.TRIGGERS.register(modEventBus);

        final DeferredRegister<MapCodec<? extends BiomeModifier>> biomeModifiers =
                DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MOD_ID);
        biomeModifiers.register(modEventBus);
        biomeModifiers.register("birch_spawns", () -> BirchBiomeModification.CODEC);
    }
    public static boolean configLoaded = false;

    public static void onServerSetup(ServerStartedEvent event){
        boolean setup = DynamicPlayerInventoryManager.setup;
        System.out.println("have we already set shit up? " + setup + ", are we on the client? "
                + DynamicPlayerInventoryManager.isClientSide());
        if (!setup) {
            DynamicPlayerInventoryManager.systemSetup(false, DynamicPlayerInventoryManager.DEFAULT_TOKEN_CONSTRUCTOR);
        }
    }
}
