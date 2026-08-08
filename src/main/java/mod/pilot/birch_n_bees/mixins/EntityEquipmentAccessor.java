package mod.pilot.birch_n_bees.mixins;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.EnumMap;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.world.entity.EntityEquipment.class)
public interface EntityEquipmentAccessor {
    @Accessor
    EnumMap<EquipmentSlot, ItemStack> getItems();
}
