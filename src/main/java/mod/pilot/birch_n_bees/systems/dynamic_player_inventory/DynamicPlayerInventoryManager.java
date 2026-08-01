package mod.pilot.birch_n_bees.systems.dynamic_player_inventory;

import io.netty.buffer.ByteBuf;
import mod.pilot.birch_n_bees.ABOBAB;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.function.Function;

public class DynamicPlayerInventoryManager {
    public static void modInit(IEventBus modEventBus){
        //modEventBus.addListener(DynamicPlayerInventoryManager::flushOnServerClose);
    }
    public static boolean setup = false;
    public static void systemSetup(boolean client, Function<Player, DynamicInventoryToken> tokenBuilder){
        DynamicPlayerInventoryManager.client = client;
        DynamicPlayerInventoryManager.TOKEN_BUILDER = tokenBuilder;
        size = 0; index = -1;

        setup = true;
    }

    static void flushOnServerClose(ServerStoppedEvent event) {
        flush();
    }
    public static void flush(){
        client = false;
        TOKEN_BUILDER = null;
        PLAYERS_BY_UUID = null;
        TOKENS_BY_SLOT = null;
        size = 0; index = -1;
    }

    private static boolean client;
    public static boolean isClientSide(){return client;}
    public static boolean isServerSide(){return !client;}

    public static Function<Player, DynamicInventoryToken> TOKEN_BUILDER;

    public static UUID[] PLAYERS_BY_UUID;
    public static DynamicInventoryToken[] TOKENS_BY_SLOT;
    public static int size, index;

    public static DynamicInventoryToken getOrCreatePlayerToken(Player player){
        UUID uuid = player.getUUID();
        if (size != 0) {
            for (int i = 0; i < size; i++) {
                if (uuid.equals(PLAYERS_BY_UUID[index])) return TOKENS_BY_SLOT[i];
            }
        }
        return createToken(player);
    }
    public static @Nullable DynamicInventoryToken getTokenIfPresent(Player player){return getTokenIfPresent(player.getUUID());}
    public static @Nullable DynamicInventoryToken getTokenIfPresent(UUID player){
        int tokenIndex = getIndex(player);
        if (tokenIndex == -1) return null;
        else return TOKENS_BY_SLOT[tokenIndex];
    }
    public static int getIndex(UUID uuid){
        for (int i = 0; i < size; i++) {
            if (uuid.equals(PLAYERS_BY_UUID[i])) return i;
        }
        return -1;
    }
    public static int getIndex(DynamicInventoryToken token){
        for (int i = 0; i < size; i++) {
            if (token.equals(TOKENS_BY_SLOT[i])) return i;
        }
        return -1;
    }

    public static DynamicInventoryToken createToken(Player player){
        DynamicInventoryToken token = TOKEN_BUILDER.apply(player);
        UUID uuid = player.getUUID();
        if (size == 0){
            size = 1;
            PLAYERS_BY_UUID = new UUID[size];
            TOKENS_BY_SLOT = new DynamicInventoryToken[size];
            PLAYERS_BY_UUID[0] = uuid;
            TOKENS_BY_SLOT[0] = token;
            index = -1;
        }
        if (index == -1){
            growArray(1);
            PLAYERS_BY_UUID[index] = uuid;
            TOKENS_BY_SLOT[index] = token;
            index = -1;
        }
        else {
            PLAYERS_BY_UUID[index] = uuid;
            TOKENS_BY_SLOT[index] = token;
            locateNextOpenIndexIfAny(index);
        }
        if (isClientSide()){
            ClientPacketDistributor.sendToServer(token);
        } else PacketDistributor.sendToAllPlayers(token);
        return token;
    }
    public static void clearToken(UUID player){
        for (int i = 0; i < size; i++) {
            if (player.equals(PLAYERS_BY_UUID[i])){
                clearToken(i);
            }
        }
    }
    public static void clearToken(int at){
        if (at >= size) return;
        UUID uuid = PLAYERS_BY_UUID[at];
        PLAYERS_BY_UUID[at] = null;
        TOKENS_BY_SLOT[at] = null;
        if (index == -1 || index > at) index = at;

        TokenTerminateRequest terminate = new TokenTerminateRequest(uuid);
        if (isClientSide()) ClientPacketDistributor.sendToServer(terminate);
        else PacketDistributor.sendToAllPlayers(terminate);
    }
    public static void clearTokenQuietly(UUID player){
        for (int i = 0; i < size; i++) {
            if (player.equals(PLAYERS_BY_UUID[i])){
                clearToken(i);
            }
        }
    }
    public static void clearTokenQuietly(int at){
        if (at >= size) return;
        PLAYERS_BY_UUID[at] = null;
        TOKENS_BY_SLOT[at] = null;
        if (index == -1 || index > at) index = at;
    }


