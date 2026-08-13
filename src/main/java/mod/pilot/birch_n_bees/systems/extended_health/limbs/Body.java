package mod.pilot.birch_n_bees.systems.extended_health.limbs;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public abstract sealed class Body<P extends Player> permits Body.Client, Body.Server {
    public Body(P player, Limb<P>[] limbs, boolean client){
        this.player = player;
        this.limbs = limbs;
        this.clientSide = client;
    }
    public final P player;
    public Limb<P>[] limbs;
    public int size() {return limbs.length;}
    @SuppressWarnings("unchecked")
    public void unsafeSet(Limb<?> limb, int index){
        limbs[index] = (Limb<P>)limb;
    }
    public @Nullable Limb<?> getLimbByID(Identifier ID){
        for (Limb<P> limb : limbs) if (limb.ID.equals(ID)) return limb;
        return null;
    }
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
