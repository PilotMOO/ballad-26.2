package mod.pilot.birch_n_bees.mixins;

import org.spongepowered.asm.mixin.gen.Accessor;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.world.food.FoodData.class)
public interface FoodDataAccessor {
    @Accessor
    int getFoodLevel();

    @Accessor
    void setFoodLevel(int foodLevel);

    @Accessor
    float getSaturationLevel();

    @Accessor
    void setSaturationLevel(float saturationLevel);

    @Accessor
    float getExhaustionLevel();

    @Accessor
    void setExhaustionLevel(float exhaustionLevel);

    @Accessor
    int getTickTimer();

    @Accessor
    void setTickTimer(int tickTimer);
}
