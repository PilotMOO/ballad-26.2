package mod.pilot.birch_n_bees.entity.mob.client;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import mod.pilot.birch_n_bees.entity.mob.NestHeadEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class NestHeadRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<NestHeadEntity, R> {
    public NestHeadRenderer(EntityRendererProvider.Context context,
                            GeoModel<NestHeadEntity> model) {
        super(context, model);
    }

    @Override
    protected float getDeathMaxRotation(GeoRenderState renderState) {
        return 0f;
    }
}
