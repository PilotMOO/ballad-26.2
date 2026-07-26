package mod.pilot.birch_n_bees.systems;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.items.BirchItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;

@EventBusSubscriber(modid = ABOBAB.MOD_ID)
public class HotBrickWatcher {
    private static int cookTime;
    public static void init(int time){
        cookTime = time;
    }

    public static HashMap<ItemEntity, Integer> hotBricks = new HashMap<>();

    public static void cookIfValid(ItemEntity entity){
        if (testBrick(entity)) cookBrick(entity);
    }
    public static boolean testBrick(ItemEntity entity){
        if (!entity.getItem().is(BirchItems.CLAY_BRICK)) return false;

        final int radius = 2;
        int x1, y1, z1, x2, y2, z2;
        x1 = x2 = entity.getBlockX();
        y1 = y2 = entity.getBlockY();
        z1 = z2 = entity.getBlockZ();
        x1 -= radius; y1 -= radius; z1 -= radius;
        x2 += radius; y2 += radius; z2 += radius;
        Level level = entity.level();
        for (BlockPos bPos : BlockPos.betweenClosed(x1, y1, z1, x2, y2, z2)){
            BlockState bState = level.getBlockState(bPos);
            if (bState.is(Blocks.FIRE) || bState.is(Blocks.SOUL_FIRE) || bState.is(Blocks.LAVA)) return true;
        }
        return false;
    }
    public static void cookBrick(ItemEntity entity){
        int time = hotBricks.getOrDefault(entity, -1);
        if (time == -1){
            hotBricks.put(entity, 0);
        } else if (++time >= cookTime){
            hotBricks.remove(entity);
            ItemStack brick = new ItemStack(Items.BRICK);
            brick.setCount(entity.getItem().getCount());
            entity.setItem(brick);
            if (entity.level() instanceof ServerLevel server){
                Vec3 pos = entity.position();
                server.playSound(entity, pos.x, pos.y, pos.z, SoundEvents.LAVA_EXTINGUISH, SoundSource.NEUTRAL, 1f, 1.25f);
                server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.x, pos.y, pos.z, 4, 0, 0.5, 0, 0.1f);
            }
        } else {
            hotBricks.replace(entity, time);
            if (entity.tickCount % 20 == 0 && entity.level() instanceof ServerLevel server) smoke(entity, server);
        }
    }
    private static void smoke(ItemEntity entity, ServerLevel server) {
        Vec3 pos = entity.position();
        server.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 4, 0, 0.5, 0, 0.01f);
        if (cookTime - hotBricks.get(entity) > 21) server.playSound(entity, pos.x, pos.y, pos.z, SoundEvents.FIRE_AMBIENT, SoundSource.NEUTRAL, 1f, 1.25f);
    }

    @SubscribeEvent
    public static void brickTick(EntityTickEvent.Post event){
        if (event.getEntity() instanceof ItemEntity entity) cookIfValid(entity);
    }
    @SubscribeEvent
    public static void cleanUp(ServerTickEvent.Pre event){
        if (event.getServer().overworld().getGameTime() % 200 == 0){
            ArrayList<ItemEntity> invalid = new ArrayList<>();
            for (ItemEntity ie : hotBricks.keySet()){
                if (!testBrick(ie)) invalid.add(ie);
            }
            for (ItemEntity ie : invalid) hotBricks.remove(ie);
        }
    }
}
