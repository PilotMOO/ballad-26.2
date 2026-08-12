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

/**
 * A given instance of an {@link Ailment} inflicted upon a player. Comparable to {@link net.minecraft.world.effect.MobEffectInstance}
 * <p>This class is {@code sealed}, defer to the permitted classes {@link Client} and {@link Server} if you wish to create unique instances</p>
 * @param <P> The related Player object this ailment manages. Due to this class being {@code sealed},
 *          there exists only two effective implementations of this class (and thus its generic),
 *          being {@link AbstractClientPlayer} via {@link Client},
 *          and {@link ServerPlayer} via {@link Server}
 */
public abstract sealed class AilmentInstance<P extends Player> permits AilmentInstance.Client, AilmentInstance.Server {
    protected AilmentInstance(Ailment parentType, boolean clientSide, byte severity, int timeUntilCured){
        this.type = parentType;
        this.clientSide = clientSide;
        this.severity = severity;
        this.timeUntilCured = timeUntilCured;
    }

    /**
     * What type of ailment this instance is of. Kept not final in case you wish to "mutate" what type of ailment this instance is,
     * for example if you are making a complex multi-stage ailment that can worsen without treatment.
     */
    public Ailment type;
    /**How long, in ticks, has this instance been active on a player.*/
    public int age;
    /**How extreme the ailment is, used to manage the strength of effects inflicted by the ailment.
     * <p>Comparable to {@code amplifier} in {@link net.minecraft.world.effect.MobEffectInstance}.</p>*/
    public byte severity;
    /**Is this instance client-side? Equivalent to {@code this instanceof AilmentInstance.Client},
     * Client-side instances do not manage the "cure" logic and exist solely for parity and for client-specific effects,
     * deferring to the server instance for incrementing cure progress.*/
    public final boolean clientSide;

    /**
     * How long, in ticks, until this ailment is cured.
     * Will ONLY progress if {@link AilmentInstance#canCureProgress(Player, HealthToken)} returns true.
     * <p>If set to {@code -1}, then the ailment has a trigger cure, where it immediately gets cured upon meeting that criteria</p>
     * <p>Note that client-side instances ALWAYS has this value set to -1, and curing can NOT be done on the client,
     * as that logic is managed on the server.</p>
     */
    public int timeUntilCured;

    /**
     * (If the ailment does not have a trigger cure)
     * Dictates whether {@link AilmentInstance#timeUntilCured} can progress during a given tick.
     * <p>Refer to {@link AilmentInstance#cureCriteria(Player, HealthToken)} if you wish to change the progress criteria,
     * as this method by default only confirms that this given instance does not use trigger cures.</p>
     * Is used exclusively in {@link AilmentInstance#incCure(Player, HealthToken)}.
     * @param player the player object this ailment is inflicted upon
     * @param token the wrapping {@link HealthToken} that contains this {@link AilmentInstance}
     *             and is attached to the supplied {@code player} argument
     * @return {@code true} if this ailment does NOT have a trigger cure AND the cure criteria is met. Otherwise {@code false}.
     */
    public boolean canCureProgress(P player, HealthToken token){
        return timeUntilCured != -1 && cureCriteria(player, token);
    }

    /**
     * Whether all required conditions are met for this ailment to get cured.
     * <p>If the AilmentInstance uses {@code Trigger Curing} (denoted by {@link AilmentInstance#timeUntilCured} equal to -1),
     * then upon this method returning {@code true}, the ailment will be immediately cured and removed.</p>
     * If the AilmentInstance does NOT use {@code Trigger Curing},
     * (denoted by {@link AilmentInstance#timeUntilCured} equalling any non-zero value)
     * then {@link AilmentInstance#timeUntilCured} will decrement every tick that this method returns true, until it hits 0;
     * upon hitting 0, this AilmentInstance will be cured.
     * @param player the player object this ailment is inflicted upon
     * @param token the wrapping {@link HealthToken} that contains this {@link AilmentInstance}
     *             and is attached to the supplied {@code player} argument
     * @return {@code true} if the required conditions for the cure to this ailment is met
     */
    public boolean cureCriteria(P player, HealthToken token){
        return true;
    }

