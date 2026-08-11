package mod.pilot.birch_n_bees.systems.extended_health;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jspecify.annotations.Nullable;

public abstract class Ailment {
    private final AilmentInstance.Serializer<? extends AilmentInstance<?>> SERIALIZER;
    public Ailment(Identifier ID){
        this.ID = ID;
        SERIALIZER = constructDefaultSidedInstance(AilmentManager.isClientSide()).getSerializer();
    }
    public final Identifier ID;

    public abstract AilmentInstance<?> buildInstance(boolean client, byte severity, int timeUntilCured);

    public abstract void onApplicationClient(AbstractClientPlayer player);
    public abstract void onApplicationServer(ServerPlayer player);
    public abstract void alieveSideEffectsClient(AbstractClientPlayer player);
    public abstract void alieveSideEffectsServer(ServerPlayer player);

    public AilmentInstance<?> deserializeSidedInstance(boolean client, String prepend, ValueInput input){
        AilmentInstance<?> instance = constructDefaultSidedInstance(client);
        instance.deserialize(prepend, input);
        return instance;
    }
    public AilmentInstance<?> readSidedInstance(IAttachmentHolder holder, RegistryFriendlyByteBuf buf,
                                                @Nullable AilmentInstance<?> oldInstance){
        return SERIALIZER.read(holder, constructDefaultSidedInstance(), buf, oldInstance);
    }
    public final AilmentInstance<?> constructDefaultSidedInstance(){
        return constructDefaultSidedInstance(AilmentManager.isClientSide());
    }
    public final AilmentInstance<?> constructDefaultSidedInstance(boolean client){
        return client ? constructDefaultClientInstance() : constructDefaultServerInstance();
    }
    public abstract AilmentInstance.Client constructDefaultClientInstance();
    public abstract AilmentInstance.Server constructDefaultServerInstance();
}
