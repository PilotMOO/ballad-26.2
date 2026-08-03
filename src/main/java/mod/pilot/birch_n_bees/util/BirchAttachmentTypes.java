package mod.pilot.birch_n_bees.util;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.DynamicInventoryToken;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class BirchAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ABOBAB.MOD_ID);

    public static final Supplier<AttachmentType<ItemStack>> ITEM_STACK_ATTACHMENT =
            ATTACHMENT_TYPES.register("item_stack_attachment", () -> AttachmentType.builder(() -> ItemStack.EMPTY).build());
    public static final Supplier<AttachmentType<DynamicInventoryToken>> INVENTORY_TOKEN_ATTACHMENT =
            ATTACHMENT_TYPES.register("inventory_token_attachment",
                    () -> AttachmentType.serializable(DynamicInventoryToken::new)
                            .sync(new DynamicInventoryToken.Syncer())
                            .build());
}
