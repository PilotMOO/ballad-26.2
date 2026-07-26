package mod.pilot.birch_n_bees.items;

import net.minecraft.world.food.FoodProperties;

public class BirchFoodProperties {
    public static final FoodProperties BARK_FOOD = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(.1f).build();
}
