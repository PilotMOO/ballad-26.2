package mod.pilot.birch_n_bees.achievements;

import net.minecraft.server.level.ServerPlayer;

public class IllegalTrigger extends CustomTrigger {
    public IllegalTrigger(String id) {super(id);}
    @Override
    public void trigger(ServerPlayer player){
        BirchCriteriaRegistries.ANYTHING_ILLEGAL.get().trigger(player);
        this.trigger(player, (instance) -> true);
    }
}
