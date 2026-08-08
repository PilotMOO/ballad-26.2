package mod.pilot.birch_n_bees.systems.dynamic_player_inventory;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import mod.pilot.birch_n_bees.mixins.InventoryAccessor;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;

import java.util.Iterator;
import java.util.function.Predicate;

public class DynamicInventory extends Inventory implements IPlayerInventoryResizable {
    public DynamicInventory(Player player, EntityEquipment equipment) {
        super(player, (this.equipment = equipment));
    }
    public EntityEquipment equipment;
    public NonNullList<ItemStack> items;
    //public FullContextWrapper fullWrapper;
    public IteratorContextWrapper wrapper;
    public void createWrapper(){
        //fullWrapper = new FullContextWrapper();
        wrapper = new IteratorContextWrapper();
    }
    public int hotbarLimit, inventoryLimit;
    public int lockedSize;
    public void resizeHotbar(int size) {
        size = Math.clamp(size, 0, 9);
        hotbarLimit = size - 1;
        lockedSize = hotbarLimit + inventoryLimit + 2;
    }
    public void resizeInventory(int size) {
        size = Math.clamp(size, 0, 27);
        inventoryLimit = size - 1;
        lockedSize = hotbarLimit + inventoryLimit + 2; //+2 because both limits are zero-context, but the locked size isn't
    }

    public DynamicInventoryToken token(){
        return DynamicInventoryToken.get(player);
    }

    public ItemStack getWithContext(int index){
        return items.get(getEffectiveIndex(index));
    }
    public ItemStack getWithoutContext(int index){
        return items.get(index);
    }
    public ItemStack setWithContext(int index, ItemStack stack){
        return items.set(getEffectiveIndex(index), stack);
    }
    public ItemStack setWithoutContext(int index, ItemStack stack){
        return items.set(index, stack);
    }
    public int getEffectiveIndex(int index){
        if (index > hotbarLimit) index += (8 - hotbarLimit);
        return index;
    }
    public boolean invalidSlot(int slot){
        if (slot < 9) return slot > hotbarLimit;
        else if (slot < 36) return slot > (inventoryLimit + 9);
        else return false;
    }

    /*REWRITTEN METHODS*/
    public int selected; //Up-porting the selected object because fuck you why the hell is it private
    @Override public int getSelectedSlot() {
        return this.selected;
    }
    @Override public void setSelectedSlot(int selected) {
        if (selected < -1 || selected >= 9) {
            throw new IllegalArgumentException("Invalid selected slot");
        }
        else if (selected <= hotbarLimit) this.selected = selected;
        else if (selected == hotbarLimit + 1) this.selected = 0;
        else /*if (selected == 8)*/ this.selected = hotbarLimit;
    }

