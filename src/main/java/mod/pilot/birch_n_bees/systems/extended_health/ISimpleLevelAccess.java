package mod.pilot.birch_n_bees.systems.extended_health;

import mod.pilot.birch_n_bees.systems.extended_health.ailments.AilmentInstance;
import net.minecraft.world.level.Level;

/**
 * A simple little interface for accessing the Level from possible non-standard implementors of
 * {@link net.neoforged.neoforge.attachment.IAttachmentHolder} who wishes to use any dist-conscious attachments,
 * like {@link AilmentInstance}. Goes unused in native code due to all relevant attachments being used exclusively on Players
 */
public interface ISimpleLevelAccess {
    Level level();
    default boolean isClient(){
        return level().isClientSide();
    }
    default boolean isServer(){
        return !isClient();
    }
}
