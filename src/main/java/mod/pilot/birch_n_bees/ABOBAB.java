package mod.pilot.birch_n_bees;

import com.mojang.serialization.MapCodec;
import mod.pilot.birch_n_bees.achievements.BirchCriteriaRegistries;
import mod.pilot.birch_n_bees.blocks.BirchBlocks;
import mod.pilot.birch_n_bees.effects.BirchEffects;
import mod.pilot.birch_n_bees.entity.BirchEntities;
import mod.pilot.birch_n_bees.items.BirchCreativeTabs;
import mod.pilot.birch_n_bees.items.BirchItems;
import mod.pilot.birch_n_bees.systems.HotBrickWatcher;
import mod.pilot.birch_n_bees.systems.extended_health.AilmentManager;
import mod.pilot.birch_n_bees.util.BirchAttachmentTypes;
import mod.pilot.birch_n_bees.util.BirchBiomeModification;
import mod.pilot.birch_n_bees.util.BirchDataComponents;
import net.neoforged.neoforge.common.world.BiomeModifier;
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

        BirchAttachmentTypes.ATTACHMENT_TYPES.register(modEventBus);
        BirchBlocks.BLOCKS.register(modEventBus);
        BirchBlocks.BLOCK_ENTITIES.register(modEventBus);
        BirchDataComponents.DATA_COMPONENTS.register(modEventBus);
        BirchItems.ITEMS.register(modEventBus);
        BirchEffects.EFFECTS.register(modEventBus);
        BirchCreativeTabs.TABS.register(modEventBus);
        BirchEntities.ENTITIES.register(modEventBus);
        BirchCriteriaRegistries.TRIGGERS.register(modEventBus);

        AilmentManager.init(modEventBus);

        final DeferredRegister<MapCodec<? extends BiomeModifier>> biomeModifiers =
                DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MOD_ID);
        biomeModifiers.register(modEventBus);
        biomeModifiers.register("birch_spawns", () -> BirchBiomeModification.CODEC);
    }
    public static boolean configLoaded = false;
}
