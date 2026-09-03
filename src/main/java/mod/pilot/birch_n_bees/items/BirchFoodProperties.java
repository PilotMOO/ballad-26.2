package mod.pilot.birch_n_bees.items;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;

public class BirchFoodProperties {
    public static final FoodProperties BARK_FOOD = new FoodProperties.Builder()
            .nutrition(1).build();

    public static final FoodProperties SMASHED_PUMPKIN_FOOD = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.25f).build();
    public static final Consumable SMASHED_PUMPKIN_CONSUMABLE = Consumable.builder()
            .consumeSeconds(5f).sound(Holder.direct(SoundEvents.HONEY_BLOCK_SLIDE))
            .soundAfterConsume(Holder.direct(SoundEvents.HONEY_BLOCK_BREAK)).build();

    public static final FoodProperties PUMPKIN_SLICE_FOOD = new FoodProperties.Builder()
            .nutrition(1).saturationModifier(0.1f).build();
    public static final Consumable PUMPKIN_SLICE_CONSUMABLE = Consumable.builder()
            .consumeSeconds(1.25f).build();

    public static final FoodProperties CHARRED_MEAT_FOOD = new FoodProperties.Builder()
            .nutrition(2).saturationModifier(0.25f).build();
    public static final Consumable CHARRED_MEAT_CONSUMABLE = Consumable.builder()
            .consumeSeconds(2.5f).build();
    public static final FoodProperties CHARRED_FISH_FOOD = new FoodProperties.Builder()
            .nutrition(1).saturationModifier(0.5f).build();
    public static final Consumable CHARRED_FISH_CONSUMABLE = Consumable.builder()
            .consumeSeconds(1.5f).build();
}
