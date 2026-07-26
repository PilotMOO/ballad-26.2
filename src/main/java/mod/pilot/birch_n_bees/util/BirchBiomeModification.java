package mod.pilot.birch_n_bees.util;


import com.mojang.serialization.MapCodec;
import mod.pilot.birch_n_bees.entity.BirchEntities;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import org.jetbrains.annotations.NotNull;

public class BirchBiomeModification implements BiomeModifier {
    public static final MapCodec<BirchBiomeModification> CODEC = MapCodec.unit(BirchBiomeModification::new);

    public MapCodec<BirchBiomeModification> codecWrapper() {
        return CODEC;
    }

    @Override
    public @NotNull MapCodec<? extends BiomeModifier> codec() {
        return CODEC;
    }

    @Override
    public void modify(@NotNull Holder<Biome> biome, @NotNull Phase phase, ModifiableBiomeInfo.BiomeInfo.@NotNull Builder builder) {
        if (phase != Phase.ADD) return;
        if (biome.is(Tags.Biomes.IS_BIRCH_FOREST)) {
            builder.getMobSpawnSettings()
                    .getSpawner(MobCategory.MONSTER)
                    .add(new MobSpawnSettings.SpawnerData(BirchEntities.SPLINTERING.get(), 1, 3), 150);
            builder.getMobSpawnSettings()
                    .getSpawner(MobCategory.MONSTER)
                    .add(new MobSpawnSettings.SpawnerData(BirchEntities.NESTHEAD.get(), 1, 2), 175);
            builder.getMobSpawnSettings()
                    .getSpawner(MobCategory.MONSTER)
                    .add(new MobSpawnSettings.SpawnerData(BirchEntities.BLOOMING_REMAINS.get(), 1, 3), 135);
        }
    }
}
