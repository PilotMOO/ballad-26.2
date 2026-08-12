package mod.pilot.birch_n_bees.items.unique.testing;

import mod.pilot.birch_n_bees.systems.extended_health.HealthToken;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class AilmentInspectionWand extends Item {
    public AilmentInspectionWand(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        String dist = level.isClientSide() ? "CLIENT" : "SERVER";
        player.sendSystemMessage(Component.literal(dist + ", HEALTH TOKEN: " + HealthToken.get(player)));
        return super.use(level, player, hand);
    }
}
