package mod.pilot.birch_n_bees.mixins.common;

import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.treedecorators.BeehiveDecorator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Deprecated
@Mixin(TreeFeatures.class)
public abstract class BeeHiveRarityMixin {
    @Shadow
    private static TreeConfiguration.TreeConfigurationBuilder createStraightBlobTree(Block logBlock, Block leavesBlock, int baseHeight, int heightRandA, int heightRandB, int radius) {
        return null;
    }

    @Unique private static final BeehiveDecorator ballad$birchBeehiveDecorator = new BeehiveDecorator(0.75f);

    /**
     * @author Dr. Pilot
     * @reason testing to see if overwriting will get this fuckass beehive thing to work
     */
    @Overwrite
    private static TreeConfiguration.TreeConfigurationBuilder createBirch() {
        System.out.println("FUCK YOU (Overwrite birch trees)");
        return createStraightBlobTree(Blocks.TNT, Blocks.BIRCH_LEAVES, 5, 2, 0, 2).ignoreVines();
    }


    /*@Inject(method = "createBirch", at = @At("RETURN"), cancellable = true)
    private static void ModifyBirchConstructor(CallbackInfoReturnable<TreeConfiguration.TreeConfigurationBuilder> cir){
        System.out.println("Modified birch trees!");
        //cir.setReturnValue(cir.getReturnValue().decorators(List.of(root.json$birchBeehiveDecorator)));
        cir.setReturnValue(createStraightBlobTree(Blocks.TNT, Blocks.BIRCH_LEAVES, 5, 2, 0, 2).ignoreVines());
    }*/

    @Inject(method = "createSuperBirch", at = @At("RETURN"), cancellable = true)
    private static void ModifySuperBirchConstructor(CallbackInfoReturnable<TreeConfiguration.TreeConfigurationBuilder> cir){
        System.out.println("Modified SUPER birch trees!");
        cir.setReturnValue(cir.getReturnValue().decorators(List.of(ballad$birchBeehiveDecorator)));
    }

    @ModifyArg(method = "bootstrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/feature/treedecorators/BeehiveDecorator;<init>(F)V"), index = 0)
    private static float ModifyBeehiveChance(float probability){
        System.out.println("Modified beehive chance from " + probability + " to " + 1f);
        return /*Math.min(1f, probability * 10);*/ 1f;
    }
}
