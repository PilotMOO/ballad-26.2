package mod.pilot.birch_n_bees.entity.projectiles.client;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.entity.projectiles.OvergrownArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class OvergrownArrowRenderer extends ArrowRenderer<OvergrownArrowEntity, ArrowRenderState> {
    public static final Identifier RESOURCE =
            Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID,
                    "textures/entity/overgrown_arrow.png");

    public OvergrownArrowRenderer(EntityRendererProvider.Context p_173917_) {
        super(p_173917_);
    }

    @Override
    protected @NonNull Identifier getTextureLocation(@NonNull ArrowRenderState arrowRenderState) {
        return RESOURCE;
    }

    @Override
    public @NotNull ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }
}
