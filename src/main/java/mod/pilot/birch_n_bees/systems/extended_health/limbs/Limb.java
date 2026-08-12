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
    public final P player;
    public final Identifier ID;

    @Override
    public Identifier getIdentifier() {
        return ID;
    }

    public float entityHealthScalar;
    public float maxHealth;
    public float damage;
    public final boolean clientSide;

    @Override
    public boolean clientSide() {
        return clientSide;
    }

    protected Limb(Identifier ID, P player, float healthPercent, boolean client){
        this.player = player;
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
    public void updateHealth(float entityMaxHealth){
        float old = maxHealth;
        maxHealth = entityHealthScalar * entityMaxHealth;
        if (old > maxHealth && damage > old){
            float difference = old - maxHealth;
            damage -= difference * 0.1f;
        }
    }

    public abstract void hurt(float amount, DamageSource source, float relativeYaw, float relativePitch, HealthToken token);

    public float getEffectiveHealth(){
        return maxHealth - damage;
    }

    public static non-sealed class Client extends Limb<AbstractClientPlayer>{
        protected Client(Identifier ID, AbstractClientPlayer player, float healthPercent) {
            super(ID, player, healthPercent, true);
        }

        public boolean awaitServerSync = false;
        @Override
        public void hurt(float amount, DamageSource source, float relativeYaw, float relativePitch, HealthToken token) {
            awaitServerSync = true;
        }

        @Override
        public Serializer<AbstractClientPlayer, Limb<AbstractClientPlayer>> getSerializer() {
            return null;
        }

        @Override
        public Serializer<AbstractClientPlayer, Limb<AbstractClientPlayer>> getDefaultSerializer() {
            return DefaultSerializer.CLIENT_INSTANCE;
        }
    }
    public static non-sealed class Server extends Limb<ServerPlayer>{
        protected Server(Identifier ID, ServerPlayer player, float healthPercent) {
            super(ID, player, healthPercent, false);
        }

        @Override
        public void hurt(float amount, DamageSource source, float relativeYaw, float relativePitch, HealthToken token) {
            damage += modifyLimbDamage(amount, source, relativeYaw, relativePitch, token);
        }
        public float modifyLimbDamage(float amount, DamageSource source, float relativeYaw, float relativePitch, HealthToken token){
            float delta = amount - getEffectiveHealth();
            if (delta < 0){
                return amount;
            } else {
                return getEffectiveHealth() + (delta * 0.1f);
            }
        }

        @Override
        public Serializer<ServerPlayer, Limb<ServerPlayer>> getSerializer() {
            return null;
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
        public static final DefaultSerializer<AbstractClientPlayer> CLIENT_INSTANCE = new DefaultSerializer<>();
        public static final DefaultSerializer<ServerPlayer> SERVER_INSTANCE = new DefaultSerializer<>();

        @Override
        public String generatePrepend(int index) {
            return "limb" + index;
        }

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
}
