package mod.pilot.birch_n_bees.systems.dynamic_player_inventory;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.mixins.SlotAccessor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class LockedSlot extends Slot {
    private static final Identifier greenquisitor
            = Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "greenquisitor");

    public LockedSlot(Slot slot) {
        super(slot.container, slot.getSlotIndex(), slot.x, slot.y);
        this.referencedSlot = slot;
        locked = true;
    }

    public static LockedSlot wrap(Slot slot){
        return new LockedSlot(slot);
    }
    public boolean locked;
    public Slot referencedSlot;

    @Override
    protected void onQuickCraft(ItemStack picked, int count) {
        if (!locked){
            ((SlotAccessor)referencedSlot).callOnQuickCraft(picked, count);
        }
    }
    @Override
    protected void onSwapCraft(int count) {
        if (!locked){
            ((SlotAccessor)referencedSlot).callOnSwapCraft(count);
        }
    }
    @Override
    protected void checkTakeAchievements(ItemStack carried) {
        if (!locked){
            ((SlotAccessor)referencedSlot).callCheckTakeAchievements(carried);
        }
    }

    @Override
    public void onTake(Player player, ItemStack carried) {
        if (!locked) referencedSlot.onTake(player, carried);
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        return !locked && referencedSlot.mayPlace(itemStack);
    }

    @Override
    public @NonNull ItemStack getItem() {
        return locked ? ItemStack.EMPTY : referencedSlot.getItem();
    }
    @Override
    public boolean hasItem() {
        return !locked && referencedSlot.hasItem();
    }

    @Override
    public void setByPlayer(ItemStack itemStack) {
        if (!locked) referencedSlot.setByPlayer(itemStack);
    }
    @Override
    public void setByPlayer(ItemStack itemStack, ItemStack previous) {
        if (!locked) referencedSlot.setByPlayer(itemStack, previous);
    }

    @Override
    public void set(ItemStack itemStack) {
        if (!locked) referencedSlot.set(itemStack);
    }

    @Override
    public void setChanged() {
        if (!locked) referencedSlot.setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return !locked ? referencedSlot.getMaxStackSize() : super.getMaxStackSize();
    }

    @Override
    public int getMaxStackSize(ItemStack itemStack) {
        return !locked ? referencedSlot.getMaxStackSize() : super.getMaxStackSize(itemStack);
    }

    @Override
    public @Nullable Identifier getNoItemIcon() {
        return locked ? greenquisitor : referencedSlot.getNoItemIcon();
    }

    @Override
    public ItemStack remove(int amount) {
        return locked ? ItemStack.EMPTY : referencedSlot.remove(amount);
    }
    @Override
    public boolean mayPickup(Player player) {
        return !locked && referencedSlot.mayPickup(player);
    }

    @Override
    public boolean isActive() {
        return true; /*!locked && referencedSlot.isActive();*/
    }

    @Override
    public int getSlotIndex() {
        return !locked ? referencedSlot.getSlotIndex() : super.getSlotIndex();
    }

    @Override
    public Slot setBackground(Identifier sprite) {
        if (locked) super.setBackground(sprite);
        else referencedSlot.setBackground(sprite);
        return this;
    }

    @Override
    public Optional<ItemStack> tryRemove(int amount, int maxAmount, Player player) {
        return !locked ? referencedSlot.tryRemove(amount, maxAmount, player) : Optional.empty();
    }

    @Override
    public ItemStack safeTake(int amount, int maxAmount, Player player) {
        return !locked ? referencedSlot.safeTake(amount, maxAmount, player) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack safeClone(Player player) {
        return !locked ? referencedSlot.safeClone(player) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack safeInsert(ItemStack stack) {
        return !locked ? referencedSlot.safeInsert(stack) : stack;
    }

    @Override
    public ItemStack safeInsert(ItemStack inputStack, int inputAmount) {
        return !locked ? referencedSlot.safeInsert(inputStack, inputAmount) : inputStack;
    }

    @Override
    public boolean allowModification(Player player) {
        return !locked && referencedSlot.allowModification(player);
    }

    @Override
    public int getContainerSlot() {
        return !locked ? referencedSlot.getSlotIndex() : super.getContainerSlot();
    }

    @Override
    public boolean isHighlightable() {
        return !locked && referencedSlot.isHighlightable();
    }
    @Override
    public boolean isFake() {
        return locked || referencedSlot.isFake();
    }
}