    public static @Nullable DynamicInventoryToken mutateToken(Player player, Function<DynamicInventoryToken, DynamicInventoryToken> mutator){
        int tokenIndex = getIndex(player.getUUID());
        if (tokenIndex == -1) return null;
        return TOKENS_BY_SLOT[tokenIndex] = mutator.apply(TOKENS_BY_SLOT[tokenIndex]);
    }
    public static @Nullable DynamicInventoryToken mutateToken(UUID player, Function<DynamicInventoryToken, DynamicInventoryToken> mutator){
        int tokenIndex = getIndex(player);
        if (tokenIndex == -1) return null;
        return TOKENS_BY_SLOT[tokenIndex] = mutator.apply(TOKENS_BY_SLOT[tokenIndex]);
    }
    public static @Nonnull DynamicInventoryToken createTokenIfNotPresentThenMutate(Player player, Function<DynamicInventoryToken, DynamicInventoryToken> mutator){
        int tokenIndex = getIndex(player.getUUID());
        DynamicInventoryToken token;
        if (tokenIndex == -1) {
            token = createToken(player);
            tokenIndex = getIndex(token);
        }
        else token = TOKENS_BY_SLOT[tokenIndex];
        return TOKENS_BY_SLOT[tokenIndex] = mutator.apply(token);
    }

    public static void resizeArray(int newSize){
        UUID[] uuidDelta = new UUID[newSize];
        DynamicInventoryToken[] tokenDelta = new DynamicInventoryToken[newSize];

        boolean shrink = newSize < size;
        if (shrink) {
            System.arraycopy(PLAYERS_BY_UUID, 0, uuidDelta, 0, newSize);
            System.arraycopy(TOKENS_BY_SLOT, 0, tokenDelta, 0, newSize);
        } else {
            System.arraycopy(PLAYERS_BY_UUID, 0, uuidDelta, 0, size);
            System.arraycopy(TOKENS_BY_SLOT, 0, tokenDelta, 0, size);
        }
        PLAYERS_BY_UUID = uuidDelta;
        TOKENS_BY_SLOT = tokenDelta;
        size = newSize;
        if (shrink && index >= size) locateNextOpenIndexIfAny();
    }
    public static void growArray(int amount){
        int newSize = size + amount;
        UUID[] uuidDelta = new UUID[newSize];
        DynamicInventoryToken[] tokenDelta = new DynamicInventoryToken[newSize];

        System.arraycopy(PLAYERS_BY_UUID, 0, uuidDelta, 0, size);
        System.arraycopy(TOKENS_BY_SLOT, 0, tokenDelta, 0, size);
        PLAYERS_BY_UUID = uuidDelta;
        TOKENS_BY_SLOT = tokenDelta;
        size = newSize;
        if (index == -1) index = size;
    }

    public static void locateNextOpenIndexIfAny(){locateNextOpenIndexIfAny(0);}
    public static void locateNextOpenIndexIfAny(int startAt){
        for (int i = startAt; i < size; i++){
            if (testIndex(i)){
                index = i;
                return;
            }
        }
        index = -1;
    }
    public static boolean testIndex(int index){
        if (index >= size) return false;
        else return PLAYERS_BY_UUID[index] == null;
    }

    public static long mostSignificantBits(DynamicInventoryToken token){
        int index = getIndex(token);
        return PLAYERS_BY_UUID[index].getMostSignificantBits();
    }
    public static long leastSignificantBits(DynamicInventoryToken token){
        int index = getIndex(token);
        return PLAYERS_BY_UUID[index].getLeastSignificantBits();
    }

