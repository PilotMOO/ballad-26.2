package mod.pilot.birch_n_bees.systems.extended_health.limbs.default_limbs;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.systems.extended_health.HealthToken;
import mod.pilot.birch_n_bees.systems.extended_health.limbs.Limb;
import mod.pilot.birch_n_bees.systems.extended_health.limbs.LimbManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public class Arm {
    public static final Identifier
            LEFT_ARM = Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "left_arm"),
            RIGHT_ARM = Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "right_arm");

    private static final double THIRTY_DEG_IN_RAD = 0.523599d;
    private static final double PI_OVER_2 = Math.PI / 2;
    private static final double PI_OVER_4 = Math.PI / 4;

    public static class LeftClient extends Limb.Client{
        public LeftClient(AbstractClientPlayer player) {super(LEFT_ARM, player, 0.75f);}
        private LeftClient(){super(LEFT_ARM);}
    }
    public static class LeftServer extends Limb.Server{
        public LeftServer(ServerPlayer player) {super(LEFT_ARM, player, 0.75f);}
        private LeftServer() {super(LEFT_ARM);}

        @Override
        public boolean isDamageApplicableToLimb(ServerPlayer player, float amount, DamageSource source,
                                                double relativeYaw, double relativePitch, HealthToken token) {
            return LimbManager.validateAgainstDamageOnly(source, ID)
                    && relativePitch < PI_OVER_2
                    && relativePitch > -PI_OVER_2
                    && relativeYaw > THIRTY_DEG_IN_RAD
                    && relativeYaw < (Math.PI - THIRTY_DEG_IN_RAD);
        }

        @Override
        public float modifyLimbDamage(ServerPlayer player, float amount, DamageSource source,
                                      double relativeYaw, double relativePitch, HealthToken token) {
            float modAmount = amount;
            //If the arm isn't hit head-on (in between a 45 and 30 deg angle respective to its side)
            // then the arm takes half damage
            if (relativeYaw < PI_OVER_4 || relativeYaw > (Math.PI - PI_OVER_4)) modAmount /= 2;
            return super.modifyLimbDamage(player, modAmount, source, relativeYaw, relativePitch, token);
        }
    }
    public static class RightClient extends Limb.Client{
        public RightClient(AbstractClientPlayer player) {super(RIGHT_ARM, player, 0.75f);}
        private RightClient() {super(RIGHT_ARM);}
    }
    public static class RightServer extends Limb.Server{
        public RightServer(ServerPlayer player) {super(RIGHT_ARM, player, 0.75f);}
        private RightServer() {super(RIGHT_ARM);}
        @Override
        public boolean isDamageApplicableToLimb(ServerPlayer player, float amount, DamageSource source, double relativeYaw, double relativePitch, HealthToken token) {
            return LimbManager.validateAgainstDamageOnly(source, ID)
                    && relativePitch < PI_OVER_2
                    && relativePitch > -PI_OVER_2
                    && relativeYaw < -THIRTY_DEG_IN_RAD
                    && relativeYaw > (-Math.PI + THIRTY_DEG_IN_RAD);
        }

        @Override
        public float modifyLimbDamage(ServerPlayer player, float amount, DamageSource source,
                                      double relativeYaw, double relativePitch, HealthToken token) {
            float modAmount = amount;
            //If the arm isn't hit head-on (in between a 45 and 30 deg angle respective to its side)
            // then the arm takes half damage
            if (relativeYaw > -PI_OVER_4 || relativeYaw < (-Math.PI + PI_OVER_4)) modAmount /= 2;
            return super.modifyLimbDamage(player, modAmount, source, relativeYaw, relativePitch, token);
        }
    }

    public static final LimbManager.SimpleLimbSupplier LEFT_SUPPLIER = new LimbManager.SimpleLimbSupplier(LEFT_ARM,
            LeftClient::new, LeftClient::new,
            LeftServer::new, LeftServer::new);
    public static final LimbManager.SimpleLimbSupplier RIGHT_SUPPLIER = new LimbManager.SimpleLimbSupplier(RIGHT_ARM,
            RightClient::new, RightClient::new,
            RightServer::new, RightServer::new);
}
