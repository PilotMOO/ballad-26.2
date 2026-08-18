package mod.pilot.birch_n_bees;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config
{
    public static final Server SERVER;
    public static final ModConfigSpec SERVER_SPEC;

    public static class Server{
        public final ModConfigSpec.ConfigValue<Double> birchChance;

        public final ModConfigSpec.ConfigValue<Integer> easyHungerRegenSpeedFast;
        public final ModConfigSpec.ConfigValue<Integer> easyHungerRegenSpeed;
        public final ModConfigSpec.ConfigValue<Integer> normalHungerRegenSpeedFast;
        public final ModConfigSpec.ConfigValue<Integer> normalHungerRegenSpeed;
        public final ModConfigSpec.ConfigValue<Integer> hardHungerRegenSpeedFast;
        public final ModConfigSpec.ConfigValue<Integer> hardHungerRegenSpeed;

        public final ModConfigSpec.ConfigValue<Integer> easyPartiallySatiatedThreshold;
        public final ModConfigSpec.ConfigValue<Integer> normalPartiallySatiatedThreshold;
        public final ModConfigSpec.ConfigValue<Integer> hardPartiallySatiatedThreshold;

        public final ModConfigSpec.ConfigValue<Double> easyHealingDeficiencyScalar;
        public final ModConfigSpec.ConfigValue<Double> normalHealingDeficiencyScalar;
        public final ModConfigSpec.ConfigValue<Double> hardHealingDeficiencyScalar;

        public final ModConfigSpec.ConfigValue<Integer> easyStarvationSpeed;
        public final ModConfigSpec.ConfigValue<Integer> normalStarvationSpeed;
        public final ModConfigSpec.ConfigValue<Integer> hardStarvationSpeed;


        public final ModConfigSpec.ConfigValue<Integer> brickCookTime;
        public final ModConfigSpec.ConfigValue<Boolean> enableDefaultInventory;

        public Server(ModConfigSpec.Builder builder){
            builder.push("Ballad Config");

            builder.push("World generation settings");
            birchChance = builder.defineInRange("Chance for a biome to be replaced with a birch biome: ", 0.99, 0, 1);
            builder.pop();

            builder.push("Revamped Hunger");
            builder.push("Easy Mode");
            easyHungerRegenSpeedFast = builder.defineInRange("How quickly, in ticks, players heal when fully saturated when difficulty is EASY: ",
                    300, 0, Integer.MAX_VALUE);
            easyHungerRegenSpeed = builder.defineInRange("How quickly, in ticks, players heal when partially saturated when difficulty is EASY: ",
                    600, 0, Integer.MAX_VALUE);
            easyPartiallySatiatedThreshold = builder.defineInRange("The threshold at which players are partially satiated, enabling passive regeneration when difficulty is EASY: ",
                    10, 0, 20);
            easyHealingDeficiencyScalar = builder.defineInRange("How impactful (multiplicatively) regeneration speed is affected by ailments when difficulty is EASY: ",
                    0.5d, 0d, Double.MAX_VALUE);
            easyStarvationSpeed = builder.defineInRange("How quickly, in ticks, players passively gain exhaustion when difficulty is EASY: ",
                    600, 0, Integer.MAX_VALUE);
            builder.pop();
            builder.push("Normal Mode");
            normalHungerRegenSpeedFast = builder.defineInRange("How quickly, in ticks, players heal when fully saturated when difficulty is NORMAL: ",
                    600, 0, Integer.MAX_VALUE);
            normalHungerRegenSpeed = builder.defineInRange("How quickly, in ticks, players heal when partially saturated when difficulty is NORMAL: ",
                    1200, 0, Integer.MAX_VALUE);
            normalPartiallySatiatedThreshold = builder.defineInRange("The threshold at which players are partially satiated, enabling passive regeneration when difficulty is NORMAL: ",
                    14, 0, 20);
            normalHealingDeficiencyScalar = builder.defineInRange("How impactful (multiplicatively) regeneration speed is affected by ailments when difficulty is NORMAL: ",
                    1d, 0d, Double.MAX_VALUE);
            normalStarvationSpeed = builder.defineInRange("How quickly, in ticks, players passively gain exhaustion when difficulty is NORMAL: ",
                    400, 0, Integer.MAX_VALUE);
            builder.pop();
            builder.push("Hard Mode");
            hardHungerRegenSpeedFast = builder.defineInRange("How quickly, in ticks, players heal when fully saturated when difficulty is HARD: ",
                    900, 0, Integer.MAX_VALUE);
            hardHungerRegenSpeed = builder.defineInRange("How quickly, in ticks, players heal when partially saturated when difficulty is HARD: ",
                    1500, 0, Integer.MAX_VALUE);
            hardPartiallySatiatedThreshold = builder.defineInRange("The threshold at which players are partially satiated, enabling passive regeneration when difficulty is HARD: ",
                    18, 0, 20);
            hardHealingDeficiencyScalar = builder.defineInRange("How impactful (multiplicatively) regeneration speed is affected by ailments when difficulty is HARD: ",
                    2d, 0d, Double.MAX_VALUE);
            hardStarvationSpeed = builder.defineInRange("How quickly, in ticks, players passively gain exhaustion when difficulty is HARD: ",
                    300, 0, Integer.MAX_VALUE);
            builder.pop();
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