    /**
     * Helper method to check relevant criteria before decrementing {@link AilmentInstance#timeUntilCured}.
     * Does NOTHING if this ailment uses {@code Trigger Curing}.
     * <p>returns {@code true} if this tick the ailment should be cured.</p>
     * @param player the player object this ailment is inflicted upon
     * @param token the wrapping {@link HealthToken} that contains this {@link AilmentInstance}
     *             and is attached to the supplied {@code player} argument
     * @return {@code true} if this tick the ailment should be cured
     */
    public boolean incCure(P player, HealthToken token){
        if (canCureProgress(player, token)) return --timeUntilCured <= 0;
        else return false;
    }

    /**
     * Handles the basic logic of the instance.
     * <p>For on-application and on-cure logic,
     * defer to {@link AilmentInstance#onApplication(Player, HealthToken)} and {@link AilmentInstance#cure(Player, HealthToken)}
     * and/or the methods they invoke in the parent {@link Ailment}.</p>
     * @param player the player object this ailment is inflicted upon
     * @param token the wrapping {@link HealthToken} that contains this {@link AilmentInstance}
     *             and is attached to the supplied {@code player} argument
     * @return {@code true} if the ailment was cured this tick.
     * Return value is used to manage removing the instance from the containing {@link HealthToken}
     */
    public abstract boolean tick(P player, HealthToken token);

    /**Handles effects caused when this ailment is first applied, like decreasing stats or setting up a flag elsewhere.
     * <p>Default implementations defer to either
     * {@link Ailment#onApplicationClient(Client, AbstractClientPlayer, HealthToken)}
     * or {@link Ailment#onApplicationServer(Server, ServerPlayer, HealthToken)}</p>
     * @param player the player object this ailment is inflicted upon
     * @param token the wrapping {@link HealthToken} that contains this {@link AilmentInstance}
     *             and is attached to the supplied {@code player} argument
     */
    public abstract void onApplication(P player, HealthToken token);
    /**Handles the removal of effects inflicted by {@link AilmentInstance#onApplication(Player, HealthToken)}
     * <p>Default implementations defer to either
     * {@link Ailment#alieveSideEffectsClient(Client, AbstractClientPlayer, HealthToken)}
     * or {@link Ailment#alieveSideEffectsServer(Server, ServerPlayer, HealthToken)}</p>
     * @param player the player object this ailment is inflicted upon
     * @param token the wrapping {@link HealthToken} that contains this {@link AilmentInstance}
     *             and is attached to the supplied {@code player} argument
     */
    public abstract void cure(P player, HealthToken token);

    /**Helper method to serialize this instance for saving to file*/
    public void serialize(String prepend, ValueOutput output){
        getSerializer().serialize(this, prepend, output);
    }
    /**Helper method to deserialize this instance for parsing from save file*/
    public void deserialize(String prepend, ValueInput input){
        getSerializer().deserialize(this, prepend, input);
    }

    /**Helper method to write the relevant data to the ByteBuf for sync handling
     * @param buf the {@link RegistryFriendlyByteBuf} to write data to*/
    public void write(RegistryFriendlyByteBuf buf){
        getSerializer().write(this, buf);
    }

    /**
     * Get the {@link Serializer} compatible for this instance.
     * <p>This method is {@code final}, defer to {@link AilmentInstance#getComplexSerializer()} to implement a custom serializer</p>
     * @return the {@link Serializer} to save this instance to file or write data to sync
     */
    public final Serializer<AilmentInstance<P>> getSerializer(){
        if (hasComplexData()) return getComplexSerializer();
        else return getSidedSerializer();
    }

    /**
     * Returns the associated {@link AilmentInstance.SimpleSerializer} for this instance--
     * either {@link SimpleSerializer#CLIENT_INSTANCE} for {@link AilmentInstance.Client},
     * or {@link SimpleSerializer#SERVER_INSTANCE} for {@link AilmentInstance.Server}.
     * <p>All accessible implementations of this method are {@code final},
     * defer to {@link AilmentInstance#getComplexSerializer()} to implement a custom serializer.</p>
     * @return the associated {@link AilmentInstance.SimpleSerializer} for this instance
     */
    protected abstract SimpleSerializer<P> getSidedSerializer();

