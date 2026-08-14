package mod.pilot.birch_n_bees.systems.extended_health;

import io.netty.buffer.ByteBuf;
import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.systems.extended_health.ailments.Ailment;
import mod.pilot.birch_n_bees.systems.extended_health.ailments.AilmentInstance;
import mod.pilot.birch_n_bees.systems.extended_health.ailments.AilmentManager;
import mod.pilot.birch_n_bees.systems.extended_health.limbs.Body;
import mod.pilot.birch_n_bees.systems.extended_health.limbs.Limb;
import mod.pilot.birch_n_bees.systems.extended_health.limbs.LimbManager;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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


    public HealthToken(@Nullable Player player){
        this(0, LimbManager.constructDefaultSidedBody(player));
    }
    public HealthToken(int size, Body<?> body){
        this(new ArrayList<>(size), body);
    }
    public HealthToken(ArrayList<AilmentInstance<?>> ailments, Body<?> body){
        this.instances = ailments;
        this.body = body;
    }

    public ArrayList<AilmentInstance<?>> instances;
    public @Nullable AilmentInstance<?> getAilment(Ailment ailment){
        for (AilmentInstance<?> instance : instances){
            if (instance.type.equals(ailment)) return instance;
        }
        return null;
    }
    public void addAilment(AilmentInstance<?> ailmentInstance){instances.add(ailmentInstance);}
    public void removeAilmentByID(Identifier id){
        Ailment ailment = AilmentManager.byIdentifier(id);
        if (ailment != null) removeAilment(ailment);
    }
    public void removeAilment(Ailment ailment){
        int size = instances.size();
        for (int i = 0; i < size; i++){
            if (instances.get(i).type.equals(ailment)){
                instances.remove(i);
                return;
            }
        }
    }
    public void removeAilment(AilmentInstance<?> ailmentInstance){
        instances.remove(ailmentInstance);
    }

    public void tickAilmentInstanceClient(AbstractClientPlayer player){
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
    public void tickAilmentInstanceServer(ServerPlayer player){
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

    public Body<?> body;
    public @Nullable Limb<?> getLimb(Identifier id){
        return body.getLimbByID(id);
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
        int limbCount = body.size();
        output.putInt("limbCount", limbCount);
        for (int i = 0;  i < limbCount; i++){
            Limb<?> limb = body.limbs[i];
            String prepend = "limb" + i;
            output.putString(prepend + "_ID", limb.ID.toString());
            output.putBoolean(prepend + "_client", limb.clientSide);
            limb.serialize(prepend, output);
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
                instances.add(instance);
            }
        });
        input.getInt("limbCount").ifPresent((size) -> {
            for (int i = 0; i < size; i++){
                String prepend = "limb" + i;
                String stringID = input.getStringOr(prepend + "_ID", "");
                if (stringID.isEmpty()) continue;
                Identifier ident = Identifier.parse(stringID);
                LimbManager.LimbDefaultInstanceSupplier supplier = LimbManager.byIdentifier(ident);
                if (supplier == null) continue;
                boolean client = input.getBooleanOr(prepend + "_client", false);
                Limb<?> limb = supplier.getSidedEmptyInstance(client);
                limb.deserialize(prepend, input);
                body.unsafeSet(limb, i); //I am once again reminded why java generics killed the dinosaurs
            }
        });

    }
    public static class Syncer implements AttachmentSyncHandler<HealthToken> {
        @Override
        public void write(@NonNull RegistryFriendlyByteBuf buf, @NonNull HealthToken attachment, boolean initialSync) {
            int ailmentSize = attachment.instances.size();
            buf.writeInt(ailmentSize);
            for (int i = 0; i < ailmentSize; i++){
                AilmentInstance<?> ailmentInstance = attachment.instances.get(i);
                buf.writeUtf(ailmentInstance.type.ID.toString());
                ailmentInstance.write(buf);
            }
            int limbSize = attachment.body.limbs.length;
            buf.writeInt(limbSize);
            for (int i = 0; i < limbSize; i++){
                Limb<?> limb = attachment.body.limbs[i];
                buf.writeUtf(limb.ID.toString());
                limb.write(buf);
            }
        }
        @Override
        public @Nullable HealthToken read(@NonNull IAttachmentHolder holder, RegistryFriendlyByteBuf buf,
                                                    @Nullable HealthToken previousValue) {
            boolean validateClient = true,
                    clientFlagFinal = false;

            int ailmentSize = buf.readInt();
            ArrayList<AilmentInstance<?>> instances = new ArrayList<>(ailmentSize);
            if (ailmentSize != 0) {
                for (int i = 0; i < ailmentSize; i++) {
                    String stringID = buf.readUtf();
                    Identifier ident = Identifier.parse(stringID);
                    Ailment ailment = AilmentManager.byIdentifier(ident);
                    if (ailment != null) {
                        AilmentInstance<?> oldInstance = previousValue != null ? previousValue.getAilment(ailment) : null;
                        boolean client;
                        if (oldInstance != null) client = oldInstance.clientSide;
                        else if (holder instanceof Entity e) client = e.level().isClientSide();
                        else throw new RuntimeException("Oops! Couldn't figure out dist context from supplied arguments when attempting to read an ailment instance from HealthToken syncing. HealthTokens are only compatible with Players, make sure you aren't putting them on anything else!");
                        clientFlagFinal |= client;
                        validateClient = false;
                        //Why the FUCK does ArrayList.set(int, E) throw an error even if the index would be in bounds of the
                        // array as defined by the initialCapacity argument??? Why can't I use the FUCKING ARRAY in my FUCKING ARRAYLIST
                        instances.add(ailment.readSidedInstance(holder, buf, client, oldInstance));
                    }
                }
            }
            int limbSize = buf.readInt();
            Limb<?>[] limbs = new Limb<?>[limbSize];
            if (limbSize != 0){
                for (int i = 0; i < limbSize; i++){
                    String stringID = buf.readUtf();
                    Identifier ident = Identifier.parse(stringID);
                    LimbManager.LimbDefaultInstanceSupplier supplier = LimbManager.byIdentifier(ident);
                    if (supplier != null){
                        Limb<?> oldInstance = previousValue != null ? previousValue.getLimb(ident) : null;
                        boolean client;
                        if (oldInstance != null) client = oldInstance.clientSide;
                        else if (holder instanceof Entity e) client = e.level().isClientSide();
                        else throw new RuntimeException("Oops! Couldn't figure out dist context from supplied arguments when attempting to read a limb from HealthToken syncing. HealthTokens are only compatible with Players, make sure you aren't putting them on anything else!");
                        clientFlagFinal |= client;
                        validateClient = false;
                        Limb<?> limb = supplier.getSidedEmptyInstance(client);
                        limb.readUnsafe(holder, buf, oldInstance);
                        limbs[i] = limb;
                    }
                }
            }
            if (validateClient){
                if (holder instanceof Entity e) clientFlagFinal = e.level().isClientSide();
                else throw new RuntimeException("Oops! Couldn't figure out dist context from supplied arguments when attempting to build a body for HealthToken syncing. HealthTokens are only compatible with Players, make sure you aren't putting them on anything else!");
            }
            return new HealthToken(instances, Body.buildSidedBody(limbs, clientFlagFinal));
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
                    token.removeAilment(instance);
                }
            });
        }
    }

    @Override
    public String toString() {
        return "HealthToken{" +
                "instances=" + instances +
                ", body=" + body +
                '}';
    }
}
