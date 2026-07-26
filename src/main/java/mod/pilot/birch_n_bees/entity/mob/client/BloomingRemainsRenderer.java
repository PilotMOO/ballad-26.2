package mod.pilot.birch_n_bees.entity.mob.client;

import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;
import mod.pilot.birch_n_bees.entity.mob.BloomingRemainsEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class BloomingRemainsRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<BloomingRemainsEntity, @NonNull R> {
    public BloomingRemainsRenderer(EntityRendererProvider.Context context,
                                   GeoModel<BloomingRemainsEntity> model) {
        super(context, model);
        withRenderLayer(new ItemInHandGeoLayer<>(context,this, "right_hand", "left_hand"));
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
}
