package mod.pilot.birch_n_bees.items.unique;

import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.DynamicInventory;
import mod.pilot.birch_n_bees.systems.dynamic_player_inventory.DynamicInventoryToken;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
            if (token.hotbarSlots != 0) {
                token.hotbarSlots = 0;
                token.inventorySlots = 27;
                token.offhand = true;

                token.armor[0] = false;
                token.armor[1] = true;
                token.armor[2] = false;
                token.armor[3] = true;
            } else {
                token.hotbarSlots = 9;
                token.inventorySlots = 0;
                token.offhand = false;

                token.armor[0] = true;
                token.armor[1] = false;
                token.armor[2] = true;
                token.armor[3] = false;
            }
            player.syncData(DynamicInventoryToken.ATTACHMENT);
            DynamicInventoryToken.applyInFull(player);
        }
        return super.use(level, player, hand);
    }
}
