package mod.pilot.birch_n_bees.systems.extended_health.limbs;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public abstract sealed class Body<P extends Player> permits Body.Client, Body.Server {
    public Body(Limb<P>[] limbs, boolean client){
        this.limbs = limbs;
        this.clientSide = client;
    }
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

    @SuppressWarnings("unchecked")
    public static Body<?> buildSidedBody(Limb<?>[] limbs, boolean client){
        return client ?
                new Client((Limb<AbstractClientPlayer>[])limbs) :
                new Server((Limb<ServerPlayer>[])limbs);
    }
    public static non-sealed class Client extends Body<AbstractClientPlayer>{
        public Client(Limb<AbstractClientPlayer>[] limbs) {
            super(limbs, true);
        }
    }
    public static non-sealed class Server extends Body<ServerPlayer>{
        public Server(Limb<ServerPlayer>[] limbs) {
            super(limbs, false);
        }
    }

    @Override
    public String toString() {
        return "Body{" +
                "limbs=" + Arrays.toString(limbs) +
                ", clientSide=" + clientSide +
                '}';
    }
}
