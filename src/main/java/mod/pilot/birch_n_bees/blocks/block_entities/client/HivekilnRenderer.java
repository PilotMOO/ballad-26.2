package mod.pilot.birch_n_bees.blocks.block_entities.client;

import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.*;
import com.google.common.reflect.TypeToken;
import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.blocks.BirchBlocks;
import mod.pilot.birch_n_bees.blocks.block_entities.HivekilnBlockEntity;
import mod.pilot.birch_n_bees.blocks.unique.HivekilnBlock;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class HivekilnRenderer extends GeoBlockRenderer<HivekilnBlockEntity, HivekilnRenderer.RenderState> {
    public static final DataTicket<Integer> FUEL = DataTicket.create("hivekiln_fuel", new TypeToken<>() {});

    public final ItemModelResolver itemModelResolver;
    public HivekilnRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new Model());
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public void addRenderData(@NonNull HivekilnBlockEntity animatable, @Nullable Void relatedObject,
                              RenderState renderState, float partialTick) {
        BlockEntityRenderState.extractBase(animatable, renderState, null);
        renderState.itemRenderState = new ItemStackRenderState();
        itemModelResolver.updateForTopItem(renderState.itemRenderState,
                (renderState.itemStack = animatable.heldStack), ItemDisplayContext.FIXED,
                animatable.getLevel(), null, (renderState.seed = (int)animatable.getBlockPos().asLong()));
        renderState.facing = animatable.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        renderState.random = animatable.getLevel().getRandom();
        BlockState bState = animatable.getLevel().getBlockState(animatable.getBlockPos());
        if (bState.is(BirchBlocks.HIVEKILN)) renderState.addGeckolibData(FUEL, bState.getValue(HivekilnBlock.FUEL));
    }
    public static class RenderState extends BlockEntityRenderState{
        public ItemStackRenderState itemRenderState;
        public ItemStack itemStack;
        public Direction facing;
        int seed;
        RandomSource random;
        public RenderState() {
            itemRenderState = new ItemStackRenderState();
            itemStack = ItemStack.EMPTY;
            facing = Direction.NORTH;
        }
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<RenderState> renderPassInfo, BoneSnapshots snapshots) {
        RenderState renderState = renderPassInfo.renderState();
        if (renderState.hasGeckolibData(FUEL)) {
            final int fuel = renderState.getGeckolibData(FUEL);
            snapshots.get("fuel_pile").ifPresent((fuelLayerBone) ->{
                if (fuel == 0){
                    fuelLayerBone.skipRender(true).skipChildrenRender(true);
                }
                else{
                    int fuelLevel = (fuel + 15) / 16;
                    BoneSnapshot level2 = snapshots.get("level2").orElse(null);
                    BoneSnapshot level3 = snapshots.get("level3").orElse(null);
                    if (fuelLevel < 3){
                        fuelLayerBone.setTranslateY(-1.99f);
                        if (level3 != null) level3.skipRender(true);

                        if (fuelLevel == 2) snapshots.get("smallCoal")
                                .ifPresent((coal) -> coal.setTranslateY(1f));
                        else if (level2 != null) level2.skipRender(true)
                                .skipChildrenRender(true);
                    }
                }

            });
        } else snapshots.get("fuel_pile").ifPresent(
                (f) -> f.skipRender(true).skipChildrenRender(true));
    }

    public static class Model extends GeoModel<HivekilnBlockEntity>{
        public static final Identifier MODEL = Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID,
                "block/hivekiln/new");
        public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID,
                "textures/block/hivekiln/new.png");
        public static final Identifier ANIMATION = Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID,
                "block/hivekiln");

        @Override
        public Identifier getModelResource(GeoRenderState renderState) {
            return MODEL;
        }

        @Override
        public Identifier getTextureResource(GeoRenderState renderState) {
            return TEXTURE;
        }

        @Override
        public Identifier getAnimationResource(HivekilnBlockEntity animatable) {
            return ANIMATION;
        }
    }
}
