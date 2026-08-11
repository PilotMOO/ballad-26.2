package mod.pilot.birch_n_bees.items.unique;

import mod.pilot.birch_n_bees.systems.extended_health.Ailment;
import mod.pilot.birch_n_bees.systems.extended_health.AilmentManager;
import mod.pilot.birch_n_bees.systems.extended_health.HealthToken;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class WandOfBonePain extends Item {
    public WandOfBonePain(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, @NonNull Player player, InteractionHand hand) {
        System.out.println("The wand of bone pain has been used! according to the AilmentManager, this is the "
                + (AilmentManager.isClientSide() ? "Client side." : "Server side.")
                + " according to the level, this is the "
                + (level.isClientSide() ? "Client side." : "Server side."));
        HealthToken token = HealthToken.get(player);
        if (level instanceof ServerLevel) {
            if (player.isSecondaryUseActive()) {
                token.remove(AilmentManager.OH_OUCH_MY_BONES);
            } else if (token.getAilment(AilmentManager.OH_OUCH_MY_BONES) == null) {
                token.add(AilmentManager.OH_OUCH_MY_BONES.buildInstance(false, (byte)0, 200));
            } else player.sendSystemMessage(Component.literal("Hi."));
            player.setData(HealthToken.ATTACHMENT, token);
            System.out.println("Token AFTER modification on the SERVER: " + token);
        } else System.out.println("Token as seen on the client... " + token);
        player.getCooldowns().addCooldown(player.getItemInHand(hand), 5);
        return super.use(level, player, hand);
    }
}
