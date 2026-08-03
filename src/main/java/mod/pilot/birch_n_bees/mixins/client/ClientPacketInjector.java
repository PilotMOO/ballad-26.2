package mod.pilot.birch_n_bees.mixins.client;

import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.DynamicInventoryToken;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketInjector extends ClientCommonPacketListenerImpl implements ClientGamePacketListener, TickablePacketListener {
    protected ClientPacketInjector(Minecraft minecraft, Connection connection, CommonListenerCookie cookie) {
        super(minecraft, connection, cookie);
    }

    @Inject(method = "handleOpenScreen", at = @At(value = "RETURN"))
    private void applyToken(ClientboundOpenScreenPacket packet, CallbackInfo ci){
        /*System.out.println("Trying to shit yourself on the client");
        DynamicInventoryToken token = DynamicInventoryToken.get(this.minecraft.player);
        System.out.println("Token: " + token);
        token.applyToMenu(this.minecraft.player.containerMenu);*/
    }
}
