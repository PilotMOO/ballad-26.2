package mod.pilot.birch_n_bees.items.unique;

import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.DynamicInventoryToken;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class OffhandLockingTestWand extends Item {
    public OffhandLockingTestWand(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel){
            DynamicInventoryToken token = DynamicInventoryToken.get(player);
            token.offhand = !token.offhand;
            player.syncData(DynamicInventoryToken.ATTACHMENT);
        }
        return super.use(level, player, hand);
    }
}
