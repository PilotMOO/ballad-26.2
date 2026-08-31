package mod.pilot.birch_n_bees.items.unique.testing;

import mod.pilot.birch_n_bees.screens.ExtendedHealthMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class HealthMenuWand extends Item implements MenuProvider {
    public HealthMenuWand(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.openMenu(this);
        return super.use(level, player, hand);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("fuck you");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new ExtendedHealthMenu(i, inventory, player);
    }
}
