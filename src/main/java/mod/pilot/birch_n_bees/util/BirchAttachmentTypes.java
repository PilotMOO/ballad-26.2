package mod.pilot.birch_n_bees.util;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.DynamicInventoryToken;
import mod.pilot.birch_n_bees.systems.extended_health.HealthToken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

public class BirchAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ABOBAB.MOD_ID);


    public static final Supplier<AttachmentType<DynamicInventoryToken>> INVENTORY_TOKEN_ATTACHMENT =
            ATTACHMENT_TYPES.register("inventory_token_attachment",
                    () -> AttachmentType.serializable(DynamicInventoryToken::new)
                            .sync(new DynamicInventoryToken.Syncer())
                            .copyOnDeath()
                            .build());

    public static final Supplier<AttachmentType<Integer>> ITEM_STACK_COOK_TIME_ATTACHMENT =
            ATTACHMENT_TYPES.register("item_stack_cook_time",
                    () -> AttachmentType.builder(() -> 0).serialize(new IAttachmentSerializer<>() {
                        @Override
                        public @NonNull Integer read(IAttachmentHolder holder, ValueInput input) {
                            return input.getIntOr("item_stack_cook_time", 0);
                        }
                        @Override
                        public boolean write(Integer attachment, ValueOutput output) {
                            output.putInt("item_stack_cook_time", attachment);
                            return attachment != 0;
                        }
                    }).build());

    @Deprecated
    public static final Supplier<AttachmentType<HealthToken>> HEALTH_TOKEN_ATTACHMENT =
            ATTACHMENT_TYPES.register("health_token_attachment",
                    () -> AttachmentType.serializable(
                            (holder) -> new HealthToken(holder instanceof Player p ? p : null))
                            .sync(new HealthToken.Syncer())
                            .build());
}
