package mod.pilot.birch_n_bees.systems.extended_health.limbs;

import mod.pilot.birch_n_bees.systems.extended_health.HealthToken;
import mod.pilot.birch_n_bees.systems.extended_health.IHealthTokenSerializable;
import mod.pilot.birch_n_bees.systems.extended_health.Serializer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract sealed class Limb<P extends Player> implements IHealthTokenSerializable<P, Limb<P>> permits Limb.Client, Limb.Server {
    public final Identifier ID;

    public float entityHealthScalar;
    public float maxHealth;
    public float damage;
    public final boolean clientSide;

    protected Limb(Identifier ID, P player, float healthPercent, boolean client){
        this.ID = ID;
        damage = 0;

        AttributeInstance playerHP = player.getAttribute(Attributes.MAX_HEALTH);
        if (playerHP != null) maxHealth = (float)(playerHP.getValue() * (entityHealthScalar = healthPercent));
        else {
            maxHealth = 1;
            entityHealthScalar = -1;
        }

        this.clientSide = client;
    }
    protected Limb(Identifier ID, boolean client){
        this.ID = ID;
        this.maxHealth = 1;
        this.entityHealthScalar = -1;
        this.damage = 0;
        this.clientSide = client;
    }
    public void updateHealth(float entityMaxHealth){
        float old = maxHealth;
        maxHealth = entityHealthScalar * entityMaxHealth;
        if (old > maxHealth && damage > old){
            float difference = old - maxHealth;
            damage -= difference * 0.1f;
        }
    }
    public void heal(float count){
        if (count > damage) damage = 0;
        else damage -= count;
    }

    public void onHeal(float lastHealAmount){}

    /**
     * Invoked for every applicable limb when the player takes damage.
     * Use the supplied arguments to decide if the limb should take damage
     * and apply additional effects dependent on the limb's health.
     * <p><b>NOTE! Client limbs should NOT modify the health! Health is managed on the server and synced
     * to the client, so any modifications made on the client will be OVERWRITTEN.</b></p>
     * Refer to {@link Limb.Client#onExternalClientUpdate(AbstractClientPlayer)} for making adjustments
     * on the client-side after a limb update, as this should be fired after the information has been synced.
     * @param player the Player object associated with this limb
     * @param amount the amount of damage dealt
     * @param source the Damage Source
     * @param relativeYaw the angle between the entity's facing direction and the direction of the damage along the X-Z plane.
     *                    A.K.A. the direction of the attack relative to the entity's facing direction along just the X-Z plane
     * @param relativePitch the angle between the entity's position and the damage's position in respect to the Y axis.
     * @param token the HealthToken associated with this limb
     */
    public abstract void hurt(P player, float amount, DamageSource source,
                              double relativeYaw, double relativePitch, HealthToken token);

    public float getEffectiveHealth(){
        return maxHealth - damage;
    }

    public static non-sealed class Client extends Limb<AbstractClientPlayer>{
        public Client(Identifier ID, AbstractClientPlayer player, float healthPercent) {
            super(ID, player, healthPercent, true);
        }
        public Client(Identifier ID){
            super(ID, true);
        }

        /**
         * Invoked on ONLY the client after modifications have been made (and synced) from the server.
         * @param player
         */
        public void onExternalClientUpdate(AbstractClientPlayer player){}

        @Override
        public Serializer<AbstractClientPlayer, Limb<AbstractClientPlayer>> getDefaultSerializer() {
            return DefaultSerializer.CLIENT_INSTANCE;
        }

        @Override public void hurt(AbstractClientPlayer player, float amount, DamageSource source, double relativeYaw, double relativePitch, HealthToken token) {}
    }
    public static non-sealed class Server extends Limb<ServerPlayer>{
        public Server(Identifier ID, ServerPlayer player, float healthPercent) {
            super(ID, player, healthPercent, false);
        }
        public Server(Identifier ID){
            super(ID, false);
        }

        @Override
        public void hurt(ServerPlayer player, float amount, DamageSource source,
                         double relativeYaw, double relativePitch, HealthToken token) {
            damage += modifyLimbDamage(player, amount, source, relativeYaw, relativePitch, token);
        }
        public float modifyLimbDamage(ServerPlayer player, float amount, DamageSource source,
                                      double relativeYaw, double relativePitch, HealthToken token){
            float delta = amount - getEffectiveHealth();
            if (delta < 0){
                return amount;
            } else {
                return getEffectiveHealth() + (delta * 0.1f);
            }
        }
        public boolean isDamageApplicableToLimb(ServerPlayer player, float amount, DamageSource source,
                                                double relativeYaw, double relativePitch, HealthToken token){
            return LimbManager.validateAgainstDamageOnly(source, ID);
        }

        @Override
        public Serializer<ServerPlayer, Limb<ServerPlayer>> getDefaultSerializer() {
            return DefaultSerializer.SERVER_INSTANCE;
        }
    }

    public static class DefaultSerializer<P extends Player> extends Serializer<P, Limb<P>> {
        /**Refer to {@link DefaultSerializer#CLIENT_INSTANCE} or {@link DefaultSerializer#SERVER_INSTANCE},
         * or implement your own custom serializer via the superclass {@link Serializer}*/
        private DefaultSerializer(){}
        public static DefaultSerializer<?> getSidedDefaultSerializer(boolean client){
            return client ? CLIENT_INSTANCE : SERVER_INSTANCE;
        }
        public static final DefaultSerializer<AbstractClientPlayer> CLIENT_INSTANCE = new DefaultSerializer<>();
        public static final DefaultSerializer<ServerPlayer> SERVER_INSTANCE = new DefaultSerializer<>();

        @Override
        public void serialize(Limb<P> instance, String prepend, @NonNull ValueOutput output) {
            output.putFloat(prepend + "_healthScalar", instance.entityHealthScalar);
            output.putFloat(prepend + "_health", instance.maxHealth);
            output.putFloat(prepend + "_damage", instance.damage);
        }
        @Override
        public void deserialize(Limb<P> instance, String prepend, @NonNull ValueInput input) {
            instance.entityHealthScalar = input.getFloatOr(prepend + "_healthScalar", instance.entityHealthScalar);
            instance.maxHealth = input.getFloatOr(prepend + "_health", instance.maxHealth);
            instance.damage = input.getFloatOr(prepend + "_damage", 0);
        }

        @Override
        public void write(Limb<P> instance, @NonNull RegistryFriendlyByteBuf buf) {
            buf.writeFloat(instance.entityHealthScalar);
            buf.writeFloat(instance.maxHealth);
            buf.writeFloat(instance.damage);
        }

        @Override
        public Limb<P> read(IAttachmentHolder holder, Limb<P> newInstance,
                                       @NonNull RegistryFriendlyByteBuf buf, @Nullable Limb<P> oldInstance) {
            newInstance.entityHealthScalar = buf.readFloat();
            newInstance.maxHealth = buf.readFloat();
            newInstance.damage = buf.readFloat();
            return newInstance;
        }
    }

    @Override
    public String toString() {
        return "Limb{" +
                "ID=" + ID.toString() +
                ", entityHealthScalar=" + entityHealthScalar +
                ", maxHealth=" + maxHealth +
                ", damage=" + damage +
                ", clientSide=" + clientSide +
                '}';
    }
}
