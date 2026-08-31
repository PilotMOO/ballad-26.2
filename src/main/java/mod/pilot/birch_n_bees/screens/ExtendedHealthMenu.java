package mod.pilot.birch_n_bees.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class ExtendedHealthMenu extends AbstractContainerMenu /*implements IDynamicMenuDisplay*/ {
    public final Player player;

    public ExtendedHealthMenu(int containerId, Inventory inventory){
        this(containerId, inventory, Minecraft.getInstance().player);
    }
    public ExtendedHealthMenu(int containerId, Inventory inventory, Player player) {
        super(BirchMenuTypes.HEALTH_UI_MENU_TYPE.get(), containerId);
        this.player = player;
        addStandardInventorySlots(inventory, 8, 159);

        this.addSlot(new ArmorSlot(inventory, player, EquipmentSlot.HEAD, 39, -17, 155,
                InventoryMenu.EMPTY_ARMOR_SLOT_HELMET));
        this.addSlot(new ArmorSlot(inventory, player, EquipmentSlot.CHEST, 38, -17, 173,
                InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE));
        this.addSlot(new ArmorSlot(inventory, player, EquipmentSlot.LEGS, 37, -17, 191,
                InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS));
        this.addSlot(new ArmorSlot(inventory, player, EquipmentSlot.FEET, 36, -17, 209,
                InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS));

    }

    /*public IDynamicMenuDisplayable[] displayables;
    int index;
    public DynamicMenuButton[] buttons;
    int buttonIndex;
    public void addDisplayable(IDynamicMenuDisplayable displayable){
        if (index >= displayables.length){
            IDynamicMenuDisplayable[] newArray = new IDynamicMenuDisplayable[displayables.length + 1];
            System.arraycopy(displayables, 0, newArray, 0, displayables.length);
            displayables = newArray;
        }
        displayables[index++] = displayable;
        if (displayable instanceof DynamicMenuButton button){
            if (buttonIndex >=  buttons.length){
                DynamicMenuButton[] newArray = new DynamicMenuButton[buttons.length + 1];
                System.arraycopy(buttons, 0, newArray, 0, buttons.length);
                buttons = newArray;
            }
            buttons[buttonIndex++] = button;
        }
    }
    public void removeDisplayable(int at){
        if (at >= displayables.length || at < 0 || at >= index) return;
        IDynamicMenuDisplayable old = displayables[at];
        System.arraycopy(displayables, at + 1, displayables, at, ((index--) - at));
        if (old instanceof DynamicMenuButton){
            int oldButtonIndex = buttonsBeforeIndex(at);
            System.arraycopy(buttons, oldButtonIndex + 1, buttons, oldButtonIndex, (buttonIndex - oldButtonIndex));
        }
    }
    public int buttonsBeforeIndex(int index){
        int count = index == -1 ? displayables.length : Math.min(index, displayables.length);
        int total = 0;
        for (int i = 0; i < count; i++){
            if (displayables[i] instanceof DynamicMenuButton) total++;
        }
        return total;
    }

    @Override
    public IDynamicMenuDisplayable byIndex(int index) {
        return displayables[index];
    }*/

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        return super.clickMenuButton(player, buttonId);
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int i) {
        return ItemStack.EMPTY;
    }
    @Override
    public boolean stillValid(Player player) {
        return player.isAlive();
    }
}
