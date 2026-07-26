package mod.pilot.birch_n_bees.mixins.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import mod.pilot.birch_n_bees.util.BirchTags;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends Player {
    public AbstractClientPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @ModifyExpressionValue(method = "getFieldOfViewModifier",
    at = @At(value = "INVOKE", target ="Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z"))
    private boolean fovZoomMixin(boolean original){
        return original || getUseItem().is(BirchTags.Items.CAUSES_FOV_ZOOM);
    }
}
