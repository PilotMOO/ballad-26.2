package mod.pilot.birch_n_bees;

import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.DynamicPlayerInventoryManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = ABOBAB.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ABOBAB.MOD_ID, value = Dist.CLIENT)
public class ABOBABClient {
    public ABOBABClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        DynamicPlayerInventoryManager.systemSetup(true, DynamicPlayerInventoryManager.DEFAULT_TOKEN_CONSTRUCTOR);
    }
}
