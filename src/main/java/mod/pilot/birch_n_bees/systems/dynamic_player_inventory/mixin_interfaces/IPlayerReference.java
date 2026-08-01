package mod.pilot.birch_n_bees.systems.dynamic_player_inventory.mixin_interfaces;

import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public interface IPlayerReference {
    void reference(Player player);
    @Nullable
    Player getReference();
}
