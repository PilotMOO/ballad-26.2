package mod.pilot.birch_n_bees.systems.extended_health.limbs.default_limbs;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.systems.extended_health.limbs.Limb;
import mod.pilot.birch_n_bees.systems.extended_health.limbs.LimbManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class Torso {
    public static final Identifier TORSO = Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "torso");

    public static class Client extends Limb.Client{
        protected Client(AbstractClientPlayer player) {
            super(TORSO, player, 2f);
        }
    }
    public static class Server extends Limb.Server{
        protected Server(ServerPlayer player) {
            super(TORSO, player, 2f);
        }
    }

    public static final LimbManager.SimpleLimbSupplier SUPPLIER = new LimbManager.SimpleLimbSupplier(TORSO, Client::new, Server::new);

}
