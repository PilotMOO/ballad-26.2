package mod.pilot.birch_n_bees.achievements;

import mod.pilot.birch_n_bees.ABOBAB;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BirchCriteriaRegistries {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(Registries.TRIGGER_TYPE, ABOBAB.MOD_ID);

    private static DeferredHolder<CriterionTrigger<?>, CustomTrigger> registerTrigger(String ID){
        return TRIGGERS.register(ID, () -> new CustomTrigger(ID));
    }
    private static DeferredHolder<CriterionTrigger<?>, IllegalTrigger> illegalTrigger(String ID){
        return TRIGGERS.register(ID, () -> new IllegalTrigger(ID));
    }

    public static DeferredHolder<CriterionTrigger<?>, CustomTrigger> ANYTHING_ILLEGAL = registerTrigger("illegal");
    public static DeferredHolder<CriterionTrigger<?>, IllegalTrigger> BREAK_BIRCH = illegalTrigger("break_birch");
    public static DeferredHolder<CriterionTrigger<?>, IllegalTrigger> ILLEGAL_HUNTING = illegalTrigger("illegal_hunting");
    public static DeferredHolder<CriterionTrigger<?>, IllegalTrigger> CONTRABAND = illegalTrigger("contraband");

    public static DeferredHolder<CriterionTrigger<?>, CustomTrigger> NIGHT = registerTrigger("night");
    public static DeferredHolder<CriterionTrigger<?>, CustomTrigger> SALMON = registerTrigger("salmon");
}
