package mod.pilot.birch_n_bees.systems.extended_health;

import mod.pilot.birch_n_bees.systems.extended_health.ailments.AilmentInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A baseclass for serializers that handle the serialization of objects related to {@link HealthToken}
 * <p>Refer to the {@code DefaultSerializer} within the related superclasses if your object does not use custom data.</p>
 * @param <O> the type of {@link IHealthTokenSerializable} this serializer handles
 */
public abstract class Serializer<P extends Player, O extends IHealthTokenSerializable<P, O>>{
    /**
     * Writes all the relevant data of the first argument to the supplied {@link ValueOutput} argument for serialization.
     * <p><b>When implementing this method, make sure to concatenate the 2nd string argument to the FRONT
     * of each of the string IDs when putting data.</b>
     * Refer to the default implementation of your related serializer for reference.</p>
     * @param instance the {@link IHealthTokenSerializable} to get the data from
     * @param prepend the unique string I.D. to prepend to each of the data string arguments when assigning data to the {@link ValueOutput}
     * @param output the {@link ValueOutput} to write data to
     */
    public abstract void serialize(O instance, String prepend, @NonNull ValueOutput output);

    /**
     * Reads all relevant data of the {@link ValueInput} and assigns the data to the default {@link IHealthTokenSerializable} object for deserialization.
     * <p>Make sure to prepend each string ID with the supplied string prepend in order to access the correct data.
     * Refer to your custom implementation of {@link Serializer#serialize(IHealthTokenSerializable, String, ValueOutput)}
     * or the default implementation in your respective object for reference.</p>
     * @param instance the {@link IHealthTokenSerializable} to assign the data to
     * @param prepend the unique string I.D. to prepend to each of the data string arguments when reading data from the {@link ValueInput}
     * @param input the {@link ValueInput} to read data from
     */
    public abstract void deserialize(O instance, String prepend, @NonNull ValueInput input);

    /**
     * Writes all the relevant data of the first argument to the supplied {@link RegistryFriendlyByteBuf} for syncing.
     * @param instance the {@link IHealthTokenSerializable} to get the relevant data
     * @param buf the {@link RegistryFriendlyByteBuf} to write data to
     */
    public abstract void write(O instance, @NonNull RegistryFriendlyByteBuf buf);

    /**
     * Reads all relevant data of the {@link RegistryFriendlyByteBuf} and assigns the data to the default {@link IHealthTokenSerializable} object for syncing.
     * <p>Because java generics kind of suck, you may have to do an unchecked cast to return the correct generic</p>
     * @param holder the {@link IAttachmentHolder} the {@link IHealthTokenSerializable} belongs to
     * @param newInstance the {@link IHealthTokenSerializable} to assign the data to
     * @param buf the {@link RegistryFriendlyByteBuf} to get the data from
     * @param oldInstance an optional {@link IHealthTokenSerializable} that refers to the old value that will be replaced with the new value.
     *                    <b>May be null, but can be used to preserve information that would otherwise not be synced
     *                    or lost when syncing</b>
     * @return the 2nd {@link AilmentInstance} argument with the synced data now applied
     */
    public abstract O read(IAttachmentHolder holder, O newInstance,
                                            @NonNull RegistryFriendlyByteBuf buf, @Nullable O oldInstance);
}
