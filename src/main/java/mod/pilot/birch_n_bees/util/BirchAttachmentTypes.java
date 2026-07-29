package mod.pilot.birch_n_bees.util;

import mod.pilot.birch_n_bees.ABOBAB;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class BirchAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ABOBAB.MOD_ID);


    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ItemStack>> ITEM_STACK_ATTACHMENT =
            ATTACHMENT_TYPES.register("item_stack_attachment", () -> AttachmentType.builder(() -> ItemStack.EMPTY).build());
}
