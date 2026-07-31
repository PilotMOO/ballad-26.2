package mod.pilot.birch_n_bees.mixins.common;

import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.LockedSlot;
import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.mixin_interfaces.IServerPlayerReferenceHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(AbstractContainerMenu.class)
@Implements(@Interface(iface = IServerPlayerReferenceHolder.class, prefix = "playerRef$"))
public abstract class ContainerMenuRestrictionMixin{

    public @Nullable ServerPlayer playerRef$getReference(){
        return playerRef$serverPlayer;
    }
    public void playerRef$reference(ServerPlayer player){
        playerRef$serverPlayer = player;
    }
    @Unique
    public @Nullable ServerPlayer playerRef$serverPlayer;



    /*@Inject(method = "addInventoryExtendedSlots", at = @At("HEAD"), cancellable = true)
    private void applyExtendedInventoryRestrictions(Container inventory, int left, int top, CallbackInfo ci){
        ci.cancel();
    }*/

/*    @ModifyVariable(method = "addSlot", at = @At("HEAD"), argsOnly = true, name = "slot")
    private Slot lockAddedSlots(Slot slot){
        AbstractContainerMenu self = (AbstractContainerMenu)(Object)this;
        if (self instanceof InventoryMenu inv){
            *//*DynamicPlayerInventoryManager.DynamicInventoryToken invToken;
            Player player;
            if (DynamicPlayerInventoryManager.isServerSide()){
                player = playerRef$getReference();
            } else {
                player = Minecraft.getInstance().player;
            }
            if (player != null){
                invToken = DynamicPlayerInventoryManager.getOrCreatePlayerToken(player);
                LockedSlot lockedSlot = LockedSlot.wrap(slot);
                lockedSlot.locked = invToken.shouldLock(slot.getSlotIndex());
                return lockedSlot;
            }*//*
            System.out.println("WRAPPING PLAYER INVENTORY SLOTS (fuck you)");
            return LockedSlot.wrap(slot);
        }
        return slot;
    }*/

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

    @Inject(method = "removed", at = @At("HEAD"))
    private void removeListener(Player player, CallbackInfo ci){
        if (player == playerRef$getReference()) playerRef$reference(null);
    }
}
