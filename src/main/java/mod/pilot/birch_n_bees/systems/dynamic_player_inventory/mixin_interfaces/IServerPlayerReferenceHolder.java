package mod.pilot.birch_n_bees.systems.dynamic_player_inventory.mixin_interfaces;

import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public interface IServerPlayerReferenceHolder {
    void reference(ServerPlayer player);
    @Nullable
    ServerPlayer getReference();
}
