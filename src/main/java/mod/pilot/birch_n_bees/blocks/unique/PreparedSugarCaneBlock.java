package mod.pilot.birch_n_bees.blocks.unique;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class PreparedSugarCaneBlock extends DirectionalBlock {
    public static final int MAX_PROGRESS = 20;
    public static final int V_MAX_PROGRESS = 2;
    public static final IntegerProperty PROGRESS = IntegerProperty.create("progress", 0, MAX_PROGRESS);
    public static final IntegerProperty VISUAL_PROGRESS = IntegerProperty.create("v_progress", 0, V_MAX_PROGRESS);
    public PreparedSugarCaneBlock(Properties properties) {
        super(properties.destroyTime(0.1f).sound(SoundType.LEAF_LITTER));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PROGRESS, 0)
                .setValue(VISUAL_PROGRESS, 0));
    }

    public static final MapCodec<PreparedSugarCaneBlock> CODEC = simpleCodec(PreparedSugarCaneBlock::new);
    @Override
    protected @NotNull MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
        builder.add(PROGRESS);
        builder.add(VISUAL_PROGRESS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getNearestLookingDirection());
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(PROGRESS) < MAX_PROGRESS;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isBrightOutside() && level.canSeeSky(pos.above())){
            int progress = state.getValue(PROGRESS);
            if (progress < MAX_PROGRESS) {
                BlockState newState = state.setValue(PROGRESS, ++progress);

                int vProgress = 0;
                if (progress == MAX_PROGRESS) vProgress = 2;
                else if (progress > 0) vProgress = 1;
                if (vProgress != 0) newState = newState.setValue(VISUAL_PROGRESS, vProgress);

                level.setBlockAndUpdate(pos, newState);
            }
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    protected @NotNull VoxelShape getOcclusionShape(BlockState state) {
        return Shapes.empty();
    }
    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }
    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }


    public static final VoxelShape SHAPE = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.1875D, 1.0D);
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SHAPE;
    }
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
