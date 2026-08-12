package mod.pilot.birch_n_bees.systems.extended_health.limbs;

import mod.pilot.birch_n_bees.systems.extended_health.IHealthTokenSerializable;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public abstract sealed class Body<P extends Player> permits Body.Client, Body.Server {
    public Body(P player, Limb<P>[] limbs, boolean client){
        this.player = player;
        this.limbs = limbs;
        this.clientSide = client;
    }
    public final P player;
    public Limb<P>[] limbs;
    public final boolean clientSide;

    public static non-sealed class Client extends Body<AbstractClientPlayer>{
        public Client(AbstractClientPlayer player, Limb<AbstractClientPlayer>[] limbs) {
            super(player, limbs, true);
        }
    }
    public static non-sealed class Server extends Body<ServerPlayer>{
        public Server(ServerPlayer player, Limb<ServerPlayer>[] limbs) {
            super(player, limbs, false);
        }
    }
}
