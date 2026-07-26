package mod.pilot.birch_n_bees.mixins.common;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.Config;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(OverworldBiomeBuilder.class)
public abstract class BirchInfector {
    @Unique
    private static final Random ballad$random = new Random();

    @Unique private static double ballad$birchChance = -1d;
    @Unique private static double ballad$getBirchChance(){
        if (!ABOBAB.configLoaded) return -1d;
        if (ballad$birchChance == -1) ballad$birchChance = Config.SERVER.birchChance.get();
        return ballad$birchChance;
    }

    @Unique private static boolean ballad$chance(){
        return ballad$random.nextDouble() <= ballad$getBirchChance();
    }
    @Unique private static ResourceKey<Biome> ballad$birch(){
        return Biomes.BIRCH_FOREST;
    }
    @Unique private static ResourceKey<Biome> ballad$old_birch(){
        return Biomes.OLD_GROWTH_BIRCH_FOREST;
    }

    @Inject(method = "pickMiddleBiome", at = @At("HEAD"), cancellable = true)
    private void birchifyMiddleBiome(int temperature, int humidity, Climate.Parameter param,
                                     CallbackInfoReturnable<ResourceKey<Biome>> cir){
        if (ballad$chance()) cir.setReturnValue(ballad$random.nextDouble() <= 0.25 ? ballad$birch() : ballad$old_birch());
    }
    @Inject(method = "pickMiddleBiomeOrBadlandsIfHot", at = @At("HEAD"), cancellable = true)
    private void birchifyMiddleOrBad(int temperature, int humidity, Climate.Parameter param, CallbackInfoReturnable<ResourceKey<Biome>> cir){
        if (ballad$chance()) cir.setReturnValue(ballad$random.nextDouble() <= 0.25 ? ballad$birch() : ballad$old_birch());
    }
    @Inject(method = "pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold", at = @At("HEAD"), cancellable = true)
    private void birchifyMiddleOrBadOrSlope(int temperature, int humidity, Climate.Parameter param, CallbackInfoReturnable<ResourceKey<Biome>> cir){
        if (ballad$chance()) cir.setReturnValue(ballad$random.nextDouble() <= 0.25 ? ballad$birch() : ballad$old_birch());
    }
    @Inject(method = "maybePickWindsweptSavannaBiome", at = @At("HEAD"), cancellable = true)
    private void birchifyWindsweptSavanna(int temperature, int humidity, Climate.Parameter param, ResourceKey<Biome> key, CallbackInfoReturnable<ResourceKey<Biome>> cir){
        if (ballad$chance()) cir.setReturnValue(ballad$random.nextDouble() <= 0.25 ? ballad$birch() : ballad$old_birch());
    }
    @Inject(method = "pickShatteredCoastBiome", at = @At("HEAD"), cancellable = true)
    private void birchifyShatteredCoast(int temperature, int humidity, Climate.Parameter param, CallbackInfoReturnable<ResourceKey<Biome>> cir){
        if (ballad$chance()) cir.setReturnValue(ballad$random.nextDouble() <= 0.25 ? ballad$birch() : ballad$old_birch());
    }
    @Inject(method = "pickBeachBiome", at = @At("HEAD"), cancellable = true)
    private void birchifyBeach(int temperature, int humidity, CallbackInfoReturnable<ResourceKey<Biome>> cir){
        if (ballad$chance()) cir.setReturnValue(ballad$random.nextDouble() <= 0.25 ? ballad$birch() : ballad$old_birch());
    }
    @Inject(method = "pickBadlandsBiome", at = @At("HEAD"), cancellable = true)
    private void birchifyBadlands(int humidity, Climate.Parameter param, CallbackInfoReturnable<ResourceKey<Biome>> cir){
        if (ballad$chance()) cir.setReturnValue(ballad$random.nextDouble() <= 0.25 ? ballad$birch() : ballad$old_birch());
    }
    @Inject(method = "pickPlateauBiome", at = @At("HEAD"), cancellable = true)
    private void birchifyPlateau(int temperature, int humidity, Climate.Parameter param, CallbackInfoReturnable<ResourceKey<Biome>> cir){
        if (ballad$chance()) cir.setReturnValue(ballad$random.nextDouble() <= 0.25 ? ballad$birch() : ballad$old_birch());
    }
    @Inject(method = "pickPeakBiome", at = @At("HEAD"), cancellable = true)
    private void birchifyPeak(int temperature, int humidity, Climate.Parameter param, CallbackInfoReturnable<ResourceKey<Biome>> cir){
        if (ballad$chance()) cir.setReturnValue(ballad$random.nextDouble() <= 0.25 ? ballad$birch() : ballad$old_birch());
    }
    @Inject(method = "pickSlopeBiome", at = @At("HEAD"), cancellable = true)
    private void birchifySlope(int temperature, int humidity, Climate.Parameter param, CallbackInfoReturnable<ResourceKey<Biome>> cir){
        if (ballad$chance()) cir.setReturnValue(ballad$random.nextDouble() <= 0.25 ? ballad$birch() : ballad$old_birch());
    }
    @Inject(method = "pickShatteredBiome", at = @At("HEAD"), cancellable = true)
    private void birchifyShattered(int temperature, int humidity, Climate.Parameter param, CallbackInfoReturnable<ResourceKey<Biome>> cir){
        if (ballad$chance()) cir.setReturnValue(ballad$random.nextDouble() <= 0.25 ? ballad$birch() : ballad$old_birch());
    }
}
