package mod.pilot.birch_n_bees.systems.extended_health;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jspecify.annotations.Nullable;

public abstract class Ailment {
    public final AilmentInstance.Serializer<? extends AilmentInstance<AbstractClientPlayer>> CLIENT_SERIALIZER;
    public final AilmentInstance.Serializer<? extends AilmentInstance<ServerPlayer>> SERVER_SERIALIZER;
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
    public AilmentInstance<?> readSidedInstance(IAttachmentHolder holder, RegistryFriendlyByteBuf buf,
                                                boolean client, @Nullable AilmentInstance<?> oldInstance){
        AilmentInstance<?> instance = constructDefaultSidedInstance(client);
        AilmentInstance.Serializer<?> serializer = client ? CLIENT_SERIALIZER : SERVER_SERIALIZER;
        return serializer.read(holder, instance, buf, oldInstance);
    }
    public final AilmentInstance<?> constructDefaultSidedInstance(boolean client){
        return client ? constructDefaultClientInstance() : constructDefaultServerInstance();
    }
    public abstract AilmentInstance.Client constructDefaultClientInstance();
    public abstract AilmentInstance.Server constructDefaultServerInstance();
}
