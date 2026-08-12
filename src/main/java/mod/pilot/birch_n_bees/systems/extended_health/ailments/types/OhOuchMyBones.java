package mod.pilot.birch_n_bees.systems.extended_health.ailments.types;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.systems.extended_health.ailments.Ailment;
import mod.pilot.birch_n_bees.systems.extended_health.ailments.AilmentInstance;
import mod.pilot.birch_n_bees.systems.extended_health.HealthToken;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class OhOuchMyBones extends Ailment {
    public OhOuchMyBones() {
        super(Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "oh_ouch_my_bones"));
    }

    @Override
    public AilmentInstance<?> buildInstance(boolean client, byte severity, int timeUntilCured) {
        if (client){
            return new AilmentInstance.Client(this, severity);
        } else return new AilmentInstance.Server(this, severity, timeUntilCured);
    }

    @Override
    public void onApplicationClient(AilmentInstance.Client instance, AbstractClientPlayer player, HealthToken token) {
        player.spawnItemParticles(new ItemStack(Items.ACACIA_BUTTON), 40);
    }

    @Override
    public void onApplicationServer(AilmentInstance.Server instance, ServerPlayer player, HealthToken token) {
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(5f);
        player.setHealth(5f);
    }

    @Override
    public void alieveSideEffectsClient(AilmentInstance.Client instance, AbstractClientPlayer player, HealthToken token) {
        player.spawnItemParticles(new ItemStack(Items.BAKED_POTATO), 30);
    }

    @Override
    public void alieveSideEffectsServer(AilmentInstance.Server instance, ServerPlayer player, HealthToken token) {
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20f);
        player.setHealth(20f);
    }

    @Override
    public AilmentInstance.Client constructDefaultClientInstance() {
        return new AilmentInstance.Client(this, (byte)0);
    }
    @Override
    public AilmentInstance.Server constructDefaultServerInstance() {
        return new AilmentInstance.Server(this, (byte)0, 200);
    }
}
