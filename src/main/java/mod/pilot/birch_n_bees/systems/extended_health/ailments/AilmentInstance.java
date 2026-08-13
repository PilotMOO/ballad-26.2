package mod.pilot.birch_n_bees.systems.extended_health.ailments;

import mod.pilot.birch_n_bees.systems.extended_health.HealthToken;
import mod.pilot.birch_n_bees.systems.extended_health.IHealthTokenSerializable;
import mod.pilot.birch_n_bees.systems.extended_health.Serializer;
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
public abstract sealed class AilmentInstance<P extends Player> implements IHealthTokenSerializable<P, AilmentInstance<P>>
        permits AilmentInstance.Client, AilmentInstance.Server {
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

    /**
     * Returns the associated {@link DefaultSerializer} for this instance--
     * either {@link DefaultSerializer#CLIENT_INSTANCE} for {@link AilmentInstance.Client},
     * or {@link DefaultSerializer#SERVER_INSTANCE} for {@link AilmentInstance.Server}.
     * <p>All accessible implementations of this method are {@code final},
     * defer to {@link AilmentInstance#getComplexSerializer()} to implement a custom serializer.</p>
     * @return the associated {@link DefaultSerializer} for this instance
     */
    @Override public abstract DefaultSerializer<P> getDefaultSerializer();

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
        public final DefaultSerializer<AbstractClientPlayer> getDefaultSerializer() {
            return DefaultSerializer.CLIENT_INSTANCE;
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
        public DefaultSerializer<ServerPlayer> getDefaultSerializer() {
            return DefaultSerializer.SERVER_INSTANCE;
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
     * A simple, default implementation of {@link Serializer} for handling {@link AilmentInstance}s
     * that do not contain any additional data in need of syncing/serializing
     * @param <P> the relevant {@link Player} object the {@link AilmentInstance} objects this serializer handles manages;
     *           either {@link AbstractClientPlayer} for {@link Client} or {@link ServerPlayer} for {@link Server}
     */
    public static class DefaultSerializer<P extends Player> extends Serializer<P, AilmentInstance<P>> {
        /**Refer to {@link DefaultSerializer#CLIENT_INSTANCE} or {@link DefaultSerializer#SERVER_INSTANCE},
         * or implement your own custom serializer via the superclass {@link Serializer}*/
        private DefaultSerializer(){}
        public static final DefaultSerializer<AbstractClientPlayer> CLIENT_INSTANCE = new DefaultSerializer<>();
        public static final DefaultSerializer<ServerPlayer> SERVER_INSTANCE = new DefaultSerializer<>();

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
        public AilmentInstance<P> read(IAttachmentHolder holder, AilmentInstance<P> newInstance,
                                       @NonNull RegistryFriendlyByteBuf buf, @Nullable AilmentInstance<P> oldInstance) {
            newInstance.age = buf.readInt();
            newInstance.severity = buf.readByte();

            int buf_cureTime = buf.readInt();
            if (newInstance.clientSide) newInstance.timeUntilCured = -1;
            else if (buf_cureTime == -1){
                if (oldInstance != null) newInstance.timeUntilCured = oldInstance.timeUntilCured;
            } else newInstance.timeUntilCured = buf_cureTime;

            return newInstance;
        }
    }

    @Override
    public String toString() {
        return "AilmentInstance{" +
                "type=" + type.ID +
                ", age=" + age +
                ", severity=" + severity +
                ", clientSide=" + clientSide +
                ", timeUntilCured=" + timeUntilCured +
                '}';
    }
}
