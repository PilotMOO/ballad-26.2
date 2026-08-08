package mod.pilot.birch_n_bees.mixins.common;

import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.DynamicInventoryToken;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class OffhandContainerSwapRestrictionMixin {
    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void validateOffhandBeforeSwap(Slot slot, int slotId, int buttonNum, ContainerInput containerInput, CallbackInfo ci){
        if (buttonNum == 40) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                DynamicInventoryToken token = DynamicInventoryToken.get(mc.player);
                if (!token.offhand) ci.cancel();
            }
        }
    }
}
