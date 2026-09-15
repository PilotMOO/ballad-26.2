package mod.pilot.birch_n_bees.blocks.block_entities;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import mod.pilot.birch_n_bees.blocks.BirchBlocks;
import mod.pilot.birch_n_bees.blocks.unique.HivekilnBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;

public class HivekilnBlockEntity extends BlockEntity implements GeoBlockEntity {

    public HivekilnBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BirchBlocks.HIVEKILN_BLOCK_ENTITY.get(), worldPosition, blockState);
    }

    public boolean clientOpenFlag = false;
    public byte animateFlag = 0;
    public static final byte FLAG_OPEN = 1, FLAG_CLOSE = -1;
    public void animateOpen(){
        animateFlag = FLAG_OPEN;
        getLevel().playLocalSound(worldPosition, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.25f, 0.75f, false);
    }
    public void animateClose(){
        animateFlag = FLAG_CLOSE;
        getLevel().playLocalSound(worldPosition, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 1.25f, 1.25f, false);
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, HivekilnBlockEntity entity) {

    }
    public static void clientTick(Level level, BlockPos pos, BlockState state, HivekilnBlockEntity entity) {
        boolean flag = state.getValue(HivekilnBlock.OPEN);
        if (flag != entity.clientOpenFlag){
            System.out.println("I'm shitting on the client side@!!!!!!112");
            if (flag) entity.animateOpen();
            else entity.animateClose();
            entity.clientOpenFlag = flag;
        }
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
    }
    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("hivekiln", 0, event -> {
            if (animateFlag == 1){
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("open"));
            }
            if (animateFlag == -1){
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("close"));
            }
            return PlayState.CONTINUE;
        }));
    }
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    @Override
    public @NonNull AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
