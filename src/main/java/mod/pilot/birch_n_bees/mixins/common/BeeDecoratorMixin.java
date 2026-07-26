package mod.pilot.birch_n_bees.mixins.common;

import net.minecraft.world.level.levelgen.feature.treedecorators.BeehiveDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeehiveDecorator.class)
public abstract class BeeDecoratorMixin extends TreeDecorator {
    @Mutable
    @Shadow @Final private float probability;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initMixin(float probability, CallbackInfo ci){
        this.probability = .03f;
    }
}
