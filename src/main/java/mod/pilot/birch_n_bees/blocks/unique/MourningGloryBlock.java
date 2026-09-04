package mod.pilot.birch_n_bees.blocks.unique;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

import java.util.function.ToIntFunction;

public class MourningGloryBlock extends MultifaceSpreadeableBlock implements BonemealableBlock {
    public static final MapCodec<MourningGloryBlock> CODEC = simpleCodec(MourningGloryBlock::new);
    public final MultifaceSpreader spreader = new MultifaceSpreader(this);

    public MapCodec<MourningGloryBlock> codec() {
        return CODEC;
    }

    public MourningGloryBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static ToIntFunction<BlockState> emission(int lightEmission) {
        return (state) -> MultifaceBlock.hasAnyFace(state) ? lightEmission : 0;
    }
    public static boolean hasAnyFace(BlockState bState){
        return MultifaceBlock.hasAnyFace(bState);
    }

    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return Direction.stream().anyMatch((face) -> this.spreader.canSpreadInAnyDirection(state, level, pos, face.getOpposite()));
    }

    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        this.spreader.spreadFromRandomFaceTowardRandomDirection(state, level, pos, random);
    }

    protected boolean propagatesSkylightDown(BlockState state) {
        return state.getFluidState().isEmpty();
    }

    public @NonNull MultifaceSpreader getSpreader() {
        return this.spreader;
    }
}
