package mod.pilot.birch_n_bees.systems.extended_health;

import mod.pilot.birch_n_bees.Config;
import mod.pilot.birch_n_bees.mixins.FoodDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.gamerules.GameRules;

public class RevampedFoodData extends FoodData {
    public FoodDataAccessor asAccessor(){
        return (FoodDataAccessor)this;
    }

    @Override
    public void tick(ServerPlayer player) {
        ServerLevel level = player.level();
        Difficulty difficulty = level.getDifficulty();
        FoodDataAccessor accessor = asAccessor();

        float exhaustion = accessor.getExhaustionLevel();
        int starvation = getStarvationSpeed(difficulty);
        if (player.tickCount % starvation == 0){
            accessor.setExhaustionLevel(++exhaustion);
        }

        float saturation = accessor.getSaturationLevel();
        int foodLevel = accessor.getFoodLevel();
        if (exhaustion > 4.0F) {
            accessor.setExhaustionLevel(exhaustion -= 4);
            if (saturation > 0.0F) {
                accessor.setSaturationLevel(Math.max(--saturation, 0.0F));
            } else if (difficulty != Difficulty.PEACEFUL) {
                accessor.setFoodLevel(Math.max(--foodLevel, 0));
            }
        }

        int timer = accessor.getTickTimer();
        HealthToken token = HealthToken.get(player);
        boolean regen = token.passiveRegen && level.getGameRules().get(GameRules.NATURAL_HEALTH_REGENERATION);
        if (regen && player.isHurt()){
            int regenSpeed = -1;
            if (foodLevel == 20 && saturation > 0) regenSpeed = getRegenSpeedFast(difficulty);
            else if (foodLevel >= getPartiallySatiatedThreshold(difficulty)) regenSpeed = getRegenSpeed(difficulty);
            if (regenSpeed != -1){
                int healingEff = token.healingEfficiency;
                if (healingEff < 0) healingEff = (int)(healingEff * getDeficiencyScalar(difficulty));
                regenSpeed -= healingEff;
                if (++timer >= regenSpeed){
                    player.heal(1);
                    accessor.setExhaustionLevel(exhaustion -= Math.min(exhaustion, 6.0f));
                    accessor.setTickTimer(timer = 0);
                }
            }
        }
        if (foodLevel <= 0){
            if (++timer >= 80){
                if (difficulty.getId() > 1 || player.getHealth() > 1f){
                    player.hurtServer(level, player.damageSources().starve(), 1.0F);
                }
                //ToDo: properly set up starvation ailment (and then debug... duh)
                accessor.setTickTimer(timer = 0);
            }
        } else accessor.setTickTimer(timer = 0);
    }

    public static void initConfig(Config.Server config){
        regenSpeedFast_EASY = config.easyHungerRegenSpeedFast.get();
        regenSpeed_EASY = config.easyHungerRegenSpeed.get();
        partiallySatiated_EASY = config.easyPartiallySatiatedThreshold.get();
        deficiencyScalar_EASY = config.easyHealingDeficiencyScalar.get();
        starvationSpeed_EASY = config.easyStarvationSpeed.get();

        regenSpeedFast_NORMAL = config.normalHungerRegenSpeedFast.get();
        regenSpeed_NORMAL = config.normalHungerRegenSpeed.get();
        partiallySatiated_NORMAL = config.normalPartiallySatiatedThreshold.get();
        deficiencyScalar_NORMAL = config.normalHealingDeficiencyScalar.get();
        starvationSpeed_NORMAL = config.normalStarvationSpeed.get();

        regenSpeedFast_HARD = config.hardHungerRegenSpeedFast.get();
        regenSpeed_HARD = config.hardHungerRegenSpeed.get();
        partiallySatiated_HARD = config.hardPartiallySatiatedThreshold.get();
        deficiencyScalar_HARD = config.hardHealingDeficiencyScalar.get();
        starvationSpeed_HARD = config.hardStarvationSpeed.get();
    }

    public static int getRegenSpeedFast(Difficulty difficulty){
        return switch (difficulty){
            case PEACEFUL -> -1;
            case EASY -> regenSpeedFast_EASY;
            case NORMAL -> regenSpeedFast_NORMAL;
            case HARD -> regenSpeedFast_HARD;
        };
    }
    public static int getRegenSpeed(Difficulty difficulty){
        return switch (difficulty){
            case PEACEFUL -> -1;
            case EASY -> regenSpeed_EASY;
            case NORMAL -> regenSpeed_NORMAL;
            case HARD -> regenSpeed_HARD;
        };
    }
    public static int getPartiallySatiatedThreshold(Difficulty difficulty){
        return switch (difficulty){
            case PEACEFUL -> -1;
            case EASY -> partiallySatiated_EASY;
            case NORMAL -> partiallySatiated_NORMAL;
            case HARD -> partiallySatiated_HARD;
        };
    }
    public static double getDeficiencyScalar(Difficulty difficulty){
        return switch (difficulty){
            case PEACEFUL -> -1;
            case EASY -> deficiencyScalar_EASY;
            case NORMAL -> deficiencyScalar_NORMAL;
            case HARD -> deficiencyScalar_HARD;
        };
    }
    public static int getStarvationSpeed(Difficulty difficulty){
        return switch (difficulty){
            case PEACEFUL -> -1;
            case EASY -> starvationSpeed_EASY;
            case NORMAL -> starvationSpeed_NORMAL;
            case HARD -> starvationSpeed_HARD;
        };
    }

    public static int regenSpeedFast_EASY;
    public static int regenSpeed_EASY;
    public static int partiallySatiated_EASY;
    public static double deficiencyScalar_EASY;
    public static int starvationSpeed_EASY;

    public static int regenSpeedFast_NORMAL;
    public static int regenSpeed_NORMAL;
    public static int partiallySatiated_NORMAL;
    public static double deficiencyScalar_NORMAL;
    public static int starvationSpeed_NORMAL;

    public static int regenSpeedFast_HARD;
    public static int regenSpeed_HARD;
    public static int partiallySatiated_HARD;
    public static double deficiencyScalar_HARD;
    public static int starvationSpeed_HARD;
}