    /**If this given AilmentInstance type requires a custom serializer. Default {@code false}
     * @return if this given AilmentInstance type requires a custom serializer*/
    public boolean hasComplexData(){return false;}
    /**Retrieves the custom {@link Serializer} for this instance.
     * <b>This method returns a null value by default, despite the {@code @NonNull} annotation.</b>
     * Validate if this instance has a custom serializer via {@link AilmentInstance#hasComplexData()}
     * <p>For instances that require a custom serializer due to implementing and saving additional data,
     * override this method to return your custom {@link Serializer}.
     * <b>Make sure to override {@link AilmentInstance#hasComplexData()} to return {@code true},
     * otherwise your serializer will not be used</b></p>
     * @return the custom serializer for this instance, {@code null} for default implementations
     */
    public @NonNull Serializer<AilmentInstance<P>> getComplexSerializer(){return null;}

    /**The client-side implementation of {@link AilmentInstance}, for use with {@link AbstractClientPlayer} objects.
     * <p>Client-side instances <b>should not manage cure logic, and only apply client-side effects</b>
     * to prevent desync issues.</p>
     * <p>Client-side instances ALWAYS has their {@link AilmentInstance#timeUntilCured} set to -1, as syncing
     * this contextual value between clients and servers is unrealistic.</p>
     * Some implementations of this class may be duds, and don't implement any actual logic themselves.
     */
    public static non-sealed class Client extends AilmentInstance<AbstractClientPlayer>{
        public Client(Ailment parentType, byte severity) {
            super(parentType, true, severity, -1);
        }

        @Override
        public boolean tick(AbstractClientPlayer player, HealthToken token){
            if (age++ == 0) onApplication(player, token);
            return false;
        }

        @Override
        public void onApplication(AbstractClientPlayer player, HealthToken token) {
            type.onApplicationClient(this, player, token);
        }
        @Override
        public void cure(AbstractClientPlayer player, HealthToken token) {
            type.alieveSideEffectsClient(this, player, token);
        }

        @Override
        protected final SimpleSerializer<AbstractClientPlayer> getSidedSerializer() {
            return SimpleSerializer.CLIENT_INSTANCE;
        }
    }

    /**
     * The server-side implementation of {@link AilmentInstance}, for use with {@link ServerPlayer} objects.
     * <p>Server-side instances manage all cure logic,
     * and sends relevant {@link HealthToken.RequestClientCure} packets to the client when all cure criteria is met</p>
     */
    public static non-sealed class Server extends AilmentInstance<ServerPlayer>{
        public Server(Ailment parentType, byte severity, int timeUntilCured) {
            super(parentType, false, severity, timeUntilCured);
        }

        @Override
        public boolean tick(ServerPlayer player, HealthToken token) {
            if (age++ == 0) onApplication(player, token);
            if ((timeUntilCured == -1 && cureCriteria(player, token)) || incCure(player, token)){
                cure(player, token);
                return true;
            }
            return false;
        }

        @Override
        public void onApplication(ServerPlayer player, HealthToken token) {
            type.onApplicationServer(this, player, token);
        }

        @Override
        public void cure(ServerPlayer player, HealthToken token) {
            type.alieveSideEffectsServer(this, player, token);
            requestClientCure(player);
        }
        @Override
        protected SimpleSerializer<ServerPlayer> getSidedSerializer() {
            return SimpleSerializer.SERVER_INSTANCE;
        }

        /**
         * Helper method to send a request to cure this given ailment on all associated clients.
         * @param player the {@link ServerPlayer} to cure the ailment on other clients
         */
        public void requestClientCure(ServerPlayer player){
            player.connection.send(new HealthToken.RequestClientCure(this.type.ID));
        }
    }

