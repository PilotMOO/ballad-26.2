package mod.pilot.birch_n_bees.achievements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CustomTrigger extends SimpleCriterionTrigger<CustomTrigger.Instance> {
    public CustomTrigger(String id){
        this.id = id;
    }
    public final String id;

    public Instance newInstance() {
        return new Instance(Optional.empty()/*, id*/);
    }

    public void trigger(Entity entity){ if (entity instanceof ServerPlayer sP) trigger(sP); }
    public void trigger(ServerPlayer player){
        this.trigger(player, (instance) -> true);
    }

    @Override
    public @NotNull Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public record Instance(Optional<ContextAwarePredicate> player/*, String id*/) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(
                p_348086_ -> p_348086_.group(
                                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player")
                                        .forGetter(Instance::player)
                        )
                        .apply(p_348086_, Instance::new)
        );
    }
}
