package mod.pilot.birch_n_bees.mixins.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.DynamicInventory;
import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.DynamicInventoryToken;
import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.IPlayerInventoryResizable;
import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.LockableInventoryItemStackList;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Inventory.class)
public abstract class InventoryMixin implements Container, Nameable/*, IPlayerInventoryResizable*/ {
    @Final @Shadow private NonNullList<ItemStack> items;
    @Shadow @Final public Player player;
    @Shadow @Final private EntityEquipment equipment;

    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;withSize(ILjava/lang/Object;)Lnet/minecraft/core/NonNullList;"))
    private NonNullList<ItemStack> hijackItemListInit(NonNullList<ItemStack> original){
        Object self = this;
        if (self instanceof DynamicInventory dyn) {
            dyn.items = original;
            dyn.createWrapper();
        }
        return original;
    }

/*    @ModifyReturnValue(method = "getFreeSlot", at = @At("RETURN"))
    private int applyResizeContext_getFreeSlot(int original){
        if (original == -1 || !(items instanceof LockableInventoryItemStackList lockable)) return -1;
        else return lockable.getEffectiveIndex(original);
    }
    @ModifyReturnValue(method = "findSlotMatchingItem", at = @At("RETURN"))
    private int applyResizeContext_findSlotMatchingItem(int original){
        if (original == -1 || !(items instanceof LockableInventoryItemStackList lockable)) return -1;
        else return lockable.getEffectiveIndex(original);
    }
    @ModifyReturnValue(method = "findSlotMatchingCraftingIngredient", at = @At("RETURN"))
    private int applyResizeContext_findSlotMatchingCraftingIngredients(int original){
        if (original == -1 || !(items instanceof LockableInventoryItemStackList lockable)) return -1;
        else return lockable.getEffectiveIndex(original);
    }
    @ModifyReturnValue(method = "getSlotWithRemainingSpace", at = @At("RETURN"))
    private int applyResizeContext_getSlotWithRemainingSpace(int original){
        if (original == -1 || !(items instanceof LockableInventoryItemStackList lockable)) return -1;
        else if (original == 40){
            if (DynamicInventoryToken.get(player).offhand) return 40;
            else return -1;
        }
        else return lockable.getEffectiveIndex(original);
    }*/

    /*@Override
    public void ballad$resizeHotbar(int size) {
        if (items instanceof LockableInventoryItemStackList lockable) {
            int old = lockable.hotbarLimit;
            lockable.resizeHotbar(size);
            if (lockable.hotbarLimit < old){
                for (int i = lockable.hotbarLimit + 1; i < 9; i++){
                    ballad$dropAndRemoveItem(i);
                }
            }
        }
    }
    @Override
    public void ballad$resizeInventory(int size) {
        if (items instanceof LockableInventoryItemStackList lockable) {
            int old = lockable.inventoryLimit;
            lockable.resizeInventory(size);
            if (lockable.inventoryLimit < old) {
                for (int i = lockable.inventoryLimit + 10; i < 36; i++) {
                    ballad$dropAndRemoveItem(i);
                }
            }
        }
    }
    @Override
    public void ballad$updateArmor(boolean[] armor) {
        if (!armor[0]) ballad$dropAndRemoveEquipment(EquipmentSlot.HEAD);
        if (!armor[1]) ballad$dropAndRemoveEquipment(EquipmentSlot.CHEST);
        if (!armor[2]) ballad$dropAndRemoveEquipment(EquipmentSlot.LEGS);
        if (!armor[3]) ballad$dropAndRemoveEquipment(EquipmentSlot.FEET);
    }
    @Override
    public void ballad$updateOffhand(boolean valid) {
        if (!valid) ballad$dropAndRemoveEquipment(EquipmentSlot.OFFHAND);
    }


    @Unique private void ballad$dropAndRemoveItem(int index){
        if (items instanceof LockableInventoryItemStackList lockable) {
            ItemStack item = lockable.list.get(index);
            player.drop(item, true, false);
            lockable.list.set(index, ItemStack.EMPTY);
        }
    }
    @Unique private void ballad$dropAndRemoveEquipment(EquipmentSlot equipmentSlot){
        ItemStack item = equipment.get(equipmentSlot);
        player.drop(item, true, false);
        equipment.set(equipmentSlot, ItemStack.EMPTY);
    }*/
}
