package mod.pilot.birch_n_bees.systems.extended_health.limbs.default_limbs;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.systems.extended_health.HealthToken;
import mod.pilot.birch_n_bees.systems.extended_health.limbs.Limb;
import mod.pilot.birch_n_bees.systems.extended_health.limbs.LimbManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public class Leg {
    public static final Identifier
            LEFT_LEG = Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "left_leg"),
            RIGHT_LEG = Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "right_leg");

    public static class LeftClient extends Limb.Client{
        public LeftClient(AbstractClientPlayer player) {super(LEFT_LEG, player, 1f);}
        private LeftClient() {super(LEFT_LEG);}
    }
    public static class LeftServer extends Limb.Server{
        public LeftServer(ServerPlayer player) {super(LEFT_LEG, player, 1f);}
        private LeftServer() {super(LEFT_LEG);}
        @Override
        public float modifyLimbDamage(float amount, DamageSource source, float relativeYaw, float relativePitch, HealthToken token) {
            //ToDo implement directional damage calculations
            return super.modifyLimbDamage(amount, source, relativeYaw, relativePitch, token);
        }
    }
    public static class RightClient extends Limb.Client{
        public RightClient(AbstractClientPlayer player) {super(RIGHT_LEG, player, 1f);}
        private RightClient() {super(RIGHT_LEG);}
    }
    public static class RightServer extends Limb.Server{
        public RightServer(ServerPlayer player) {super(RIGHT_LEG, player, 1f);}
        private RightServer() {super(RIGHT_LEG);}

        @Override
        public float modifyLimbDamage(float amount, DamageSource source, float relativeYaw, float relativePitch, HealthToken token) {
            //ToDo implement directional damage calculations
            return super.modifyLimbDamage(amount, source, relativeYaw, relativePitch, token);
        }
    }

    public static final LimbManager.SimpleLimbSupplier LEFT_SUPPLIER = new LimbManager.SimpleLimbSupplier(LEFT_LEG,
            LeftClient::new, LeftClient::new,
            LeftServer::new, LeftServer::new);
    public static final LimbManager.SimpleLimbSupplier RIGHT_SUPPLIER = new LimbManager.SimpleLimbSupplier(RIGHT_LEG,
            RightClient::new, RightClient::new,
            RightServer::new, RightServer::new);
}
