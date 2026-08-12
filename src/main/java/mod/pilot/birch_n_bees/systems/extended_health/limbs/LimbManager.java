package mod.pilot.birch_n_bees.systems.extended_health.limbs;


import mod.pilot.birch_n_bees.systems.extended_health.limbs.default_limbs.Arm;
import mod.pilot.birch_n_bees.systems.extended_health.limbs.default_limbs.Head;
import mod.pilot.birch_n_bees.systems.extended_health.limbs.default_limbs.Leg;
import mod.pilot.birch_n_bees.systems.extended_health.limbs.default_limbs.Torso;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Function;

public class LimbManager {
    private static final int NATIVE_LIMB_COUNT = 6;
    public static void init(IEventBus bus){
        registered = new LimbDefaultInstanceSupplier[NATIVE_LIMB_COUNT];
        RegisterLimbsEvent event = new RegisterLimbsEvent();
        registerNativeLimbs(event);
        bus.post(event);
    }
    private static LimbDefaultInstanceSupplier[] registered;
    private static int count = 0;
    private static void register_INTERNAL(LimbDefaultInstanceSupplier supplier){
        if (count >= registered.length){
            LimbDefaultInstanceSupplier[] newArray = new LimbDefaultInstanceSupplier[count + 1];
            System.arraycopy(registered, 0, newArray, 0, count);
            registered = newArray;
        }
        registered[count++] = supplier;
    }

    public static LimbDefaultInstanceSupplier[] allLimbSuppliers(){
        return registered;
    }
    public static @Nullable LimbDefaultInstanceSupplier byIdentifier(Identifier id){
        for (LimbDefaultInstanceSupplier s : registered){
            if (s.getLimbID().equals(id)) return s;
        }
        return null;
    }

    public static @Nullable Body<?> constructDefaultSidedBody(Player player) {
        if (player instanceof AbstractClientPlayer cPlayer) return constructDefaultClientBody(cPlayer);
        else if (player instanceof ServerPlayer sPlayer) return constructDefaultServerBody(sPlayer);
        else return null;
    }
    public static Body.Client constructDefaultClientBody(AbstractClientPlayer player){
        Limb.Client[] limbs = new Limb.Client[NATIVE_LIMB_COUNT];
        for (int i = 0; i < NATIVE_LIMB_COUNT; i++){
            limbs[i] = registered[i].getDefaultClientInstance(player);
        }
        BuildBodyEvent.Client event = ModLoader.postEventWithReturn(new BuildBodyEvent.Client(player, limbs));
        limbs = (Limb.Client[])event.limbs;
        return new Body.Client(player, limbs);
    }
    public static Body.Server constructDefaultServerBody(ServerPlayer player){
        Limb.Server[] limbs = new Limb.Server[NATIVE_LIMB_COUNT];
        for (int i = 0; i < NATIVE_LIMB_COUNT; i++){
            limbs[i] = registered[i].getDefaultServerInstance(player);
        }
        BuildBodyEvent.Server event = ModLoader.postEventWithReturn(new BuildBodyEvent.Server(player, limbs));
        limbs = (Limb.Server[])event.limbs;
        return new Body.Server(player, limbs);
    }

