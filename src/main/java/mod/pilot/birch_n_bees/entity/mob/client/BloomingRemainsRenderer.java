package mod.pilot.birch_n_bees.entity.mob.client;

import com.geckolib.GeckoLibConstants;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.*;
import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer;
import com.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;
import com.geckolib.util.RenderUtil;
import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.pilot.birch_n_bees.entity.mob.BloomingRemainsEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.BiConsumer;

public class BloomingRemainsRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<BloomingRemainsEntity, @NonNull R> {
    public BloomingRemainsRenderer(EntityRendererProvider.Context context,
                                   GeoModel<BloomingRemainsEntity> model) {
        super(context, model);
        withRenderLayer(new ItemInHandGeoLayer<>(context,this, "right_hand", "left_hand"));
        withRenderLayer(new BloomingRemainsFlowerLayer<>(context,this));
    }

    public static DataTicket<Boolean> IS_AGGRESSIVE = DataTicket.create("is_aggressive", Boolean.class);
    public static DataTicket<Boolean> RIGHT_HANDED = DataTicket.create("right_handed", Boolean.class);
    public static DataTicket<ItemStack> MAIN_HAND_ITEM = DataTicket.create("main_hand_item", ItemStack.class);
    public static DataTicket<ItemStack> OFFHAND_ITEM = DataTicket.create("offhand_item", ItemStack.class);

    @Override
    public void addRenderData(@NonNull BloomingRemainsEntity animatable, @Nullable Void relatedObject,
                              @NonNull R renderState, float partialTick) {
        renderState.addGeckolibData(IS_AGGRESSIVE, animatable.isAggressive());
        ItemStack main = animatable.getMainHandItem(), offhand = animatable.getOffhandItem();
        renderState.addGeckolibData(MAIN_HAND_ITEM, main);
        renderState.addGeckolibData(OFFHAND_ITEM, offhand);

        boolean rightHanded = animatable.getMainArm() == HumanoidArm.RIGHT;
        renderState.addGeckolibData(RIGHT_HANDED, rightHanded);
    }

    @Override
    public void adjustModelBonesForRender(@NonNull RenderPassInfo<@NonNull R> renderPassInfo, @NonNull BoneSnapshots snapshots) {

        GeoModel<BloomingRemainsEntity> geoModel = getGeoModel();
        BoneSnapshot rightArm = snapshots.get("right_arm").orElse(null);
        BoneSnapshot leftArm = snapshots.get("left_arm").orElse(null);
        if (rightArm != null && leftArm != null) {
            //getGeckolibData can return null here so we need to wrap it in this weird ass way
            boolean aggressive = Boolean.TRUE.equals(renderPassInfo.getGeckolibData(IS_AGGRESSIVE));
            if (aggressive){
                boolean right = Boolean.TRUE.equals(renderPassInfo.getGeckolibData(RIGHT_HANDED));
                ItemStack main = renderPassInfo.getGeckolibData(MAIN_HAND_ITEM);
                if (main != null && main.getItem() instanceof BowItem) {
                    float mainRotY = 0.1F, offRotY = -0.5F;
                    float halfPi = (float)(Math.PI / 2f);

                    rightArm.setRotY(right ? mainRotY : offRotY);
                    leftArm.setRotY(right ? offRotY : mainRotY);
                    rightArm.setRotX(halfPi);
                    leftArm.setRotX(halfPi);
                }
            }
        }
    }

    public static final String FLOWER_BONE = "poppy";
    public class BloomingRemainsFlowerLayer<O> extends BlockAndItemGeoLayer<BloomingRemainsEntity, O, @NonNull R> {
        public static final DataTicket<RenderData> FLOWER_CONTENTS = DataTicket.create("blooming_remains_flower", new TypeToken<>() {});
        public BloomingRemainsFlowerLayer(EntityRendererProvider.Context context,
                                          GeoRenderer<BloomingRemainsEntity, O, @NonNull R> renderer) {
            super(context, renderer);
        }

        @Override
        protected @NonNull List<RenderData> getRelevantBones(@NonNull BloomingRemainsEntity animatable,
                                                             @Nullable O relatedObject,
                                                             @NonNull R renderState, float partialTick) {
            return null; //We do not use this method but we need to override it
        }

        @Override
        public void addRenderData(BloomingRemainsEntity animatable, @org.jspecify.annotations.Nullable O relatedObject, @NonNull R renderState, float partialTick) {
            renderState.addGeckolibData(FLOWER_CONTENTS, RenderData.block(FLOWER_BONE,
                    RenderUtil.createRenderStateForBlock(
                            animatable.getFlowerBlockState(), blockModelResolver
                    )));
        }

        @Override
        public void addPerBoneRender(RenderPassInfo<@NonNull R> renderPassInfo, BiConsumer<GeoBone, PerBoneRender<R>> consumer) {
            if (!renderPassInfo.willRender())
                return;

            final R renderState = renderPassInfo.renderState();
            final BakedGeoModel model = renderPassInfo.model();

            //Up-porting ItemInHandGeoLayer code and modifying as needed.
            // We don't need a for loop, we only render once
            if (renderState.hasGeckolibData(FLOWER_CONTENTS)){
                RenderData flowerData = renderState.getGeckolibData(FLOWER_CONTENTS);
                if (flowerData != null)
                    model.getBone(flowerData.boneName())
                        .ifPresentOrElse(bone ->
                                    flowerData.modelState().ifRight(blockState -> {
                                        if (!blockState.isEmpty()) {
                                            consumer.accept(bone,
                                                    (renderPassInfo1, bone2, renderTasks) ->
                                                submitBlockRender(renderPassInfo1.poseStack(), bone2, blockState, renderPassInfo1.renderState(), renderTasks, renderPassInfo.packedLight())
                                            );
                                        }
                                    }),
                                () -> GeckoLibConstants.LOGGER.error(
                                        "Unable to find bone for ItemArmorGeoLayer: {}, skipping",
                                        flowerData.boneName()));
            }
        }

        @Override
        protected void submitBlockRender(PoseStack poseStack, GeoBone bone, BlockModelRenderState blockState, R renderState, SubmitNodeCollector renderTasks, int packedLight) {
            poseStack.pushPose();
            poseStack.translate(-0.5f, 0, -0.5f);
            //poseStack.scale(0.5f, 0.5f, 0.5f);
            blockState.submit(poseStack, renderTasks, packedLight, OverlayTexture.NO_OVERLAY, renderState instanceof EntityRenderState entityState ? entityState.outlineColor : 0);
            poseStack.popPose();
        }
    }
}
