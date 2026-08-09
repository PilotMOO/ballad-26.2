package mod.pilot.birch_n_bees.blocks.unique;

import com.mojang.serialization.MapCodec;
import mod.pilot.birch_n_bees.blocks.block_entities.WildflowerBasketBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class WildflowerBasketBlock extends HorizontalDirectionalBlock implements EntityBlock, SimpleWaterloggedBlock {
    public static MapCodec<WildflowerBasketBlock> SHRIMPLE_CODEC = simpleCodec(WildflowerBasketBlock::new);
    public static BooleanProperty HAS_ITEM = BooleanProperty.create("has_item");

    @Override
    protected @NonNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return SHRIMPLE_CODEC;
    }

    public WildflowerBasketBlock(Properties properties) {
        super(properties.destroyTime(0.5f).sound(SoundType.MOSS));
        registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(BlockStateProperties.WATERLOGGED, false)
                .setValue(HAS_ITEM, Boolean.FALSE));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BlockStateProperties.WATERLOGGED, HAS_ITEM);
    }
    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos blockPos, @NonNull BlockState blockState) {
        return new WildflowerBasketBlockEntity(blockPos, blockState);
    }


    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hand.equals(InteractionHand.OFF_HAND)) return InteractionResult.PASS;
        if (level.getBlockEntity(pos) instanceof WildflowerBasketBlockEntity basket
                && level instanceof ServerLevel server) {
            ItemStack basketStack = basket.heldStack;
            if (basketStack.isEmpty()) {
                if (itemStack.isEmpty()) return InteractionResult.TRY_WITH_EMPTY_HAND;
                else if (!itemStack.has(DataComponents.BUNDLE_CONTENTS) || itemStack.get(DataComponents.BUNDLE_CONTENTS).isEmpty()){
                    basket.updateItemStack(itemStack.copyAndClear(), server, player);
                    level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                            SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS,
                            0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
                    return InteractionResult.SUCCESS_SERVER;
                }
            } else if (itemStack.is(basketStack.getItem())) {
                int handCount = itemStack.count(), basketCount = basketStack.count();
                int cumulative = handCount + basketCount,
                        delta = cumulative - itemStack.getMaxStackSize();
                if (itemStack.getMaxStackSize() > handCount
                        && basketStack.getMaxStackSize() > basketCount) {
                    if (delta <= 0) {
                        basketStack.setCount(cumulative);
                        itemStack.setCount(0);
                        basket.updateItemStack(basketStack, server, player);
                    }
                    else {
                        basketStack.setCount(basketStack.getMaxStackSize());
                        itemStack.setCount(delta);
                        basket.updateItemStack(basketStack, server, player);
                    }
                } else {
                    basketStack.setCount(handCount);
                    itemStack.setCount(basketCount);
                    basket.updateItemStack(basketStack, server, player);
                }
                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS,
                        0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
                return InteractionResult.SUCCESS_SERVER;
            } else {
                if (itemStack.isEmpty()) player.setItemInHand(hand, basketStack.copyAndClear());
                else player.addItem(basketStack.copyAndClear());
                basket.updateItemStack(basketStack, server, player);
                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS,
                        0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
                return InteractionResult.SUCCESS_SERVER;
            }
        }
        return InteractionResult.FAIL;
    }

    public static final VoxelShape SHAPE_NS = Shapes.box(0.1875, 0.0D, 0.3125, 0.8125, 0.5, 0.6875);
    public static final VoxelShape SHAPE_EW = Shapes.box(0.3125, 0.0D, 0.1875, 0.6875, 0.5, 0.8125);
    @Override
    protected @NonNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)){
            case EAST, WEST -> SHAPE_EW;
            default -> SHAPE_NS;
        };
    }
    @Override
    protected @NonNull VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return switch (state.getValue(FACING)){
            case EAST, WEST -> SHAPE_EW;
            default -> SHAPE_NS;
        };
    }
    @Override
    protected @NonNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)){
            case EAST, WEST -> SHAPE_EW;
            default -> SHAPE_NS;
        };
    }
}
