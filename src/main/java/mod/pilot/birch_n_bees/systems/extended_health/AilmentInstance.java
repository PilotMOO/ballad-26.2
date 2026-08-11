package mod.pilot.birch_n_bees.systems.extended_health;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract sealed class AilmentInstance<P extends Player> permits AilmentInstance.Client, AilmentInstance.Server {
    private AilmentInstance(Ailment parentType, boolean clientSide, byte severity, int timeUntilCured){
        this.type = parentType;
        this.clientSide = clientSide;
        this.severity = severity;
        this.timeUntilCured = timeUntilCured;
    }
    public Ailment type;
    public int age;
    public byte severity;
    public final boolean clientSide;

    /**
     * How long, in ticks, until this ailment is cured.
     * Will ONLY progress if {@link AilmentInstance#canCureProgress(Player, HealthToken)} returns true.
     * <p>If set to {@code -1}, then the ailment has a trigger cure, where it immediately gets cured upon meeting that criteria</p>
     * <p>Note that client-side instances ALWAYS has this value set to -1, as the logic is managed on the server</p>
     */
    public int timeUntilCured;
    public boolean canCureProgress(P player, HealthToken token){
        return timeUntilCured != -1 && cureCriteria(player, token);
    }
    public boolean cureCriteria(P player, HealthToken token){
        return true;
    }
    public boolean incCure(P player, HealthToken token){
        if (canCureProgress(player, token)) return --timeUntilCured <= 0;
        else return false;
    }

    public boolean tick(P player, HealthToken token){
        age++;
        if ((timeUntilCured == -1 && cureCriteria(player, token)) || incCure(player, token)){
            cure(player, token);
            return true;
        }
        return false;
    }

    public abstract void cure(P player, HealthToken token);

    public void serialize(String prepend, ValueOutput output){
        getSerializer().serialize(this, prepend, output);
    }
    public void deserialize(String prepend, ValueInput input){
        getSerializer().deserialize(this, prepend, input);
    }
    public void write(RegistryFriendlyByteBuf buf){
        getSerializer().write(this, buf);
    }

    public final Serializer<AilmentInstance<P>> getSerializer(){
        if (hasComplexData()) return getComplexSerializer();
        else return getSidedSerializer();
    }
    protected abstract SimpleSerializer<P> getSidedSerializer();
    public boolean hasComplexData(){return false;}
    public @NonNull Serializer<AilmentInstance<P>> getComplexSerializer(){return null;}

    public static non-sealed class Client extends AilmentInstance<AbstractClientPlayer>{
        public Client(Ailment parentType, byte severity) {
            super(parentType, true, severity, -1);
        }

        @Override
        public void cure(AbstractClientPlayer player, HealthToken token) {
            type.alieveSideEffectsClient(player);
        }

        @Override
        protected final SimpleSerializer<AbstractClientPlayer> getSidedSerializer() {
            return SimpleSerializer.CLIENT_INSTANCE;
        }
    }
    public static non-sealed class Server extends AilmentInstance<ServerPlayer>{
        public Server(Ailment parentType, byte severity, int timeUntilCured) {
            super(parentType, false, severity, timeUntilCured);
        }

        @Override
        public void cure(ServerPlayer player, HealthToken token) {
            type.alieveSideEffectsServer(player);
        }
        @Override
        protected SimpleSerializer<ServerPlayer> getSidedSerializer() {
            return SimpleSerializer.SERVER_INSTANCE;
        }
    }

    public static abstract class Serializer<A extends AilmentInstance<?>>{
        public abstract void serialize(A instance, String prepend, @NonNull ValueOutput output);
        public abstract void deserialize(A instance, String prepend, @NonNull ValueInput input);
        public abstract void write(A instance, @NonNull RegistryFriendlyByteBuf buf);
        public abstract AilmentInstance<?> read(IAttachmentHolder holder, AilmentInstance<?> newInstance, @NonNull RegistryFriendlyByteBuf buf,
                               @Nullable AilmentInstance<?> oldInstance);
    }
    public static class SimpleSerializer<P extends Player> extends Serializer<AilmentInstance<P>>{
        public static final SimpleSerializer<AbstractClientPlayer> CLIENT_INSTANCE = new SimpleSerializer<>();
        public static final SimpleSerializer<ServerPlayer> SERVER_INSTANCE = new SimpleSerializer<>();

        @Override
        public void serialize(AilmentInstance<P> instance, String prepend, @NonNull ValueOutput output) {
            output.putInt(prepend + "_age", instance.age);
            output.putByte(prepend + "_severity", instance.severity);
            output.putInt(prepend + "_cureTime", instance.timeUntilCured);
        }
        @Override
        public void deserialize(AilmentInstance<P> instance, String prepend, @NonNull ValueInput input) {
            instance.age = input.getIntOr(prepend + "_age", 0);
            instance.severity = input.getByteOr(prepend + "_severity", (byte)0);
            instance.timeUntilCured = input.getIntOr(prepend + "_cureTime", 0);
        }

        @Override
        public void write(AilmentInstance<P> instance, @NonNull RegistryFriendlyByteBuf buf) {
            buf.writeInt(instance.age);
            buf.writeByte(instance.severity);
            buf.writeInt(instance.timeUntilCured);
        }

        @Override
        @SuppressWarnings("unchecked") //God I hate Java generics
        public AilmentInstance<P> read(IAttachmentHolder holder, AilmentInstance<?> newInstance, @NonNull RegistryFriendlyByteBuf buf, @Nullable AilmentInstance<?> oldInstance) {
            newInstance.age = buf.readInt();
            newInstance.severity = buf.readByte();
            newInstance.timeUntilCured = buf.readInt();
            return (AilmentInstance<P>) newInstance;
        }
    }

    @Override
    public String toString() {
        return "AilmentInstance{" +
                "type=" + type +
                ", age=" + age +
                ", severity=" + severity +
                ", clientSide=" + clientSide +
                ", timeUntilCured=" + timeUntilCured +
                '}';
    }
}