    /**
     * A baseclass for serializers that handle the serialization of {@link AilmentInstance}s.
     * <p>Refer to {@link SimpleSerializer} if your instance does not use custom data.</p>
     * @param <A> the type of {@link AilmentInstance} this serializer handles
     */
    public static abstract class Serializer<A extends AilmentInstance<?>>{
        /**
         * Writes all the relevant data of the first argument to the supplied {@link ValueOutput} argument for serialization.
         * <p><b>When implementing this method, make sure to concatenate the 2nd string argument to the FRONT
         * of each of the string IDs when putting data.</b>
         * Refer to the default implementation in {@link SimpleSerializer} for reference.</p>
         * @param instance the {@link AilmentInstance} to get the data from
         * @param prepend the unique string I.D. to prepend to each of the data string arguments when assigning data to the {@link ValueOutput}
         * @param output the {@link ValueOutput} to write data to
         */
        public abstract void serialize(A instance, String prepend, @NonNull ValueOutput output);

        /**
         * Reads all relevant data of the {@link ValueInput} and assigns the data to the default {@link AilmentInstance} object for deserialization.
         * <p>Make sure to prepend each string ID with the supplied string prepend in order to access the correct data.
         * Refer to your custom implementation of {@link Serializer#serialize(AilmentInstance, String, ValueOutput)}
         * or the default implementation in {@link SimpleSerializer} for reference.</p>
         * @param instance the {@link AilmentInstance} to assign the data to
         * @param prepend the unique string I.D. to prepend to each of the data string arguments when reading data from the {@link ValueInput}
         * @param input the {@link ValueInput} to read data from
         */
        public abstract void deserialize(A instance, String prepend, @NonNull ValueInput input);

        /**
         * Writes all the relevant data of the first argument to the supplied {@link RegistryFriendlyByteBuf} for syncing.
         * @param instance the {@link AilmentInstance} to get the relevant data
         * @param buf the {@link RegistryFriendlyByteBuf} to write data to
         */
        public abstract void write(A instance, @NonNull RegistryFriendlyByteBuf buf);

        /**
         * Reads all relevant data of the {@link RegistryFriendlyByteBuf} and assigns the data to the default {@link AilmentInstance} object for syncing.
         * <p>Because java generics kind of suck, you may have to do an unchecked cast to return the correct generic
         * if you don't create your own custom subclasses of {@link Client} and {@link Server}</p>
         * @param holder the {@link IAttachmentHolder} the AilmentInstance belongs to
         * @param newInstance the {@link AilmentInstance} to assign the data to
         * @param buf the {@link RegistryFriendlyByteBuf} to get the data from
         * @param oldInstance an optional {@link AilmentInstance} that refers to the old value that will be replaced with the new value.
         *                    <b>May be null, but can be used to preserve information that would otherwise not be synced
         *                    or lost when syncing</b>
         * @return the 2nd {@link AilmentInstance} argument with the synced data now applied
         */
        public abstract AilmentInstance<?> read(IAttachmentHolder holder, AilmentInstance<?> newInstance, @NonNull RegistryFriendlyByteBuf buf,
                               @Nullable AilmentInstance<?> oldInstance);
    }

    /**
     * A simple, default implementation of {@link Serializer} for handling {@link AilmentInstance}s
     * that do not contain any additional data in need of syncing/serializing
     * @param <P> the relevant {@link Player} object the {@link AilmentInstance} objects this serializer handles manages;
     *           either {@link AbstractClientPlayer} for {@link Client} or {@link ServerPlayer} for {@link Server}
     */
    public static class SimpleSerializer<P extends Player> extends Serializer<AilmentInstance<P>>{
        /**Refer to {@link SimpleSerializer#CLIENT_INSTANCE} or {@link SimpleSerializer#SERVER_INSTANCE},
         * or implement your own custom serializer via the superclass {@link Serializer}*/
        private SimpleSerializer(){}
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

            int buf_cureTime = buf.readInt();
            if (newInstance.clientSide) newInstance.timeUntilCured = -1;
            else if (buf_cureTime == -1){
                if (oldInstance != null) newInstance.timeUntilCured = oldInstance.timeUntilCured;
            } else newInstance.timeUntilCured = buf_cureTime;

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
