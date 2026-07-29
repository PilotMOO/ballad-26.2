package mod.pilot.birch_n_bees.blocks.block_entities.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mod.pilot.birch_n_bees.blocks.block_entities.WildflowerBasketBlockEntity;
import mod.pilot.birch_n_bees.util.BirchTags;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class WildflowerBasketRenderer
        implements BlockEntityRenderer<WildflowerBasketBlockEntity, WildflowerBasketRenderer.WildflowerBasketRenderState> {
    public final ItemModelResolver itemModelResolver;
    public WildflowerBasketRenderer(BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
    }
    @Override
    public @NonNull WildflowerBasketRenderState createRenderState() {
        return new WildflowerBasketRenderState();
    }

    @Override
    public void extractRenderState(@NonNull WildflowerBasketBlockEntity blockEntity, @NonNull WildflowerBasketRenderState state,
                                   float partialTicks, @NonNull Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(blockEntity, state, breakProgress);
        state.itemRenderState = new ItemStackRenderState();
        itemModelResolver.updateForTopItem(state.itemRenderState, (state.itemStack = blockEntity.heldStack), ItemDisplayContext.FIXED,
                blockEntity.getLevel(), null, (state.seed = (int)blockEntity.getBlockPos().asLong()));
        state.facing = blockEntity.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        state.random = blockEntity.getLevel().getRandom();
    }

    @Override
    public void submit(@NonNull WildflowerBasketRenderState state, @NonNull PoseStack poseStack,
                       @NonNull SubmitNodeCollector submitNodeCollector, @NonNull CameraRenderState cameraRenderState) {
        if (!state.itemRenderState.isEmpty()) {
            boolean tool = state.itemStack.is(BirchTags.Items.TOOL_RENDERSTATE_FOR_BASKET);
            int renderAmount = ItemClusterRenderState.getRenderedAmount(state.itemStack.count());

            poseStack.pushPose();
            poseStack.translate(tool ? 0.625 : 0.5,  tool ? 0.5 : 0.375, 0.5F);
            Direction direction = Direction.from2DDataValue(state.facing.get2DDataValue());
            float angle = -direction.toYRot();
            poseStack.mulPose(Axis.YP.rotationDegrees(angle));
            poseStack.mulPose(Axis.XP.rotationDegrees(-22.5F * (1f / renderAmount)));
            if (tool) poseStack.mulPose(Axis.ZN.rotationDegrees(45F));
            poseStack.scale(0.55F, 0.55F, 0.55F);
            submitMultipleFromCount(poseStack, submitNodeCollector, state.lightCoords,
                    state, renderAmount, state.random,
                    state.itemRenderState.getModelBoundingBox());
            //state.itemRenderState.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }


    public static class WildflowerBasketRenderState extends BlockEntityRenderState{
        public ItemStackRenderState itemRenderState;
        public ItemStack itemStack;
        public Direction facing;
        int seed;
        RandomSource random;
        public WildflowerBasketRenderState() {
            itemRenderState = new ItemStackRenderState();
            itemStack = ItemStack.EMPTY;
            facing = Direction.NORTH;
        }
    }

    //Stolen then modified the item entity renderer
    public static void submitMultipleFromCount(
            PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
            WildflowerBasketRenderState state, int renderAmount, RandomSource random, AABB modelBoundingBox
    ) {
        if (renderAmount != 0) {
            random.setSeed(state.seed);
            ItemStackRenderState item = state.itemRenderState;
            float modelDepth = (float)modelBoundingBox.getZsize();
            if (modelDepth > 0.0625F) {
                item.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, 0);

                for (int i = 1; i < renderAmount; i++) {
                    poseStack.pushPose();
                    float xo = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float yo = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float zo = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    poseStack.translate(xo, yo, zo);
                    item.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, 0);
                    poseStack.popPose();
                }
            } else {
                float offsetZ = modelDepth * 1.5F;
                poseStack.translate(0.0F, 0.0F, -(offsetZ * (renderAmount - 1) / 2.0F));
                item.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, 0);
                poseStack.translate(0.0F, 0.0F, offsetZ);

                for (int i = 1; i < renderAmount; i++) {
                    poseStack.pushPose();
                    float xo = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    float yo = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    poseStack.translate(xo, yo, 0.0F);
                    item.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, 0);
                    poseStack.popPose();
                    poseStack.translate(0.0F, 0.0F, offsetZ);
                }
            }
        }
    }
}
