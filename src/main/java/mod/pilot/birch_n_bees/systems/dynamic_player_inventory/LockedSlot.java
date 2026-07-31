package mod.pilot.birch_n_bees.systems.dynamic_player_inventory;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.mixins.SlotAccessor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class LockedSlot extends Slot {
    private static final Identifier greenquisitor
            = Identifier.withDefaultNamespace("container/slot/potion");

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
    public boolean mayPlace(ItemStack itemStack) {
        return !locked && referencedSlot.mayPlace(itemStack);
    }
    @Override
    public boolean mayPickup(Player player) {
        return !locked && referencedSlot.mayPickup(player);
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
    public void set(ItemStack itemStack) {
        if (!locked) referencedSlot.set(itemStack);
    }

    @Override
    public @Nullable Identifier getNoItemIcon() {
        return Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "greenquisitor");
    }

    @Override
    public ItemStack remove(int amount) {
        return locked ? ItemStack.EMPTY : referencedSlot.remove(amount);
    }

    @Override
    public boolean isActive() {
        return true; /*!locked && referencedSlot.isActive();*/
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
