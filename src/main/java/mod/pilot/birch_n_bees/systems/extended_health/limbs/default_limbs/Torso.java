package mod.pilot.birch_n_bees.systems.extended_health.limbs.default_limbs;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.systems.extended_health.HealthToken;
import mod.pilot.birch_n_bees.systems.extended_health.limbs.Limb;
import mod.pilot.birch_n_bees.systems.extended_health.limbs.LimbManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public class Torso {
    public static final Identifier TORSO = Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "torso");
    private static final double THIRTY_DEG_IN_RAD = 0.523599d;
    private static final double PI_OVER_2 = Math.PI / 2;
    private static final double PI_OVER_3 = Math.PI / 3;

    public static class Client extends Limb.Client{
        public Client(AbstractClientPlayer player) {
            super(TORSO, player, 2f);
        }
        private Client() {super(TORSO);}
    }
    public static class Server extends Limb.Server{
        protected Server(ServerPlayer player) {
            super(TORSO, player, 2f);
        }
        private Server() {super(TORSO);}

        @Override
        public boolean isDamageApplicableToLimb(ServerPlayer player, float amount, DamageSource source,
                                                double relativeYaw, double relativePitch, HealthToken token) {
            boolean dmgOnlyFlag = LimbManager.validateAgainstDamageOnly(source, ID);
            if (!dmgOnlyFlag) return false;

            if (relativePitch > (Math.PI - PI_OVER_3)) return false;
            else if (relativePitch < -THIRTY_DEG_IN_RAD) return false;

            boolean rightArm = false;
            if (relativeYaw < -THIRTY_DEG_IN_RAD
                    && relativeYaw > (-Math.PI + THIRTY_DEG_IN_RAD)) rightArm = true;
            else if (relativeYaw < THIRTY_DEG_IN_RAD || relativeYaw > (Math.PI - THIRTY_DEG_IN_RAD)) return true;
            Limb.Server arm = (Limb.Server)token.getLimb(rightArm ? Arm.RIGHT_ARM : Arm.LEFT_ARM);
            if (arm == null){
                INTERNAL_coveredByDeadArm = false;
                return true;
            }
            if (arm.isDead()){
                INTERNAL_coveredByDeadArm = true;
                return true;
            } else return false;
        }
        boolean INTERNAL_coveredByDeadArm;

        @Override
        public float modifyLimbDamage(ServerPlayer player, float amount, DamageSource source,
                                      double relativeYaw, double relativePitch, HealthToken token) {
            float modAmount = amount;
            if (relativePitch > PI_OVER_2) modAmount /= 2;
            if (INTERNAL_coveredByDeadArm) modAmount /= 2;
            return super.modifyLimbDamage(player, modAmount, source, relativeYaw, relativePitch, token);
        }
    }

    public static final LimbManager.SimpleLimbSupplier SUPPLIER = new LimbManager.SimpleLimbSupplier(TORSO,
            Client::new, Client::new,
            Server::new, Server::new);
}
