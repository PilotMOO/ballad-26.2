package mod.pilot.birch_n_bees.items.unique;

import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.DynamicInventoryToken;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class HotbarLockingTestWand extends Item {
    public HotbarLockingTestWand(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel){
            DynamicInventoryToken token = DynamicInventoryToken.get(player);
            if (token.hotbarSlots != 4) {
                token.hotbarSlots = 4;
                token.inventorySlots = 27;
            } else {
                token.hotbarSlots = 9;
                token.inventorySlots = 0;
            }
            player.syncData(DynamicInventoryToken.ATTACHMENT);
            token.apply(player);
        }
        return super.use(level, player, hand);
    }
}
