package mod.pilot.birch_n_bees.systems.extended_health.ailments;

import mod.pilot.birch_n_bees.systems.extended_health.HealthToken;
import mod.pilot.birch_n_bees.systems.extended_health.Serializer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jspecify.annotations.Nullable;

public abstract class Ailment {
    public final Serializer<AbstractClientPlayer, AilmentInstance<AbstractClientPlayer>> CLIENT_SERIALIZER;
    public final Serializer<ServerPlayer, AilmentInstance<ServerPlayer>> SERVER_SERIALIZER;
    public Ailment(Identifier ID){
        this.ID = ID;
        CLIENT_SERIALIZER = constructDefaultClientInstance().getSerializer();
        SERVER_SERIALIZER = constructDefaultServerInstance().getSerializer();
    }
    public final Identifier ID;

    public abstract AilmentInstance<?> buildInstance(boolean client, byte severity, int timeUntilCured);

    public abstract void onApplicationClient(AilmentInstance.Client instance, AbstractClientPlayer player, HealthToken token);
    public abstract void onApplicationServer(AilmentInstance.Server instance, ServerPlayer player, HealthToken token);
    public abstract void alieveSideEffectsClient(AilmentInstance.Client instance, AbstractClientPlayer player, HealthToken token);
    public abstract void alieveSideEffectsServer(AilmentInstance.Server instance, ServerPlayer player, HealthToken token);

    public AilmentInstance<?> deserializeSidedInstance(boolean client, String prepend, ValueInput input){
        AilmentInstance<?> instance = constructDefaultSidedInstance(client);
        instance.deserialize(prepend, input);
        return instance;
    }
    @SuppressWarnings("unchecked") //Daily reminder that I hate java generics
    public AilmentInstance<?> readSidedInstance(IAttachmentHolder holder, RegistryFriendlyByteBuf buf,
                                                boolean client, @Nullable AilmentInstance<?> oldInstance){
        AilmentInstance<?> instance = constructDefaultSidedInstance(client);
        if (client){
            return CLIENT_SERIALIZER.read(holder, (AilmentInstance<AbstractClientPlayer>) instance,
                    buf, (AilmentInstance<AbstractClientPlayer>) oldInstance);
        } else {
            return SERVER_SERIALIZER.read(holder, (AilmentInstance<ServerPlayer>) instance,
                    buf, (AilmentInstance<ServerPlayer>) oldInstance);
        }
    }
    public final AilmentInstance<?> constructDefaultSidedInstance(boolean client){
        return client ? constructDefaultClientInstance() : constructDefaultServerInstance();
    }
    public abstract AilmentInstance.Client constructDefaultClientInstance();
    public abstract AilmentInstance.Server constructDefaultServerInstance();
}