    public static class DynamicInventoryToken implements CustomPacketPayload {
        public static DynamicInventoryToken defaultToken(boolean unrestricted){
            return new DynamicInventoryToken(9, unrestricted ? 27 : 0, true);
        }
        public DynamicInventoryToken(int hotbarSlots, int inventorySlots, boolean offhand){
            this.hotbarSlots = hotbarSlots;
            this.inventorySlots = inventorySlots;
            this.validOffhand = offhand;
        }
        public int hotbarSlots;
        public static int hotbarSlots(DynamicInventoryToken token) {
            return token.hotbarSlots;
        }
        public int inventorySlots;
        public static int inventorySlots(DynamicInventoryToken token) {
            return token.inventorySlots;
        }
        public boolean validOffhand;
        public static boolean validOffhand(DynamicInventoryToken token){
            return token.validOffhand;
        }

        public boolean shouldLock(int index){
            if (index < 9){
                return index > hotbarSlots;
            } else {
                return (index - 9) > inventorySlots;
            }
        }

        public void applyLocking(AbstractContainerMenu menu){
            for (Slot slot : menu.slots){
                if (slot instanceof LockedSlot locked){
                    locked.locked = shouldLock(locked.getSlotIndex());
                }
            }
        }

        public int totalSlots(){
            return inventorySlots + hotbarSlots;
        }

        public static final StreamCodec<ByteBuf, DynamicInventoryToken> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT,
                DynamicInventoryToken::hotbarSlots,
                ByteBufCodecs.INT,
                DynamicInventoryToken::inventorySlots,
                ByteBufCodecs.BOOL,
                DynamicInventoryToken::validOffhand,
                ByteBufCodecs.LONG,
                DynamicPlayerInventoryManager::mostSignificantBits,
                ByteBufCodecs.LONG,
                DynamicPlayerInventoryManager::leastSignificantBits,
                DynamicPlayerInventoryManager.DynamicInventoryToken::unpackFromPayload
        );

        public static DynamicInventoryToken unpackFromPayload(int hotbarSlots, int inventorySlots, boolean offhand,
                                                              long mostSig, long leastSig){
            DynamicInventoryToken token = new DynamicInventoryToken(hotbarSlots, inventorySlots, offhand);
            UUID uuid = new UUID(mostSig, leastSig);
            if (size == 0){
                size = 1;
                PLAYERS_BY_UUID = new UUID[size];
                TOKENS_BY_SLOT = new DynamicInventoryToken[size];
                PLAYERS_BY_UUID[0] = uuid;
                TOKENS_BY_SLOT[0] = token;
                index = -1;
            }
            if (index == -1){
                growArray(1);
                PLAYERS_BY_UUID[index] = uuid;
                TOKENS_BY_SLOT[index] = token;
                index = -1;
            }
            else {
                PLAYERS_BY_UUID[index] = uuid;
                TOKENS_BY_SLOT[index] = token;
                locateNextOpenIndexIfAny(index);
            }
            return token;
        }
        public static void handlePacketSync(DynamicInventoryToken token, IPayloadContext context){
            Player player = context.player();
            UUID uuid = PLAYERS_BY_UUID[getIndex(token)];
            if (player.getUUID().equals(uuid)){
                System.out.println("Successfully received client packet and found valid UUID!");
                token.applyLocking(player.inventoryMenu);
                if (isServerSide()){
                    PacketDistributor.sendToAllPlayers(token);
                }
            }
        }

        public static final Type<DynamicInventoryToken> PAYLOAD_TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "dynamic_inventory_token"));
        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return PAYLOAD_TYPE;
        }
    }
    public record TokenTerminateRequest(UUID uuid) implements CustomPacketPayload{
        public static TokenTerminateRequest fromLong(long mostSig, long leastSig){
            return new TokenTerminateRequest(new UUID(mostSig, leastSig));
        }
        public static final StreamCodec<ByteBuf, TokenTerminateRequest> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.LONG,
                (request) -> request.uuid().getMostSignificantBits(),
                ByteBufCodecs.LONG,
                (request) -> request.uuid().getLeastSignificantBits(),
                TokenTerminateRequest::fromLong
        );

        public static void handleTerminateRequest(TokenTerminateRequest request, IPayloadContext context){
            clearTokenQuietly(request.uuid);
            if (isServerSide()) PacketDistributor.sendToAllPlayers(request);
        }

        public static final Type<TokenTerminateRequest> PAYLOAD_TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "token_terminate_request"));
        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return PAYLOAD_TYPE;
        }
    }



    public static final Function<Player, DynamicInventoryToken> DEFAULT_TOKEN_CONSTRUCTOR =
            (player) -> DynamicPlayerInventoryManager.DynamicInventoryToken.defaultToken(false);
}