    @SuppressWarnings("unchecked")
    public static abstract class BuildBodyEvent<P extends Player, L extends Limb<P>>
            extends Event
            implements IModBusEvent, Iterable<L>{
        protected BuildBodyEvent(P player, Limb<?>[] defaultLimbs){
            this.player = player;
            this.limbs = defaultLimbs;
            size = limbs.length;
        }
        private final P player;
        public P getPlayer(){
            return player;
        }
        protected Limb<?>[] limbs;
        private int size;
        public void addLimb(L limb){
            growLimbArray(1);
            limbs[size - 1] = limb;
        }
        public void growLimbArray(int count){
            int newSize = size + count;
            Limb<?>[] newArray = new Limb<?>[newSize];
            System.arraycopy(limbs, 0, newArray, 0, size);
            limbs = newArray;
            size = newSize;
        }
        public L get(int i){return (L)limbs[i];}
        public void set(int i, L limb){limbs[i] = limb;}

        public L getHead(){return (L)limbs[0];}
        public L getTorso(){return (L)limbs[1];}
        public L getLeftArm(){return (L)limbs[2];}
        public L getRightArm(){return (L)limbs[3];}
        public L getLeftLeg(){return (L)limbs[4];}
        public L getRightLeg(){return (L)limbs[5];}

        @Override public @NotNull Iterator<L> iterator() {return new LimbItr();}
        class LimbItr implements Iterator<L>{
            public LimbItr(){
                size = limbs.length;
                cursor = -1;
            }
            int size, cursor;
            @Override
            public boolean hasNext() {
                return ++cursor < size;
            }

            @Override
            public L next() {
                return (L)limbs[cursor];
            }
        }

        public static class Client extends BuildBodyEvent<AbstractClientPlayer, Limb.Client>{
            protected Client(AbstractClientPlayer player, Limb<?>[] defaultLimbs) {super(player, defaultLimbs);}
        }
        public static class Server extends BuildBodyEvent<ServerPlayer, Limb.Server>{
            protected Server(ServerPlayer player, Limb<?>[] defaultLimbs) {super(player, defaultLimbs);}
        }
    }

    public interface LimbDefaultInstanceSupplier{
        default Limb<?> getSidedDefaultInstance(boolean client, Player player){
            if (client){
                if (player instanceof AbstractClientPlayer cPlayer){
                    return getDefaultClientInstance(cPlayer);
                } else throw new IllegalArgumentException("Oops! Somehow received a non-AbstractClientPlayer object when invoking getSidedDefaultInstance with the client argument set to true!");
            } else if (player instanceof ServerPlayer sPlayer) {
                return getDefaultServerInstance(sPlayer);
            } else throw new IllegalArgumentException("Oops! Somehow received a non-ServerPlayer object when invoking getSidedDefaultInstance with the client argument set to false!");
        }
        Identifier getLimbID();
        Limb.Client getDefaultClientInstance(AbstractClientPlayer player);
        Limb.Server getDefaultServerInstance(ServerPlayer player);
    }
    public static class SimpleLimbSupplier implements LimbDefaultInstanceSupplier{
        final Function<AbstractClientPlayer, Limb.Client> clientSupplier;
        final Function<ServerPlayer, Limb.Server> serverSupplier;
        public final Identifier limbID;
        public SimpleLimbSupplier(Identifier limbID,
                Function<AbstractClientPlayer, Limb.Client> clientSupplier,
                Function<ServerPlayer, Limb.Server> serverSupplier){
            this.limbID = limbID;
            this.clientSupplier = clientSupplier; this.serverSupplier = serverSupplier;
        }

        @Override public Identifier getLimbID() {return limbID;}
        @Override public Limb.Client getDefaultClientInstance(AbstractClientPlayer player) {return clientSupplier.apply(player);}
        @Override public Limb.Server getDefaultServerInstance(ServerPlayer player) {return serverSupplier.apply(player);}
    }
    public static class RegisterLimbsEvent extends Event implements IModBusEvent {
        public LimbDefaultInstanceSupplier[] allRegisteredLimbSuppliers() {
            return allLimbSuppliers();
        }
        public void registerLimbSupplier(LimbDefaultInstanceSupplier supplier){
            LimbManager.register_INTERNAL(supplier);
        }
    }
    private static void registerNativeLimbs(LimbManager.RegisterLimbsEvent event){
        event.registerLimbSupplier(Head.SUPPLIER);
        event.registerLimbSupplier(Torso.SUPPLIER);
        event.registerLimbSupplier(Arm.LEFT_SUPPLIER);
        event.registerLimbSupplier(Arm.RIGHT_SUPPLIER);
        event.registerLimbSupplier(Leg.LEFT_SUPPLIER);
        event.registerLimbSupplier(Leg.RIGHT_SUPPLIER);
    }
}
