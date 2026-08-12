package mod.pilot.birch_n_bees.systems.extended_health;

import io.netty.buffer.ByteBuf;
import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.DynamicInventoryToken;
import mod.pilot.birch_n_bees.util.BirchAttachmentTypes;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.rmi.UnexpectedException;
import java.util.ArrayList;

public class HealthToken implements ValueIOSerializable {
    public static AttachmentType<HealthToken> ATTACHMENT;
    public static HealthToken get(Player player){
        if (ATTACHMENT == null) ATTACHMENT = BirchAttachmentTypes.HEALTH_TOKEN_ATTACHMENT.get();
        return player.getData(ATTACHMENT);
    }
    public static boolean has(Player player){
        if (ATTACHMENT == null) ATTACHMENT = BirchAttachmentTypes.HEALTH_TOKEN_ATTACHMENT.get();
        return player.hasData(ATTACHMENT);
    }

    public HealthToken(){this(0);}
    public HealthToken(int size){
        this(new ArrayList<>(size));
    }
    public HealthToken(ArrayList<AilmentInstance<?>> ailments){
        instances = ailments;
    }

    public ArrayList<AilmentInstance<?>> instances;
    public @Nullable AilmentInstance<?> getAilment(Ailment ailment){
        for (AilmentInstance<?> instance : instances){
            if (instance.type.equals(ailment)) return instance;
        }
        return null;
    }
    public void tickInstanceClient(AbstractClientPlayer player){
        int size = instances.size();
        boolean[] toRemove = new boolean[size];
        boolean check = false;
        for (int i = 0; i < size; i++) {
            AilmentInstance<?> instance = instances.get(i);
            if (instance instanceof AilmentInstance.Client clientInstance) {
                check |= toRemove[i] = clientInstance.tick(player, this);
            }
        }
        if (check){
            int index = 0, cursor = 0;
            while (cursor < size){
                if (toRemove[index]){
                    instances.remove(cursor);
                    size--;
                } else cursor++;
                index++;
            }
        }
    }
    public void tickInstanceServer(ServerPlayer player){
        int size = instances.size();
        boolean[] toRemove = new boolean[size];
        boolean check = false;
        for (int i = 0; i < size; i++) {
            AilmentInstance<?> instance = instances.get(i);
            if (instance instanceof AilmentInstance.Server serverInstance) {
                check |= toRemove[i] = serverInstance.tick(player, this);
            }
        }
        if (check){
            int index = 0, cursor = 0;
            while (cursor < size){
                if (toRemove[index]){
                    instances.remove(cursor);
                    size--;
                } else cursor++;
                index++;
            }
        }
    }

    public void add(AilmentInstance<?> ailmentInstance){
        /*boolean client = AilmentManager.isClientSide();
        if (ailmentInstance instanceof AilmentInstance.Server && client) {
            throw new RuntimeException("Oops! found a Server-side ailment instance on the logical client! That shouldn't have happened... Instance is: " + ailmentInstance.type.ID);
        } else if (ailmentInstance instanceof AilmentInstance.Client && !client){
            throw new RuntimeException("Oops! found a Client-side ailment instance on the logical server! That shouldn't have happened... Instance is: " + ailmentInstance.type.ID);
        }*/
        instances.add(ailmentInstance);
    }
    public void remove(Identifier id){
        Ailment ailment = AilmentManager.byIdentifier(id);
        if (ailment != null) remove(ailment);
    }
    public void remove(Ailment ailment){
        int size = instances.size();
        for (int i = 0; i < size; i++){
            if (instances.get(i).type.equals(ailment)){
                instances.remove(i);
                return;
            }
        }
    }
    public void remove(AilmentInstance<?> ailmentInstance){
        instances.remove(ailmentInstance);
    }

