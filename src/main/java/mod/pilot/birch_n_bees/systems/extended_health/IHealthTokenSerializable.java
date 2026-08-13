package mod.pilot.birch_n_bees.systems.extended_health;

import mod.pilot.birch_n_bees.systems.extended_health.ailments.AilmentInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;

/**
 * An interface for defining an object as serializable and syncable within a {@link HealthToken}.
 * Exposes the Serializer for any implementing object in a generic context along with helper methods for simplifying serialization.
 * @param <P> the Player object the implementing object manages,
 *           either {@link net.minecraft.client.player.AbstractClientPlayer} for Client-side implementations
 *           or {@link net.minecraft.server.level.ServerPlayer} for Server-side implementations
 * @param <O> a reference to the implementing object itself, for use in ensuring the returned Serializers are within bound.
 *           <b>Make sure that the class defined for this generic parameter is the same or a super of the implementing class(es)</b>.
 *           Otherwise, some helper methods WILL throw errors, as it expects {@code this} to be an instance of {@link O}
 */
@SuppressWarnings("unchecked")
public interface IHealthTokenSerializable<P extends Player, O extends IHealthTokenSerializable<P, O>> {
    /**Helper method to serialize this instance to save to file*/
    default void serialize(String prepend, ValueOutput output){
        getSerializer().serialize((O)this, prepend, output);
    }
    /**Helper method to deserialize this instance for parsing from save file*/
    default void deserialize(String prepend, ValueInput input){
        getSerializer().deserialize((O)this, prepend, input);
    }

    default void write(RegistryFriendlyByteBuf buf){
        getSerializer().write((O)this, buf);
    }

    /**
     * Gets the {@link Serializer} compatible for this object.
     * <p>All implementations of this method should be {@code final}, defer to {@link getComplexSerializer()} to implement a custom serializer.</p>
     * @return the {@link Serializer} to save this instance to file or write data to sync
     */
    default Serializer<P, O> getSerializer(){
        if (hasComplexData()) return getComplexSerializer();
        else return getDefaultSerializer();
    }
    /**
     * Returns the associated {@code DefaultSerializer} for this object.
     * <p>All accessible implementations of this method should be {@code final},
     * defer to {@link getComplexSerializer()} to implement a custom serializer.</p>
     * @return the associated {@code DefaultSerializer} for this instance
     */
    Serializer<P, O> getDefaultSerializer();
    /**If this given {@link IHealthTokenSerializable} requires a custom serializer. Default {@code false}
     * @return if this given AilmentInstance type requires a custom serializer*/
    default boolean hasComplexData(){return false;}
    /**Retrieves the custom {@link Serializer} for this object.
     * <b>This method returns a null value by default, despite the {@code @NonNull} annotation.</b>
     * Validate if this instance has a custom serializer via {@link hasComplexData()}.
     * <p>For objects that require a custom serializer due to implementing and saving additional data,
     * override this method to return your custom {@link Serializer}.
     * <b>Make sure to override {@link hasComplexData()} to return {@code true},
     * otherwise your serializer will not be used.</b></p>
     * @return the custom serializer for this object, {@code null} for default implementations
     */
    default @NonNull Serializer<P, O> getComplexSerializer(){return null;}
}
