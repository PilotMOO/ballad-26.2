package mod.pilot.birch_n_bees;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config
{
    public static final Server SERVER;
    public static final ModConfigSpec SERVER_SPEC;

    public static class Server{
        public final ModConfigSpec.ConfigValue<Double> birchChance;

        public final ModConfigSpec.ConfigValue<Integer> brickCookTime;
        public final ModConfigSpec.ConfigValue<Boolean> enableDefaultInventory;

        public Server(ModConfigSpec.Builder builder){
            builder.push("Ballad Config");

            builder.push("World generation settings");
            birchChance = builder.defineInRange("Chance for a biome to be replaced with a birch biome: ", 0.99, 0, 1);
            builder.pop();

            builder.push("Misc");
            brickCookTime = builder.defineInRange("How long it takes for clay bricks to cook", 1000, 0, Integer.MAX_VALUE);
            enableDefaultInventory = builder.define("Set the default inventory size to vanilla?", false);
            builder.pop();

            builder.pop();
        }
    }

    static {
        Pair<Server, ModConfigSpec> commonSpecPair = new ModConfigSpec.Builder().configure(Server::new);
        SERVER = commonSpecPair.getLeft();
        SERVER_SPEC = commonSpecPair.getRight();
    }
}
