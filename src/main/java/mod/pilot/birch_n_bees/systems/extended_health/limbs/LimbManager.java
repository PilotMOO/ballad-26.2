package mod.pilot.birch_n_bees.systems.extended_health.limbs;


import mod.pilot.birch_n_bees.systems.extended_health.HealthToken;
import mod.pilot.birch_n_bees.systems.extended_health.limbs.default_limbs.Arm;
import mod.pilot.birch_n_bees.systems.extended_health.limbs.default_limbs.Head;
import mod.pilot.birch_n_bees.systems.extended_health.limbs.default_limbs.Leg;
import mod.pilot.birch_n_bees.systems.extended_health.limbs.default_limbs.Torso;
import mod.pilot.birch_n_bees.util.BirchTags;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

public class LimbManager {
    private static final int NATIVE_LIMB_COUNT = 6,
            NATIVE_ONLY_DAMAGES_COUNT = 4;
    public static void init(IEventBus bus){
        registered = new LimbDefaultInstanceSupplier[NATIVE_LIMB_COUNT];
        RegisterLimbsEvent event = new RegisterLimbsEvent();
        registerNativeLimbs(event);
        bus.post(event);

        onlyReferences = new OnlyDamagesReference[NATIVE_ONLY_DAMAGES_COUNT];
        bus.addListener(LimbManager::registerNativeOnlyDamageReferences);
        bus.post(new RegisterOnlyDamageTagReferencesEvent());
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

    private static OnlyDamagesReference[] onlyReferences;
    private static int onlyRefCount = 0;
    private static void register_INTERNAL(OnlyDamagesReference reference){
        if (onlyRefCount >= onlyReferences.length){
            OnlyDamagesReference[] newArray = new OnlyDamagesReference[onlyRefCount + 1];
            System.arraycopy(onlyReferences, 0, newArray, 0, onlyRefCount);
            onlyReferences = newArray;
        }
        onlyReferences[onlyRefCount++] = reference;
    }
    public static OnlyDamagesReference[] allOnlyReferences(){return onlyReferences;}
    public static boolean validateAgainstDamageOnly(DamageSource source, Identifier limbID){
        for (OnlyDamagesReference ref : onlyReferences){
            Identifier[] refID = ref.getIf(source);
            if (refID != null) {
                for (Identifier id : refID) {
                    if (id.equals(limbID)) return true;
                }
                return false;
            }
        }
        return true;
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
        return new Body.Client(limbs);
    }
    public static Body.Server constructDefaultServerBody(ServerPlayer player){
        Limb.Server[] limbs = new Limb.Server[NATIVE_LIMB_COUNT];
        for (int i = 0; i < NATIVE_LIMB_COUNT; i++){
            limbs[i] = registered[i].getDefaultServerInstance(player);
        }
        BuildBodyEvent.Server event = ModLoader.postEventWithReturn(new BuildBodyEvent.Server(player, limbs));
        limbs = (Limb.Server[])event.limbs;
        return new Body.Server(limbs);
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
        default Limb<?> getSidedEmptyInstance(boolean client){
            return client ? getEmptyClientInstance() : getEmptyServerInstance();
        }
        Identifier getLimbID();
        Limb.Client getDefaultClientInstance(AbstractClientPlayer player);
        Limb.Client getEmptyClientInstance();
        Limb.Server getDefaultServerInstance(ServerPlayer player);
        Limb.Server getEmptyServerInstance();
    }
    public static class SimpleLimbSupplier implements LimbDefaultInstanceSupplier{
        final Function<AbstractClientPlayer, Limb.Client> clientSupplier;
        final Supplier<Limb.Client> clientEmpty;
        final Function<ServerPlayer, Limb.Server> serverSupplier;
        final Supplier<Limb.Server> serverEmpty;
        public final Identifier limbID;
        public SimpleLimbSupplier(Identifier limbID,
                Function<AbstractClientPlayer, Limb.Client> clientSupplier,
                Supplier<Limb.Client> clientEmpty,
                Function<ServerPlayer, Limb.Server> serverSupplier,
                Supplier<Limb.Server> serverEmpty){
            this.limbID = limbID;
            this.clientSupplier = clientSupplier; this.serverSupplier = serverSupplier;
            this.clientEmpty = clientEmpty; this.serverEmpty = serverEmpty;
        }

        @Override public Identifier getLimbID() {return limbID;}
        @Override public Limb.Client getDefaultClientInstance(AbstractClientPlayer player) {return clientSupplier.apply(player);}
        @Override public Limb.Client getEmptyClientInstance() {return clientEmpty.get();}
        @Override public Limb.Server getDefaultServerInstance(ServerPlayer player) {return serverSupplier.apply(player);}
        @Override public Limb.Server getEmptyServerInstance() {return serverEmpty.get();}
    }
    public static class RegisterLimbsEvent extends Event implements IModBusEvent {
        public LimbDefaultInstanceSupplier[] allRegisteredLimbSuppliers() {return allLimbSuppliers();}
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

    public record OnlyDamagesReference(Supplier<TagKey<DamageType>> tag, Identifier[] id){
        public boolean is(DamageSource source){
            return source.is(tag.get());
        }
        public @Nullable Identifier[] getIf(DamageSource source){
            if (is(source)) return id;
            return null;
        }
    }
    public static class RegisterOnlyDamageTagReferencesEvent extends Event implements IModBusEvent{
        public OnlyDamagesReference[] allRegisteredOnlyReferences() { return allOnlyReferences(); }
        public void register(Supplier<TagKey<DamageType>> tag, Identifier... id){
            register_INTERNAL(new OnlyDamagesReference(tag, id));
        }
    }
    private static void registerNativeOnlyDamageReferences(LimbManager.RegisterOnlyDamageTagReferencesEvent event){
        event.register(() -> BirchTags.DamageTypes.ONLY_DAMAGES_HEAD, Head.HEAD);
        event.register(() -> BirchTags.DamageTypes.ONLY_DAMAGES_TORSO, Torso.TORSO);
        event.register(() -> BirchTags.DamageTypes.ONLY_DAMAGES_ARMS, Arm.LEFT_ARM, Arm.RIGHT_ARM);
        event.register(() -> BirchTags.DamageTypes.ONLY_DAMAGES_LEGS, Leg.LEFT_LEG, Leg.RIGHT_LEG);
    }

    public static void limbDamageHook(LivingDamageEvent.Post event){
        //This method is invoked within a different event listener that already validates that
        // the event's entity is a server player, so we can just cast
        ServerPlayer player =  (ServerPlayer) event.getEntity();

        HealthToken token = HealthToken.get(player);
        int limbCount = token.body.size();
        Limb.Server[] applicableLimbs = new Limb.Server[limbCount];

        float damage = event.getInflictedDamage();
        DamageSource dmgSource = event.getSource();
        double yaw, pitch;
        Vec3 sourcePos = dmgSource.getSourcePosition();
        if (sourcePos != null){
            double diffX = sourcePos.x - player.getX(),
                    diffZ = sourcePos.z - player.getZ();
            double horizontalDist = Math.sqrt((diffX * diffX) + (diffZ * diffZ));
            double sourceY = sourcePos.y;
            if (dmgSource.getDirectEntity() != null) sourceY += dmgSource.getDirectEntity().getBbHeight() / 2;
            double yDist = sourceY - (player.getY() + (player.getBbHeight() / 2));
            System.out.println("Player rot is " + player.getYRot());
            //for some reason entity rotations are really fucky so you need to add 90 degrees (???)
            yaw = Math.toRadians(player.getYRot() + 90) - Math.atan2(diffZ, diffX);
            pitch = Math.atan2(yDist, horizontalDist);
            if (yaw > Math.PI) yaw -= Math.TAU; //We want the rotations to be between -pi to pi (tau is 2 * pi)
        } else yaw = pitch = 0;
        int validLimbCount = 0;
        System.out.println("yaw is deg[" + Math.toDegrees(yaw) + "], pitch is deg[" + Math.toDegrees(pitch) + "]");
        for (Limb<?> limb : token.body.limbs){
            Limb.Server sLimb = (Limb.Server)limb;
            if (sLimb.isDamageApplicableToLimb(
                    player, damage, dmgSource, yaw, pitch, token)) applicableLimbs[validLimbCount++] = sLimb;
        }
        HealthToken.SyncLimb[] packets = new HealthToken.SyncLimb[validLimbCount];
        if (validLimbCount != 0){
            for (int i = 0; i < validLimbCount; i++) {
                Limb.Server sLimb = applicableLimbs[i];
                sLimb.hurt(player, damage, dmgSource, yaw, pitch, token);
                packets[i] = new HealthToken.SyncLimb(sLimb.ID);
            }
            player.syncData(HealthToken.ATTACHMENT);
            player.connection.sendBundled(packets);
        }
    }
}
