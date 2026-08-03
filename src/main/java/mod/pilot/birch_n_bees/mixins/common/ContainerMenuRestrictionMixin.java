package mod.pilot.birch_n_bees.mixins.common;

import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.LockedSlot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;


@Mixin(AbstractContainerMenu.class)
public abstract class ContainerMenuRestrictionMixin{
    @ModifyVariable(method = "addSlot", at = @At("HEAD"), argsOnly = true, name = "slot")
    private Slot lockAddedSlots(Slot slot){
        if (slot instanceof LockedSlot) return slot;
        else if (slot instanceof ArmorSlot || slot.getSlotIndex() == 40) return LockedSlot.wrap(slot);
        return slot;
    }

    @ModifyArg(method = "addInventoryHotbarSlots", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;addSlot(Lnet/minecraft/world/inventory/Slot;)Lnet/minecraft/world/inventory/Slot;"))
    private Slot hotbarLocking(Slot slot){
        return LockedSlot.wrap(slot);
    }
    @ModifyArg(method = "addInventoryExtendedSlots", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;addSlot(Lnet/minecraft/world/inventory/Slot;)Lnet/minecraft/world/inventory/Slot;"))
    private Slot inventoryLocking(Slot slot){
        return LockedSlot.wrap(slot);
    }

}
