package mod.pilot.birch_n_bees.blocks.block_entities;

import com.mojang.logging.LogUtils;
import mod.pilot.birch_n_bees.blocks.BirchBlocks;
import mod.pilot.birch_n_bees.util.BirchAttachmentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class WildflowerBasketBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();

    public WildflowerBasketBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BirchBlocks.WILDFLOWER_BASKET_ENTITY.get(), worldPosition, blockState);
    }

    public void updateItemStack(ItemStack stack, ServerLevel level, @Nullable LivingEntity entity) {
        heldStack = stack;
        level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(entity, this.getBlockState()));
        setChanged();
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }
    public ItemStack heldStack = ItemStack.EMPTY;

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        output.store("heldStack", ItemStack.OPTIONAL_CODEC, heldStack);
    }
    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        heldStack = input.read("heldStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            output.store("heldStack", ItemStack.OPTIONAL_CODEC, heldStack);
            return output.buildResult();
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (hasLevel() && !heldStack.isEmpty()) Block.popResource(getLevel(), pos, heldStack);
    }
}
