package mod.pilot.birch_n_bees.mixins.common;

import com.mojang.authlib.GameProfile;
import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.mixin_interfaces.IPlayerReference;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerListenerMixin extends Player {
    public ServerPlayerListenerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Inject(method = "initMenu", at = @At("HEAD"))
    private void menuListener(AbstractContainerMenu container, CallbackInfo ci){
        Player player = this;
        if (container instanceof IPlayerReference referenceHolder
                && player instanceof ServerPlayer sPlayer){
            referenceHolder.reference(sPlayer);
        }
    }
}
