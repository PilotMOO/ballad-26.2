package mod.pilot.birch_n_bees.events;


import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.entity.BirchEntities;
import mod.pilot.birch_n_bees.entity.mob.BloomingRemainsEntity;
import mod.pilot.birch_n_bees.entity.mob.NestHeadEntity;
import mod.pilot.birch_n_bees.entity.mob.SplinteringEntity;
import mod.pilot.birch_n_bees.items.BirchItems;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.DefaultDataComponentsBoundEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

@EventBusSubscriber(modid = ABOBAB.MOD_ID)
public class BalladEventBus {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event){
        event.put(BirchEntities.SPLINTERING.get(), SplinteringEntity.createAttributes().build());
        event.put(BirchEntities.NESTHEAD.get(), NestHeadEntity.createAttributes().build());
        event.put(BirchEntities.BLOOMING_REMAINS.get(), BloomingRemainsEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerMobSpawningRules(RegisterSpawnPlacementsEvent event){
        event.register(BirchEntities.SPLINTERING.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.WORLD_SURFACE, SplinteringEntity::splinteringSpawnCheck, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(BirchEntities.NESTHEAD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.WORLD_SURFACE, NestHeadEntity::nestHeadSpawnCheck, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(BirchEntities.BLOOMING_REMAINS.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.WORLD_SURFACE, BloomingRemainsEntity::bloomingRemainsSpawnCheck, RegisterSpawnPlacementsEvent.Operation.AND);
    }
}
