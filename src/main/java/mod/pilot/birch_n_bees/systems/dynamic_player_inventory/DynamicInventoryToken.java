package mod.pilot.birch_n_bees.systems.dynamic_player_inventory;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.Config;
import mod.pilot.birch_n_bees.util.BirchAttachmentTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
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

    public DynamicInventoryToken copy(){
        return new DynamicInventoryToken(hotbarSlots, inventorySlots, offhand, armor);
    }

    public static int HOTBAR_SOFT_CAP = 9;
    public static int INVENTORY_SOFT_CAP = 27;

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
    //goes, head, chest, legs, feet from index 0-3 (I think...)
    public boolean[] armor;

    public void apply(Player player){
        apply(player, player.containerMenu);
    }
    public void apply(Player player, AbstractContainerMenu menu) {
        for (Slot slot : menu.slots){
            if (slot instanceof LockedSlot locked){
                locked.locked = shouldLock(menu, slot);
            }
        }
        DynamicInventory resizable = (DynamicInventory) player.getInventory();
        resizable.ballad$resizeHotbar(hotbarSlots);
        resizable.ballad$resizeInventory(inventorySlots);
        resizable.ballad$updateOffhand(offhand);
        resizable.ballad$updateArmor(armor);
        if (player instanceof ServerPlayer serverPlayer){
            serverPlayer.getInventory().setChanged();
        }
    }
    public boolean shouldLock(AbstractContainerMenu menu, Slot slot) {
        int index = slot.getSlotIndex();
        if (/*menu instanceof InventoryMenu && */index == 40) return !offhand;
        if (index < 9) return index >= hotbarSlots;
        if (index >= 36 && index < 40){
            return !armor[index - 36];
        }
        else return (index - 8) > inventorySlots;
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

    @Override
    public String toString() {
        return "DynamicInventoryToken{" +
                "hotbarSlots=" + hotbarSlots +
                ", inventorySlots=" + inventorySlots +
                ", offhand=" + offhand +
                ", armor=" + Arrays.toString(armor) +
                '}';
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
}
