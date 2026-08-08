package mod.pilot.birch_n_bees.systems.dynamic_player_inventory;

import io.netty.buffer.ByteBuf;
import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.Config;
import mod.pilot.birch_n_bees.util.BirchAttachmentTypes;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

public class DynamicInventoryToken implements ValueIOSerializable {
    public static AttachmentType<DynamicInventoryToken> ATTACHMENT;
    public static DynamicInventoryToken get(Player player){
        if (ATTACHMENT == null) ATTACHMENT = BirchAttachmentTypes.INVENTORY_TOKEN_ATTACHMENT.get();
        return player.getData(ATTACHMENT);
    }
    public static boolean has(Player player){
        if (ATTACHMENT == null) ATTACHMENT = BirchAttachmentTypes.INVENTORY_TOKEN_ATTACHMENT.get();
        return player.hasData(ATTACHMENT);
    }

    public DynamicInventoryToken() {
        this(
                DEFAULT_HOTBAR,
                defaultInventoryByConfig(),
                DEFAULT_OFFHAND,
                DEFAULT_ARMOR
        );
    }
    public DynamicInventoryToken(int hotbar, int inventory, boolean offhand, boolean[] armor){
        this.hotbarSlots = hotbar;
        this.inventorySlots = inventory;
        this.offhand = offhand;
        this.armor = armor;
    }

    public static int DEFAULT_HOTBAR = 9;
    public static int DEFAULT_INVENTORY_VANILLA = 27, DEFAULT_INVENTORY_ABOBAB = 0;
    public static int defaultInventoryByConfig(){
        return (ABOBAB.configLoaded && Config.SERVER.enableDefaultInventory.get()) ?
                DEFAULT_INVENTORY_VANILLA :
                DEFAULT_INVENTORY_ABOBAB;
    }
    public static boolean DEFAULT_OFFHAND = true;
    public static boolean[] DEFAULT_ARMOR = new boolean[4];
    static{
        DEFAULT_ARMOR[0] = true;
        DEFAULT_ARMOR[1] = true;
        DEFAULT_ARMOR[2] = true;
        DEFAULT_ARMOR[3] = true;
    }

    public int hotbarSlots;
    public int inventorySlots;
    public boolean offhand;
    //goes: feet, legs, chest, head from index 0-3 (I think...)
    public boolean[] armor;

    public static void applyInFull(Player player){
        get(player).apply(player);
    }
    public static void applyOnlyToMenu(Player player){
        get(player).applyToMenu(player.containerMenu);
    }
    public static void applyOnlyToInventory(Player player){
        get(player).applyToInventory(player);
    }

    public void apply(Player player){
        applyToInventory(player);
        applyToMenu(player.containerMenu);
    }
    public void applyToMenu(AbstractContainerMenu menu) {
        for (Slot slot : menu.slots){
            if (slot instanceof LockedSlot locked){
                locked.locked = shouldLock(/*menu, */slot);
            }
        }
        //menu.broadcastFullState();
    }
    public void applyToInventory(Player player){
        DynamicInventory resizable = (DynamicInventory) player.getInventory();
        resizable.ballad$resizeHotbar(hotbarSlots);
        resizable.ballad$resizeInventory(inventorySlots);
        resizable.ballad$updateOffhand(offhand);
        resizable.ballad$updateArmor(armor);
        resizable.setChanged();
        if (player instanceof ServerPlayer serverPlayer){
            requestTokenReapplyFromServer(serverPlayer);
        }
    }

    public boolean shouldLock(/*AbstractContainerMenu menu, */Slot slot) {
        int index = slot.getSlotIndex();
        if (/*menu instanceof InventoryMenu && */index == 40) return !offhand;
        if (index < 9) return index >= hotbarSlots;
        if (index >= 36 && index < 40){
            return !armor[index - 36];
        }
        else return (index - 8) > inventorySlots;
    }

    public static void requestTokenReapplyFromServer(ServerPlayer player){
        player.connection.send(TokenReapplyRequest.INSTANCE);
    }
    public static void requestTokenReapplyFromClient(LocalPlayer player){
        player.connection.send(TokenReapplyRequest.INSTANCE);
    }

    public static void receiveReapplyRequestOnServer(TokenReapplyRequest request, final IPayloadContext context){
        context.enqueueWork(() -> {
            Player player = context.player();
            applyInFull(player);
            //if (player instanceof ServerPlayer sp) requestTokenReapplyFromServer(sp);
        });
    }
    public static void receiveReapplyRequestOnClient(TokenReapplyRequest request, final IPayloadContext context){
        context.enqueueWork(() -> applyInFull(context.player()));
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putInt("hotbarSlots", hotbarSlots);
        output.putInt("inventorySlots", inventorySlots);
        output.putBoolean("offhand", offhand);
        for (int i = 0; i < 4; i++){
            output.putBoolean("armor" + i, armor[i]);
        }
    }
    @Override
    public void deserialize(ValueInput input) {
        hotbarSlots = input.getIntOr("hotbarSlots", DEFAULT_HOTBAR);
        inventorySlots = input.getIntOr("inventorySlots", defaultInventoryByConfig());
        offhand = input.getBooleanOr("offhand", DEFAULT_OFFHAND);
        for (int i = 0; i < 4; i++){
            armor[i] = input.getBooleanOr("armor" + i, DEFAULT_ARMOR[i]);
        }
    }
    public static class Syncer implements AttachmentSyncHandler<DynamicInventoryToken> {
        @Override
        public void write(RegistryFriendlyByteBuf buf, DynamicInventoryToken attachment, boolean initialSync) {
            buf.writeInt(attachment.hotbarSlots);
            buf.writeInt(attachment.inventorySlots);
            buf.writeBoolean(attachment.offhand);
            for (int i = 0; i < 4; i++){
                buf.writeBoolean(attachment.armor[i]);
            }
        }
        @Override
        public @Nullable DynamicInventoryToken read(@NonNull IAttachmentHolder holder, RegistryFriendlyByteBuf buf,
                                                    @Nullable DynamicInventoryToken previousValue) {
            int hotbarSlots = buf.readInt();
            int inventorySlots = buf.readInt();
            boolean offhand = buf.readBoolean();
            boolean[] armor = new boolean[4];
            for (int i = 0; i < 4; i++){
                armor[i] = buf.readBoolean();
            }
            return new DynamicInventoryToken(hotbarSlots, inventorySlots, offhand, armor);
        }
    }
    public record TokenReapplyRequest() implements CustomPacketPayload{
        public static final TokenReapplyRequest INSTANCE = new TokenReapplyRequest();
        public static StreamCodec<ByteBuf, TokenReapplyRequest> CODEC = StreamCodec.unit(INSTANCE);
        public static final Type<TokenReapplyRequest> PACKET_TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "token_reapply_request"));
        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return PACKET_TYPE;
        }
    }

    @Override
    public String toString() {
        return "DynamicInventoryToken{" +
                "hotbarSlots=" + hotbarSlots +
                ", inventorySlots=" + inventorySlots +
                ", offhand=" + offhand +
                ", armor=" + Arrays.toString(armor) +
                '}';
    }
}
