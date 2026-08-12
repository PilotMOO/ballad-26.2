package mod.pilot.birch_n_bees.systems.extended_health.limbs.default_limbs;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.systems.extended_health.limbs.Limb;
import mod.pilot.birch_n_bees.systems.extended_health.limbs.LimbManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class Head {
    public static final Identifier HEAD = Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "head");

    public static class Client extends Limb.Client{
        protected Client(AbstractClientPlayer player) {
            super(HEAD, player, 1.25f);
        }
    }
    public static class Server extends Limb.Server{
        protected Server(ServerPlayer player) {
            super(HEAD, player, 1.25f);
        }
    }

    public static final LimbManager.SimpleLimbSupplier SUPPLIER = new LimbManager.SimpleLimbSupplier(HEAD, Client::new, Server::new);
}