    @Override
    public void serialize(@NonNull ValueOutput output) {
        int size = instances.size();
        output.putInt("ailmentCount", size);
        for (int i = 0; i < size; i++){
            AilmentInstance<?> ailment = instances.get(i);
            String prepend = "ailment" + i;
            output.putString(prepend + "_ID", ailment.type.ID.toString());
            output.putBoolean(prepend + "_client", ailment.clientSide);
            ailment.serialize(prepend, output);
        }
    }
    @Override
    public void deserialize(@NonNull ValueInput input) {
        input.getInt("ailmentCount").ifPresent((size) -> {
            instances = new ArrayList<>(size);
            for (int i = 0; i < size; i++){
                String prepend = "ailment" + i;
                String stringID = input.getStringOr(prepend + "_ID", "");
                if (stringID.isEmpty()) continue;
                Identifier ident = Identifier.parse(stringID);
                Ailment ailment = AilmentManager.byIdentifier(ident);
                boolean client = input.getBooleanOr(prepend + "_client", false);
                if (ailment == null) continue;
                AilmentInstance<?> instance = ailment.deserializeSidedInstance(client, prepend, input);
                instances.set(i, instance);
            }
        });
    }
    public static class Syncer implements AttachmentSyncHandler<HealthToken> {
        @Override
        public void write(@NonNull RegistryFriendlyByteBuf buf, @NonNull HealthToken attachment, boolean initialSync) {
            int size = attachment.instances.size();
            buf.writeInt(size);
            for (int i = 0; i < size; i++){
                AilmentInstance<?> ailmentInstance = attachment.instances.get(i);
                buf.writeUtf(ailmentInstance.type.ID.toString());
                ailmentInstance.write(buf);
            }
        }
        @Override
        public @Nullable HealthToken read(@NonNull IAttachmentHolder holder, RegistryFriendlyByteBuf buf,
                                                    @Nullable HealthToken previousValue) {
            int size = buf.readInt();
            System.out.println("Size is " + size);
            if (size == 0) return new HealthToken();
            ArrayList<AilmentInstance<?>> instances = new ArrayList<>(size);
            for (int i = 0; i < size; i++){
                System.out.println("Yummers, we are on cycle " + i + ", size is " + size);
                String stringID = buf.readUtf();
                Identifier ident = Identifier.parse(stringID);
                Ailment ailment = AilmentManager.byIdentifier(ident);
                System.out.println("identifier is " + ident);
                if (ailment != null){
                    AilmentInstance<?> oldInstance = previousValue != null ? previousValue.getAilment(ailment) : null;
                    boolean client;
                    if (oldInstance != null) client = oldInstance.clientSide;
                    else if (holder instanceof Entity e) client = e.level().isClientSide();
                    else throw new RuntimeException("Oops! Couldn't figure out dist context from supplied arguments when attempting to read a HealthToken from a sync. HealthTokens are only compatible with Players, make sure you aren't putting them on anything else!");
                    System.out.println("Ailment wasn't null! Context says that this is " + (client ? "client side" : "server side"));
                    System.out.println("cycle is " + i + ", instances has size " + instances.size());
                    //Why the FUCK does ArrayList.set(int, E) throw an error even if the index would be in bounds of the
                    // array as defined by the initialCapacity argument??? Why can't I use the FUCKING ARRAY in my FUCKING ARRAYLIST
                    instances.add(ailment.readSidedInstance(holder, buf, client, oldInstance));
                }
            }
            return new HealthToken(instances);
        }
    }
    public record RequestClientCure(Identifier ailmentID) implements CustomPacketPayload {
        public RequestClientCure(String id) {
            this(Identifier.parse(id));
        }
        public static StreamCodec<ByteBuf, RequestClientCure> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8,
                        (request) -> request.ailmentID.toString(),
                        RequestClientCure::new
                );
        public static final Type<RequestClientCure> PACKET_TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "cure_request"));
        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return PACKET_TYPE;
        }

        public static void handle(RequestClientCure request, final IPayloadContext context){
            context.enqueueWork(() -> {
                HealthToken token = get(context.player());
                AilmentInstance.Client instance = (AilmentInstance.Client)token.getAilment(
                        AilmentManager.byIdentifier(request.ailmentID()));
                if (instance != null){
                    if (context.player() instanceof AbstractClientPlayer player) instance.cure(player, token);
                    token.remove(instance);
                }
            });
        }
    }

    @Override
    public String toString() {
        return "HealthToken{" +
                "instances=" + instances +
                '}';
    }
}
