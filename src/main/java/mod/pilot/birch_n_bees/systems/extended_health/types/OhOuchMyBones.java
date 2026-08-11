package mod.pilot.birch_n_bees.systems.extended_health.types;

import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.systems.extended_health.Ailment;
import mod.pilot.birch_n_bees.systems.extended_health.AilmentInstance;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
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
    public void onApplicationClient(AbstractClientPlayer player) {
        player.spawnItemParticles(new ItemStack(Items.ACACIA_BUTTON), 40);
    }

    @Override
    public void onApplicationServer(ServerPlayer player) {
        player.setHealth(5f);
    }

    @Override
    public void alieveSideEffectsClient(AbstractClientPlayer player) {
        player.spawnItemParticles(new ItemStack(Items.BAKED_POTATO), 30);
    }

    @Override
    public void alieveSideEffectsServer(ServerPlayer player) {
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
