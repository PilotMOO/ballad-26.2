package mod.pilot.birch_n_bees.mixins;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.gen.Invoker;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.world.inventory.Slot.class)
public interface SlotAccessor {
    @Invoker
    void callOnQuickCraft(ItemStack picked, int count);

    @Invoker
    void callOnSwapCraft(int count);

    @Invoker
    void callCheckTakeAchievements(ItemStack carried);
}
