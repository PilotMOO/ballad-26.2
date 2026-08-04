package mod.pilot.birch_n_bees.systems.dynamic_player_inventory;

import com.google.common.collect.Lists;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LockableInventoryItemStackList extends NonNullList<ItemStack> {
    public LockableInventoryItemStackList(int size, ItemStack defaultValue) {
        super((this.list = createList(size, defaultValue)), defaultValue);
        lockedSize = size;
        this.defaultValue = defaultValue;
    }

    public static List<ItemStack> createList(int count, ItemStack defaultValue) {
        ItemStack[] array = new ItemStack[count];
        Arrays.fill(array, defaultValue);
        return Lists.newArrayList(array);
    }

    public void resizeInventory(int size) {
        size = Math.clamp(size, 0, 27);
        inventoryLimit = size - 1;
        lockedSize = hotbarLimit + inventoryLimit + 2; //+2 because both limits are zero-context, but the locked size isn't
    }
    public void resizeHotbar(int size) {
        size = Math.clamp(size, 0, 9);
        hotbarLimit = size - 1;
        lockedSize = hotbarLimit + inventoryLimit + 2;
    }
    public List<ItemStack> list;
    public int lockedSize; //9 for hotbar, 27 for inv. total 36
    public int inventoryLimit = 26; //zero-context
    public int hotbarLimit = 8; /**/
    public ItemStack defaultValue;
    @Override
    public @NonNull ItemStack get(int index) {
        ItemStack item = this.list.get(getEffectiveIndex(index));
        //System.out.println("getting index " + index +", modified to " + getEffectiveIndex(index) + " because hot, inv, lock is [" + hotbarLimit + ", " + inventoryLimit + ", " + lockedSize + "], returned " + item);
        return this.list.get(getEffectiveIndex(index));
    }
    @Override
    public @NonNull ItemStack set(int index, @NonNull ItemStack element) {
        Objects.requireNonNull(element);
        System.out.println("setting index " + index +" (modified to " + getEffectiveIndex(index) + ") to " + element + " | hot, inv, lock is [" + hotbarLimit + ", " + inventoryLimit + ", " + lockedSize + "]");
        ItemStack set = this.list.set(getEffectiveIndex(index), element);
        System.out.println(this);
        return set;
    }
    @Override
    public void add(int index, @NonNull ItemStack element) {
        Objects.requireNonNull(element);
        this.list.add(getEffectiveIndex(index), element);
    }

    public int getEffectiveIndex(int index){
        if (index > hotbarLimit) index += (8 - hotbarLimit);
        return index;
    }

    @Override
    public @NonNull ItemStack remove(int index) {
        return this.list.remove(index);
    }

    @Override
    public int size() {
        //System.out.println("lockable inventory thing locked size " +lockedSize);
        return lockedSize;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int count = list.size();
        sb.append("LockableInventoryItemStackList{").append(lockedSize).append("/").append(count).append("}[HOTBAR{").append(hotbarLimit + 1).append("/").append(9).append("} ->");
        for (int i = 0; i < count; i++) {
            if (i == 9) sb.append("| INVENTORY{").append(inventoryLimit + 1).append("/").append(27).append("} -> ");

            if ((i < 9 && i > hotbarLimit) || (i > inventoryLimit + 9)) sb.append("<!!LOCKED!!> ");
            sb.append("'").append(list.get(i)).append(" @[").append(i).append("]'");
            if (i < count - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
