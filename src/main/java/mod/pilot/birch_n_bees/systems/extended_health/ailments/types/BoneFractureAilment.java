package mod.pilot.birch_n_bees.systems.extended_health.ailments.types;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.systems.extended_health.HealthToken;
import mod.pilot.birch_n_bees.systems.extended_health.ailments.Ailment;
import mod.pilot.birch_n_bees.systems.extended_health.ailments.AilmentInstance;
import mod.pilot.birch_n_bees.systems.extended_health.ailments.LocalizedAilmentInstance;
import mod.pilot.birch_n_bees.systems.extended_health.limbs.default_limbs.Arm;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class BoneFractureAilment extends Ailment {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "bone_fracture");
    public BoneFractureAilment() {super(ID);}

    @Override
    public AilmentInstance<?> buildInstance(boolean client, byte severity, int timeUntilCured) {
        if (client){
            return new LocalizedAilmentInstance.Client(this, null, severity);
        } else return new LocalizedAilmentInstance.Server(this, null, severity, timeUntilCured);
    }

    public static final Identifier BONE_FRACTURE_SPEED_MODIFIER_ID =
            Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "bone_fracture_speed_mod");
    public static final AttributeModifier BONE_FRACTURE_SPEED_MODIFIER = new AttributeModifier(
            BONE_FRACTURE_SPEED_MODIFIER_ID,
            -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE
    );
    //ToDo: test this on application bs
    @Override
    public void onApplicationServer(AilmentInstance.Server instance, ServerPlayer player, HealthToken token) {
        Identifier id = ((LocalizedAilmentInstance)instance).limbID();
        boolean right;
        if (id.equals(Arm.LEFT_ARM) || (right = id.equals(Arm.RIGHT_ARM))){
            boolean playerRightHanded = player.getMainArm().equals(HumanoidArm.RIGHT);
        }
        token.healingEfficiency -= 5;
        HealthToken.sync(player);
    }

    @Override public void alieveSideEffectsServer(AilmentInstance.Server instance, ServerPlayer player,
                                                  HealthToken token) {
        token.healingEfficiency += 5;
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
