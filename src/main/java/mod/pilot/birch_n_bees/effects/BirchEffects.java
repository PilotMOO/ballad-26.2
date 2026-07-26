package mod.pilot.birch_n_bees.effects;

import mod.pilot.birch_n_bees.ABOBAB;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BirchEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, ABOBAB.MOD_ID);

    public static final DeferredHolder<MobEffect, FearEffect> FEAR_EFFECT =
            EFFECTS.register("fear", FearEffect::new);
}
