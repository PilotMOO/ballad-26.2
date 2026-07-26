package mod.pilot.birch_n_bees.util;

import com.mojang.serialization.Codec;
import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.items.unique.BuildableToolBase;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class BirchDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ABOBAB.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BuildableToolBase.ToolHead>> TOOL_HEAD =
            DATA_COMPONENTS.register("tool_head",
            () -> DataComponentType.<BuildableToolBase.ToolHead>builder().persistent(BuildableToolBase.ToolHead.CODEC)
                    .networkSynchronized(BuildableToolBase.ToolHead.STREAM_CODEC).cacheEncoding().build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> LAST_USE_DURABILITY =
            DATA_COMPONENTS.register("last_use_duration",
                    () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
}
