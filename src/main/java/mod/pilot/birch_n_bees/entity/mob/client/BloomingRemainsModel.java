package mod.pilot.birch_n_bees.entity.mob.client;

import com.geckolib.model.DefaultedEntityGeoModel;
import mod.pilot.birch_n_bees.ABOBAB;
import mod.pilot.birch_n_bees.entity.mob.BloomingRemainsEntity;
import net.minecraft.resources.Identifier;

public class BloomingRemainsModel extends DefaultedEntityGeoModel<BloomingRemainsEntity> {
    public BloomingRemainsModel() {
        super(Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, "blooming_remains"));
    }
}
