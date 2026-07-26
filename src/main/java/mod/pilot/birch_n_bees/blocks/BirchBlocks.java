package mod.pilot.birch_n_bees.blocks;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.blocks.unique.PreparedSugarCaneBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BirchBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ABOBAB.MOD_ID);

    public static final DeferredBlock<Block> STICKY_PLANKS = BLOCKS.registerSimpleBlock("sticky_planks",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_PLANKS));

    public static final DeferredBlock<Block> COBBLED_ANDESITE = BLOCKS.registerSimpleBlock("cobbled_andesite",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE));
    public static final DeferredBlock<Block> COBBLED_DIORITE = BLOCKS.registerSimpleBlock("cobbled_diorite",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DIORITE));
    public static final DeferredBlock<Block> COBBLED_GRANITE = BLOCKS.registerSimpleBlock("cobbled_granite",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.GRANITE));
    public static final DeferredBlock<Block> COBBLED_TUFF = BLOCKS.registerSimpleBlock("cobbled_tuff",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF));

    public static final DeferredBlock<PreparedSugarCaneBlock> PREPARED_SUGARCANE = BLOCKS.registerBlock("prepared_sugarcane",
            PreparedSugarCaneBlock::new);
}
