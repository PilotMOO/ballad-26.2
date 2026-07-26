package mod.pilot.birch_n_bees.entity;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.entity.mob.BloomingRemainsEntity;
import mod.pilot.birch_n_bees.entity.mob.NestHeadEntity;
import mod.pilot.birch_n_bees.entity.mob.SplinteringEntity;
import mod.pilot.birch_n_bees.entity.projectiles.OvergrownArrowEntity;
import mod.pilot.birch_n_bees.entity.projectiles.SplinterProjectileEntity;
import mod.pilot.birch_n_bees.entity.projectiles.WildflowerPopperProjectileEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BirchEntities {
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(ABOBAB.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<SplinterProjectileEntity>> SPLINTER_PROJECTILE =
            ENTITIES.registerEntityType("splinter", SplinterProjectileEntity::new, MobCategory.MISC,
                    (builder -> builder.sized(.5f, .5f)));

    public static final DeferredHolder<EntityType<?>, EntityType<WildflowerPopperProjectileEntity>> WILDFLOWER_POPPER_PROJECTILE =
            ENTITIES.registerEntityType("wildflower_popper", WildflowerPopperProjectileEntity::new, MobCategory.MISC,
                    (builder -> builder.sized(.3f, .3f)));

    public static final DeferredHolder<EntityType<?>, EntityType<OvergrownArrowEntity>> OVERGROWN_ARROW =
            ENTITIES.registerEntityType("overgrown_arrow", OvergrownArrowEntity::new, MobCategory.MISC,
                    (builder -> builder.sized(.5f, .5f)));

    //Entities
    public static final DeferredHolder<EntityType<?>, EntityType<SplinteringEntity>> SPLINTERING =
            ENTITIES.registerEntityType("splintering", SplinteringEntity::new, MobCategory.MONSTER,
                    (builder -> builder.sized(.6f, 2.25f)));
    public static final DeferredHolder<EntityType<?>, EntityType<NestHeadEntity>> NESTHEAD =
            ENTITIES.registerEntityType("nesthead", NestHeadEntity::new, MobCategory.MONSTER,
                    (builder -> builder.sized(.75f, 2.0f)));
    public static final DeferredHolder<EntityType<?>, EntityType<BloomingRemainsEntity>> BLOOMING_REMAINS =
            ENTITIES.registerEntityType("blooming_remains", BloomingRemainsEntity::new, MobCategory.MONSTER,
                    (builder -> builder.sized(.6f, 1.9f)));
}
