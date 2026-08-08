package mod.pilot.birch_n_bees.mixins.common;

import com.mojang.authlib.GameProfile;
import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.DynamicInventory;
import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.DynamicInventoryToken;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    public ServerPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Inject(method = "initMenu", at = @At("HEAD"))
    private void menuListener(AbstractContainerMenu container, CallbackInfo ci){
        /*if (!DynamicInventoryToken.has(this)) {
            this.setData(DynamicInventoryToken.ATTACHMENT, new DynamicInventoryToken());
        }*/
        System.out.println("applying the shits from the server");
        DynamicInventoryToken.applyInFull(this);
    }
}