    @Override
    public @NonNull ItemStack getSelectedItem() {
        if (selected == -1) return ItemStack.EMPTY;
        else return getWithoutContext(selected);
    }
    @Override
    public @NonNull ItemStack setSelectedItem(@NonNull ItemStack itemStack) {
        if (selected == -1) {
            placeItemBackInInventory(itemStack);
            return ItemStack.EMPTY;
        }
        //In theory, the selected item should never get into locked hotbar slots,
        // so we should be fine deferring to contextless assigment...
        return setWithoutContext(selected, itemStack);
    }
    @Override
    public @NonNull NonNullList<ItemStack> getNonEquipmentItems() {
        //We don't want anyone trying to read locked item slots,
        // however if they try to use the same for(...) logic where they reference indexes instead,
        // we can't just return the full context wrapper, else we will have issues.
        // So, just return a wrapper that contextualizes only the iterator!
        return wrapper;
    }
    @Override
    public int getFreeSlot() {
        for(int i = 0; i < items.size(); ++i) {
            if (invalidSlot(i)) continue;
            if (getWithoutContext(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }
    @Override
    public void addAndPickItem(@NonNull ItemStack itemStack) {
        this.setSelectedSlot(this.getSuitableHotbarSlot()); //ToDo! Override suitable hotbar method to prevent improper indexes...
        if (!(getSelectedItem().isEmpty())) {
            int freeSlot = this.getFreeSlot();
            if (freeSlot != -1) {
                setWithoutContext(freeSlot, getSelectedItem());
            }
        }
        setSelectedItem(itemStack);
    }

    @Override
    public void pickSlot(int slot) {
        this.setSelectedSlot(this.getSuitableHotbarSlot());
        ItemStack oldSelected = getSelectedItem();
        setSelectedItem(getWithoutContext(slot));
        setWithoutContext(slot, oldSelected);
    }
    @Override
    public int findSlotMatchingItem(@NonNull ItemStack itemStack) {
        for(int i = 0; i < items.size(); ++i) {
            if (invalidSlot(i)) continue;
            ItemStack curItem = getWithoutContext(i);
            if (!curItem.isEmpty() && ItemStack.isSameItemSameComponents(itemStack, curItem)) {
                return i;
            }
        }
        //We don't modify the return values at all because we only recontextaulize natively, everything else uses contextless indexes
        return -1;
    }

    public int findSlotMatchingCraftingIngredient(@NonNull Holder<Item> item, @NonNull ItemStack existingItem) {
        for(int i = 0; i < items.size(); ++i) {
            if (invalidSlot(i)) continue;
            ItemStack inventoryItemStack = getWithoutContext(i);
            if (!inventoryItemStack.isEmpty() && inventoryItemStack.is(item) && isUsableForCrafting(inventoryItemStack) && (existingItem.isEmpty() || ItemStack.isSameItemSameComponents(existingItem, inventoryItemStack))) {
                return i;
            }
        }

        return -1;
    }

    @Override //this is a tad questionable but eh...?
    public int getSuitableHotbarSlot() {
        for(int slot = 0; slot < items.size(); ++slot) {
            int index = (this.selected + slot) % 9;
            if (invalidSlot(index)) continue;
            //We don't need to worry about context, because the for statement and index remainer already clamps to context
            if (getWithoutContext(index).isEmpty()) {
                return index;
            }
        }

        for(int slot = 0; slot < items.size(); ++slot) {
            int index = (this.selected + slot) % 9;
            if (invalidSlot(index)) continue;
            if (!getWithoutContext(index).isEnchanted()) {
                return index;
            }
        }

        return this.selected;
    }

    //No need to override anything, as just contextualizing the access methods should work,,,?
    @Override
    public int clearOrCountMatchingItems(@NonNull Predicate<ItemStack> predicate, int amountToRemove, @NonNull Container craftSlots) {
        return super.clearOrCountMatchingItems(predicate, amountToRemove, craftSlots);
    }

    //I fucking hate private methods. At LEAST make them FUCKING PROTECTED
    // these methods can't be overridden without mixins,
    // but they only use externally accessible methods like setItem(int, ItemStack) so in theory we don't need to modify them...?
    /*
    private int addResource(ItemStack itemStack) {}
    private int addResource(int slot, ItemStack itemStack) {}
    */

    @Override
    public int getSlotWithRemainingSpace(@NonNull ItemStack newItemStack) {
        if (!(this instanceof InventoryAccessor accessor)) return -1;

        if (accessor.callHasRemainingSpaceForItem(getSelectedItem(), newItemStack)) {
            return this.selected;
        } else if (token().offhand && accessor.callHasRemainingSpaceForItem(this.getItem(40), newItemStack)) {
            return 40;
        } else {
            for(int i = 0; i < items.size(); ++i) {
                if (invalidSlot(i)) continue;
                if (accessor.callHasRemainingSpaceForItem(getWithoutContext(i), newItemStack)) {
                    return i;
                }
            }

            return -1;
        }
    }

    @Override
    public void tick() {
        for(int i = 0; i < items.size(); ++i) {
            if (invalidSlot(i)) continue;
            ItemStack itemStack = this.getWithoutContext(i);
            if (!itemStack.isEmpty()) {
                itemStack.inventoryTick(this.player.level(), this.player, i == this.selected ? EquipmentSlot.MAINHAND : null);
            }
        }

    }

    @Override public boolean add(@NonNull ItemStack itemStack) {return super.add(itemStack);}
    @Override
    public boolean add(int slot, ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        } else {
            try {
                if (itemStack.isDamaged()) {
                    if (slot == -1) {
                        slot = this.getFreeSlot();
                    }

                    if (slot >= 0) {
                        setWithoutContext(slot, itemStack.copyAndClear());
                        getWithoutContext(slot).setPopTime(5);
                        return true;
                    } else if (this.player.hasInfiniteMaterials()) {
                        itemStack.setCount(0);
                        return true;
                    } else {
                        return false;
                    }
                } else if (this instanceof InventoryAccessor accessor) {
                    int lastSize;
                    do {
                        lastSize = itemStack.getCount();
                        if (slot == -1) {
                            itemStack.setCount(accessor.callAddResource(itemStack));
                        } else {
                            itemStack.setCount(accessor.callAddResource(slot, itemStack));
                        }
                    } while(!itemStack.isEmpty() && itemStack.getCount() < lastSize);

                    if (itemStack.getCount() == lastSize && this.player.hasInfiniteMaterials()) {
                        itemStack.setCount(0);
                        return true;
                    } else {
                        return itemStack.getCount() < lastSize;
                    }
                } else return false;
            } catch (Throwable t) {
                CrashReport report = CrashReport.forThrowable(t, "Adding item to inventory");
                CrashReportCategory category = report.addCategory("Item being added");
                category.setDetail("Registry Name", () -> String.valueOf(BuiltInRegistries.ITEM.getKey(itemStack.getItem())));
                category.setDetail("Item Class", () -> itemStack.getItem().getClass().getName());
                category.setDetail("Item ID", Item.getId(itemStack.getItem()));
                category.setDetail("Item data", itemStack.getDamageValue());
                category.setDetail("Item name", () -> itemStack.getHoverName().getString());
                throw new ReportedException(report);
            }
        }
    }

    @Override public void placeItemBackInInventory(@NonNull ItemStack itemStack) {
        super.placeItemBackInInventory(itemStack);
    }
    @Override public void placeItemBackInInventory(ItemStack itemStack, boolean shouldSendSetSlotPacket) {
        super.placeItemBackInInventory(itemStack, shouldSendSetSlotPacket);
    }

    @Override //Due to this being used externally, we will actually just still use the getItem method
    public @NonNull ClientboundSetPlayerInventoryPacket createInventoryUpdatePacket(int slot) {
        return super.createInventoryUpdatePacket(slot);
    }

    @Override
    public @NonNull ItemStack removeItem(int slot, int count) {
        if (invalidSlot(slot)) return ItemStack.EMPTY;
        if (slot < items.size()) {
            return ContainerHelper.removeItem(wrapper, slot, count);
        } else {
            EquipmentSlot equipmentSlot = EQUIPMENT_SLOT_MAPPING.get(slot);
            if (equipmentSlot != null) {
                ItemStack itemStack = this.equipment.get(equipmentSlot);
                if (!itemStack.isEmpty()) {
                    return itemStack.split(count);
                }
            }

            return ItemStack.EMPTY;
        }
    }

    @Override
    public void removeItem(@NonNull ItemStack itemStack) {
        for(int slot = 0; slot < items.size(); ++slot) {
            if (invalidSlot(slot)) continue;
            if (getWithoutContext(slot) == itemStack) {
                setWithoutContext(slot, ItemStack.EMPTY);
                return;
            }
        }

        ObjectIterator<EquipmentSlot> var5 = EQUIPMENT_SLOT_MAPPING.values().iterator();

        while(var5.hasNext()) {
            EquipmentSlot equipmentSlot = var5.next();
            ItemStack stackInSlot = this.equipment.get(equipmentSlot);
            if (stackInSlot == itemStack) {
                this.equipment.set(equipmentSlot, ItemStack.EMPTY);
                return;
            }
        }
    }

    @Override
    public @NonNull ItemStack removeItemNoUpdate(int slot) {
        if (invalidSlot(slot)) return ItemStack.EMPTY;
        if (slot < items.size()) {
            ItemStack itemStack = getWithoutContext(slot);
            setWithoutContext(slot, ItemStack.EMPTY);
            return itemStack;
        } else {
            EquipmentSlot equipmentSlot = EQUIPMENT_SLOT_MAPPING.get(slot);
            return equipmentSlot != null ? this.equipment.set(equipmentSlot, ItemStack.EMPTY) : ItemStack.EMPTY;
        }
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        if (invalidSlot(slot)) return;
        if (slot < items.size()) {
            setWithoutContext(slot, itemStack);
        }

        EquipmentSlot equipmentSlot = EQUIPMENT_SLOT_MAPPING.get(slot);
        if (equipmentSlot != null) {
            this.equipment.set(equipmentSlot, itemStack);
        }
    }

    @Override
    public void save(ValueOutput.TypedOutputList<ItemStackWithSlot> output) {
        for (int i = 0; i < items.size(); i++) {
            ItemStack item = getWithoutContext(i);
            if (!item.isEmpty()) {
                System.out.println("shitting MYself... index " + i + ", stack " + item);
                output.add(new ItemStackWithSlot(i, item));
            }
        }
    }
    @Override
    public void load(ValueInput.TypedInputList<ItemStackWithSlot> input) {
        this.items.clear();

        for (ItemStackWithSlot item : input) {
            if (item.isValidInContainer(this.items.size())) {
                System.out.println("shitting yourself... index " + item.slot() + ", stack " + item.stack());
                setWithoutContext(item.slot(), item.stack());
            }
        }
    }

    @Override public int getContainerSize() {return super.getContainerSize();}

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : wrapper) {
            if (!itemStack.isEmpty()) {
                return false;
            }
        }

        for (EquipmentSlot slot : EQUIPMENT_SLOT_MAPPING.values()) {
            if (!this.equipment.get(slot).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public @NonNull ItemStack getItem(int slot) {
        if (invalidSlot(slot)) return ItemStack.EMPTY;
        if (slot < this.items.size()) {
            return getWithoutContext(slot);
        }

        EquipmentSlot equipmentSlot = EQUIPMENT_SLOT_MAPPING.get(slot);
        return equipmentSlot != null ? this.equipment.get(equipmentSlot) : ItemStack.EMPTY;
    }

    //Here for completionism
    @Override public @NonNull Component getName() {return super.getName();}

    //None of this really changed, I just cleaned it up to use the helper methods so it's clearer how it functions within context
    @Override
    public void dropAll() {
        for(int i = 0; i < this.items.size(); ++i) {
            ItemStack itemStack = getWithoutContext(i);
            if (!itemStack.isEmpty()) {
                this.player.drop(itemStack, true, false);
                setWithoutContext(i, ItemStack.EMPTY);
            }
        }

        this.equipment.dropAll(this.player);
    }

    //Because MC API is a piece of shit, this variable is private. However, we don't really need to override it anyway,
    // so this just sits here for completionism, and so we know that it has been addressed
    /*@Override
    public void setChanged() {
        ++this.timesChanged;
    }
    @Override
    public int getTimesChanged() {
        return this.timesChanged;
    }*/

    //Completionism...
    @Override public boolean stillValid(@NonNull Player player) {return super.stillValid(player);}

    //Just invoke the super because the only part we would want to change is have it reference a contextual iterator,
    // which is already done via overriding the iterator() method that Container implements from the Iterable<> interface
    @Override public boolean contains(@NonNull ItemStack searchStack) {return super.contains(searchStack);}
    @Override public boolean contains(@NonNull TagKey<Item> tag) {return super.contains(tag);}
    @Override public boolean contains(@NonNull Predicate<ItemStack> predicate) {return super.contains(predicate);}

    @Override public void replaceWith(Inventory other) {super.replaceWith(other);}
    @Override public void clearContent() {super.clearContent();}

    @Override
    public void fillStackedContents(@NonNull StackedItemContents contents) {
        for (ItemStack itemStack : wrapper) {
            contents.accountSimpleStack(itemStack);
        }
    }

    @Override
    public @NonNull ItemStack removeFromSelected(boolean all) {
        //Up-ported just in case it's finicky as shit and that's why the tossing logic was fucky
        ItemStack selectedItem = this.getSelectedItem();
        return selectedItem.isEmpty() ? ItemStack.EMPTY : this.removeItem(this.selected, all ? selectedItem.getCount() : 1);
    }
    /**/

    @Override public @NonNull Iterator<ItemStack> iterator() {return new ContextualIterator();}

    /*public class FullContextWrapper extends NonNullList<ItemStack>{
        protected FullContextWrapper() {
            //Dud arguments, we never actually reference or use these...
            super(ImmutableList.of(), null);
        }
        @Override public @NonNull ItemStack get(int index) {
            return getWithContext(index);
        }
        @Override public @NonNull ItemStack set(int index, ItemStack element) {
            return setWithContext(index, element);
        }
        @Override public void add(int index, ItemStack element) {
            items.add(getEffectiveIndex(index), element);
        }
        @Override public @NonNull ItemStack remove(int index) {
            return items.remove(getEffectiveIndex(index));
        }

        @Override public int size() {return lockedSize;}
        @Override public void clear() {items.clear();}

        @Override
        public @NonNull Iterator<ItemStack> iterator() {
            return new ContextualIterator();
        }
    }*/
    public class IteratorContextWrapper extends NonNullList<ItemStack>{
        protected IteratorContextWrapper() {
            //Dud arguments, we never actually reference or use these...
            super(ImmutableList.of(), null);
        }
        @Override public @NonNull ItemStack get(int index) {
            return getWithoutContext(index);
        }
        @Override public @NonNull ItemStack set(int index, ItemStack element) {
            return setWithoutContext(index, element);
        }
        @Override public void add(int index, ItemStack element) {
            items.add(index, element);
        }
        @Override public @NonNull ItemStack remove(int index) {
            return items.remove(index);
        }

        @Override public int size() {return items.size();}
        @Override public void clear() {items.clear();}
        @Override
        public @NonNull Iterator<ItemStack> iterator() {
            return new ContextualIterator();
        }
    }
    public class ContextualIterator implements Iterator<ItemStack>{
        public ContextualIterator(){
            cursor = -1; size = lockedSize;
        }
        final int size;
        int cursor;

        @Override
        public boolean hasNext() {
            return ++cursor < size;
        }
        @Override
        public ItemStack next() {
            return getWithContext(cursor);
        }
    }

    //IPlayerInventoryResizable Implementation
    @Override
    public void ballad$resizeHotbar(int size) {
        int old = hotbarLimit;
        resizeHotbar(size);
        if (hotbarLimit < old){
            if (selected > hotbarLimit) setSelectedSlot(hotbarLimit);
            if (player instanceof ServerPlayer sp) {
                ClientboundSetPlayerInventoryPacket[] packets = new ClientboundSetPlayerInventoryPacket[9 - hotbarLimit];
                for (int i = hotbarLimit + 1; i < 9; i++) {
                    ballad$dropAndRemoveItem(i, true);
                    packets[i - (hotbarLimit + 1)] = createInventoryUpdatePacket(i);
                }
                for (ClientboundSetPlayerInventoryPacket packet : packets) {
                    if (packet != null) sp.connection.send(packet);
                }
            }

            if (hotbarLimit == -1 && equipment instanceof DynamicPlayerEquipment dynEq) dynEq.updateMainhand(false);
        }
        else if (old == -1) {
            setSelectedSlot(0);
            if (equipment instanceof DynamicPlayerEquipment dynEq) dynEq.updateMainhand(true);
        }
    }
    @Override
    public void ballad$resizeInventory(int size) {
        int old = inventoryLimit;
        resizeInventory(size);
        if (inventoryLimit < old && player instanceof ServerPlayer sp) {
            ClientboundSetPlayerInventoryPacket[] packets = new ClientboundSetPlayerInventoryPacket[36 - (inventoryLimit + 9)];
            for (int i = inventoryLimit + 9; i < 36; i++) {
                ballad$dropAndRemoveItem(i, false);
                packets[i - 8] = createInventoryUpdatePacket(i);
            }
            for (ClientboundSetPlayerInventoryPacket packet : packets){
                if (packet != null) sp.connection.send(packet);
            }
        }

    }
    @Override
    public void ballad$updateArmor(boolean[] armor) {
        if (equipment instanceof DynamicPlayerEquipment dynEq){
            dynEq.updateArmor(armor);
        }
    }
    @Override
    public void ballad$updateOffhand(boolean valid) {
        if (equipment instanceof DynamicPlayerEquipment dynEq){
            dynEq.updateOffhand(valid);
        }
    }

    private void ballad$dropAndRemoveItem(int index, boolean tossFromHand){
        ItemStack item = getWithoutContext(index);
        player.drop(item, !tossFromHand, tossFromHand);
        setWithoutContext(index, ItemStack.EMPTY);
    }
}
