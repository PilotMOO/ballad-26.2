package mod.pilot.birch_n_bees.entity.mob.client;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import mod.pilot.birch_n_bees.entity.mob.SplinteringEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class SplinteringRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<SplinteringEntity, R> {
    public SplinteringRenderer(EntityRendererProvider.Context context,
                               GeoModel<SplinteringEntity> model) {
        super(context, model);
    }
}
