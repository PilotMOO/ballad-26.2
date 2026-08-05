package mod.pilot.birch_n_bees.mixins.common;

import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.DynamicInventory;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class PlayerInventoryInterceptor {
    @Redirect(method = "<init>", at = @At(value = "NEW", target = "(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/EntityEquipment;)Lnet/minecraft/world/entity/player/Inventory;"))
    private Inventory makeInventoryDynamic(Player player, EntityEquipment equipment){
        return new DynamicInventory(player, equipment);
    }
}
