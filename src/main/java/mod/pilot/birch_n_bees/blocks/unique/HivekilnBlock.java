package mod.pilot.birch_n_bees.blocks.unique;

import com.mojang.serialization.MapCodec;
import mod.pilot.birch_n_bees.blocks.block_entities.HivekilnBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class HivekilnBlock extends HorizontalDirectionalBlock implements EntityBlock, SimpleWaterloggedBlock {
    public static MapCodec<HivekilnBlock> SHRIMPLE_CODEC = simpleCodec(HivekilnBlock::new);
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    public static final int MAX_FUEL = 48;
    public static final IntegerProperty FUEL = IntegerProperty.create("fuel", 0, MAX_FUEL);

    @Override
    protected @NonNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return SHRIMPLE_CODEC;
    }

    public HivekilnBlock(Properties properties) {
        super(properties.destroyTime(1.5f)
                .noOcclusion()
                .sound(SoundType.MUD_BRICKS));
        registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(BlockStateProperties.WATERLOGGED, false)
                .setValue(OPEN, false)
                .setValue(FUEL, 0));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BlockStateProperties.WATERLOGGED, OPEN, FUEL);
    }

    //ACTUAL LOGIC

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level,
                                          BlockPos pos, Player player, InteractionHand hand,
                                          BlockHitResult hitResult) {
        if (/*!level.isClientSide() &&*/ player.isSecondaryUseActive()) {
            level.setBlock(pos, state.setValue(OPEN, !state.getValue(OPEN)), 3);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    //BLOCK ENTITY AND MISC LOGIC

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new HivekilnBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type) {
        if (level instanceof ServerLevel server)
            return (innerLevel, pos, state, entity) ->
                    HivekilnBlockEntity.serverTick(server, pos, state, (HivekilnBlockEntity)entity);
        else return (innerLevel, pos, state, entity) ->
                HivekilnBlockEntity.clientTick(level, pos, state, (HivekilnBlockEntity)entity);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    public static final VoxelShape SHAPE = Shapes.box(-0.0625, 0.0D, -0.0625,
            1.0625, 1.0625, 1.0625);
    @Override
    protected @NonNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    @Override
    protected @NonNull VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SHAPE;
    }
    @Override
    protected @NonNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
