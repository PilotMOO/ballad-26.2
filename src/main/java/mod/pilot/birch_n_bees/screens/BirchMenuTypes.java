package mod.pilot.birch_n_bees.screens;

import mod.pilot.birch_n_bees.ABOBAB;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BirchMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES
            = DeferredRegister.create(Registries.MENU, ABOBAB.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedHealthMenu>> HEALTH_UI_MENU_TYPE =
            MENU_TYPES.register("extended_health_ui", () -> new MenuType<>(ExtendedHealthMenu::new, FeatureFlagSet.of()));
}
