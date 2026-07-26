package mod.pilot.birch_n_bees.entity.projectiles.client;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.entity.projectiles.SplinterProjectileEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class SplinterProjectileRenderer extends ArrowRenderer<SplinterProjectileEntity, ArrowRenderState> {
    public SplinterProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }


    private static final Identifier texture =
            Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "textures/entity/splinter_projectile.png");
    @Override
    protected @NotNull Identifier getTextureLocation(@NotNull ArrowRenderState arrowRenderState) {
        return texture;
    }

    @Override
    public @NotNull ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }
}
