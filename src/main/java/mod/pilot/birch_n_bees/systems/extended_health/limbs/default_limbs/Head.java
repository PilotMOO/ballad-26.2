package mod.pilot.birch_n_bees.systems.extended_health.limbs.default_limbs;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.systems.extended_health.HealthToken;
import mod.pilot.birch_n_bees.systems.extended_health.limbs.Limb;
import mod.pilot.birch_n_bees.systems.extended_health.limbs.LimbManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public class Head {
    public static final Identifier HEAD = Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "head");
    private static final double THIRTY_DEG_IN_RAD = 0.523599d;

    public static class Client extends Limb.Client{
        public Client(AbstractClientPlayer player) {
            super(HEAD, player, 1.25f);
        }
        private Client() {super(HEAD);}
    }
    public static class Server extends Limb.Server{
        public Server(ServerPlayer player) {
            super(HEAD, player, 1.25f);
        }
        private Server() {super(HEAD);}

        @Override
        public boolean isDamageApplicableToLimb(ServerPlayer player, float amount, DamageSource source,
                                                double relativeYaw, double relativePitch, HealthToken token) {
            return LimbManager.validateAgainstDamageOnly(source, ID) && relativePitch > THIRTY_DEG_IN_RAD;
        }
    }

    public static final LimbManager.SimpleLimbSupplier SUPPLIER = new LimbManager.SimpleLimbSupplier(HEAD,
            Client::new, Client::new,
            Server::new, Server::new);
}
