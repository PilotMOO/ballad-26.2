package mod.pilot.birch_n_bees.mixins.common;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.DynamicInventoryToken;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.waypoints.WaypointTransmitter;
import net.neoforged.neoforge.common.extensions.ILivingEntityExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class PlayerCanUseEquipmentSlotMixin extends Entity implements Attackable, WaypointTransmitter, ILivingEntityExtension {
    protected PlayerCanUseEquipmentSlotMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }


    @ModifyReturnValue(method = "canUseSlot", at = @At("RETURN"))
    private boolean validateEquipmentSlotViaToken(boolean original, EquipmentSlot slot) {
        Object self = this;
        if (self instanceof Player player){
            DynamicInventoryToken token = DynamicInventoryToken.get(player);
            if (slot.equals(EquipmentSlot.OFFHAND)) return token.offhand;
            if (slot.equals(EquipmentSlot.HEAD)) return token.armor[0];
            if (slot.equals(EquipmentSlot.CHEST)) return token.armor[1];
            if (slot.equals(EquipmentSlot.LEGS)) return token.armor[2];
            if (slot.equals(EquipmentSlot.FEET)) return token.armor[3];
        }
        return original;
    }
}
