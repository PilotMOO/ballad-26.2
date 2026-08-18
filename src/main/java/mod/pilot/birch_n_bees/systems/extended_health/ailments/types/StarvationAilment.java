package mod.pilot.birch_n_bees.systems.extended_health.ailments.types;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.systems.extended_health.HealthToken;
import mod.pilot.birch_n_bees.systems.extended_health.ailments.Ailment;
import mod.pilot.birch_n_bees.systems.extended_health.ailments.AilmentInstance;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class StarvationAilment extends Ailment {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "starvation");
    public StarvationAilment() {super(ID);}

    @Override
    public AilmentInstance<?> buildInstance(boolean client, byte severity, int timeUntilCured) {
        if (client){
            return new AilmentInstance.Client(this, severity);
        } else return new AilmentInstance.Server(this, severity, timeUntilCured){
            @Override
            public boolean cureCriteria(ServerPlayer player, HealthToken token) {
                return false;
            }
        };
    }

    @Override
    public void onApplicationClient(AilmentInstance.Client instance, AbstractClientPlayer player, HealthToken token) {}

    public static final Identifier STARVATION_SPEED_MODIFIER_ID =
            Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "starvation_speed_mod");
    public static final AttributeModifier STARVATION_SPEED_MODIFIER = new AttributeModifier(
            STARVATION_SPEED_MODIFIER_ID,
            -0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE
    );
    @Override
    public void onApplicationServer(AilmentInstance.Server instance, ServerPlayer player, HealthToken token) {
        player.getAttribute(Attributes.BLOCK_BREAK_SPEED)
                .addOrUpdateTransientModifier(STARVATION_SPEED_MODIFIER);
        player.getAttribute(Attributes.ATTACK_SPEED)
                .addOrUpdateTransientModifier(STARVATION_SPEED_MODIFIER);
    }

    @Override public void alieveSideEffectsClient(AilmentInstance.Client instance, AbstractClientPlayer player, HealthToken token) {}

    @Override public void alieveSideEffectsServer(AilmentInstance.Server instance, ServerPlayer player,
                                                  HealthToken token) {
        player.getAttribute(Attributes.BLOCK_BREAK_SPEED)
                .removeModifier(STARVATION_SPEED_MODIFIER);
        player.getAttribute(Attributes.ATTACK_SPEED)
                .removeModifier(STARVATION_SPEED_MODIFIER);
    }

    @Override
    public AilmentInstance.Client constructDefaultClientInstance() {
        return new AilmentInstance.Client(this, (byte)0);
    }
    @Override
    public AilmentInstance.Server constructDefaultServerInstance() {
        return new AilmentInstance.Server(this, (byte)0, -1){
            @Override
            public boolean cureCriteria(ServerPlayer player, HealthToken token) {
                return false;
            }
        };
    }
}
