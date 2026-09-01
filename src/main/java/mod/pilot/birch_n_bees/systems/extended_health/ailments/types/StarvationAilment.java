package mod.pilot.birch_n_bees.systems.extended_health.ailments.types;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.systems.extended_health.HealthToken;
import mod.pilot.birch_n_bees.systems.extended_health.ailments.Ailment;
import mod.pilot.birch_n_bees.systems.extended_health.ailments.AilmentInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

@Deprecated
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

    public static final Identifier STARVATION_SPEED_MODIFIER_ID =
            Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "starvation_speed_mod");
    public static final AttributeModifier STARVATION_SPEED_MODIFIER = new AttributeModifier(
            STARVATION_SPEED_MODIFIER_ID,
            -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE
    );
    //ToDo: test this on application bs
    @Override
    public void onApplicationServer(AilmentInstance.Server instance, ServerPlayer player, HealthToken token) {
        System.out.println("on application!!!! ");
        //This might need some testng but yeah dikajgd g
        player.getAttribute(Attributes.BLOCK_BREAK_SPEED)
                .addOrUpdateTransientModifier(STARVATION_SPEED_MODIFIER);
        player.getAttribute(Attributes.ATTACK_SPEED)
                .addOrUpdateTransientModifier(STARVATION_SPEED_MODIFIER);
        player.getAttribute(Attributes.MOVEMENT_SPEED)
                .addOrUpdateTransientModifier(STARVATION_SPEED_MODIFIER);
        player.getAttribute(Attributes.JUMP_STRENGTH)
                .addOrUpdateTransientModifier(STARVATION_SPEED_MODIFIER);
        token.healingEfficiency -= 10;
        HealthToken.sync(player);
    }

    @Override public void alieveSideEffectsServer(AilmentInstance.Server instance, ServerPlayer player,
                                                  HealthToken token) {
        System.out.println("Guh im no longer hungry!!!");
        player.getAttribute(Attributes.BLOCK_BREAK_SPEED)
                .removeModifier(STARVATION_SPEED_MODIFIER);
        player.getAttribute(Attributes.ATTACK_SPEED)
                .removeModifier(STARVATION_SPEED_MODIFIER);
        player.getAttribute(Attributes.MOVEMENT_SPEED)
                .removeModifier(STARVATION_SPEED_MODIFIER);
        player.getAttribute(Attributes.JUMP_STRENGTH)
                .removeModifier(STARVATION_SPEED_MODIFIER);
        token.healingEfficiency += 10;
        HealthToken.sync(player);
    }

    @Override
    public AilmentInstance.Client constructDefaultClientInstance() {
        return new AilmentInstance.Client(this, (byte)0);
    }
    @Override
    public AilmentInstance.Server constructDefaultServerInstance() {
        //Set to 60 ticks for testing
        return new AilmentInstance.Server(this, (byte)0, 60/*00*/){
            @Override
            public boolean cureCriteria(ServerPlayer player, HealthToken token) {
                return player.getFoodData().hasEnoughFood();
            }
        };
    }
}
