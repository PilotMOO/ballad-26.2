package mod.pilot.birch_n_bees.mixins.common;

import mod.pilot.birch_n_bees.effects.BirchEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemUseMixin implements FeatureElement, ItemLike, IItemExtension {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void panickingItemUse(Level level, Player player, InteractionHand hand,
                                  CallbackInfoReturnable<InteractionResult> cir){
        MobEffectInstance effect = player.getEffect(BirchEffects.FEAR_EFFECT);
        if (effect != null){
            int amplifier = effect.getAmplifier() + 2;
            float chance = 1f / amplifier;
            if (player.getRandom().nextFloat() > chance){
                cir.cancel();
                cir.setReturnValue(InteractionResult.FAIL);
                player.stopUsingItem();
                player.sendSystemMessage(Component.translatable("birch_n_bees.panicking"));
            }
        }
    }
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void panickingUseOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir){
        Player player = context.getPlayer();
        if (player != null) {
            MobEffectInstance effect = player.getEffect(BirchEffects.FEAR_EFFECT);
            if (effect != null) {
                int amplifier = effect.getAmplifier() + 2;
                float chance = 1f / amplifier;
                if (player.getRandom().nextFloat() > chance) {
                    cir.cancel();
                    cir.setReturnValue(InteractionResult.FAIL);
                    player.stopUsingItem();
                    player.sendOverlayMessage(Component.translatable("birch_n_bees.panicking"));
                }
            }
        }
    }
}
