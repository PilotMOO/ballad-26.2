package mod.pilot.birch_n_bees.mixins;

import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.world.entity.player.Inventory.class)
public interface InventoryAccessor {
    @Invoker
    boolean callHasRemainingSpaceForItem(ItemStack slotItemStack, ItemStack newItemStack);

    @Invoker
    int callAddResource(ItemStack itemStack);
    @Invoker
    int callAddResource(int slot, ItemStack itemStack);

    @Accessor
    EntityEquipment getEquipment();
}
