package mod.pilot.birch_n_bees.entity.mob.client;

import com.geckolib.model.DefaultedEntityGeoModel;
import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.entity.mob.NestHeadEntity;
import net.minecraft.resources.Identifier;

public class NestHeadModel extends DefaultedEntityGeoModel<NestHeadEntity> {
    /*private static final ResourceLocation model = ResourceLocation.fromNamespaceAndPath(ABalladofBirchandBees.MOD_ID, "geo/entity/mob/splintering.geo.json");
    private static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(ABalladofBirchandBees.MOD_ID, "textures/entity/splintering.png");
    private static final ResourceLocation animation = ResourceLocation.fromNamespaceAndPath(ABalladofBirchandBees.MOD_ID, "animations/entity/mob/splintering.animation.json");*/

    public NestHeadModel() {
        super(Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "nesthead"));
    }
}
