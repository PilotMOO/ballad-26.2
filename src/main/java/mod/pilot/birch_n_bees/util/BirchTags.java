package mod.pilot.birch_n_bees.util;

import mod.pilot.birch_n_bees.ABOBAB;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class BirchTags {
    public static class Blocks{
        private static TagKey<Block> tag(String name){
            return BlockTags.create(Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, name));
        }

        public static final TagKey<Block> WARN_IF_INCORRECT_TOOL_KNIFE = tag("warn_if_incorrect_tool_knife");
    }

    public static class Items{
        private static TagKey<Item> tag(String name){
            return ItemTags.create(Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, name));
        }

        public static final TagKey<Item> HONEYCOMB_CRAFTING = tag("honey_crafting");
        public static final TagKey<Item> ROCK_CHIPPING = tag("rock_chipping");
        public static final TagKey<Item> ROCK_TIER_1 = tag("rock_tier_1");
        public static final TagKey<Item> ROCK_TIER_2 = tag("rock_tier_2");
        public static final TagKey<Item> ROCK_TIER_3 = tag("rock_tier_3");

        public static final TagKey<Item> COBBLE_TIER_1 = tag("cobble_tier_1");
        public static final TagKey<Item> COBBLE_TIER_2 = tag("cobble_tier_2");
        public static final TagKey<Item> COBBLE_TIER_3 = tag("cobble_tier_3");

        public static final TagKey<Item> KNIFE_HARVESTING = tag("knife_harvesting");

        public static final TagKey<Item> CAUSES_FOV_ZOOM = tag("causes_fov_zoom");

        public static final TagKey<Item> TOOL_RENDERSTATE_FOR_BASKET = tag("tool_renderstate_for_basket");

        public static final TagKey<Item> PASS_THE_TIME = tag("item_trigger_pass_the_time");
    }

    public static class DamageTypes{
        private static TagKey<DamageType> tag(String name){
            return TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(ABOBAB.MOD_ID, name));
        }

        public static final TagKey<DamageType> ONLY_DAMAGES_HEAD = tag("only_damages_head");
        public static final TagKey<DamageType> ONLY_DAMAGES_TORSO = tag("only_damages_torso");
        public static final TagKey<DamageType> ONLY_DAMAGES_ARMS = tag("only_damages_arms");
        public static final TagKey<DamageType> ONLY_DAMAGES_LEGS = tag("only_damages_legs");
    }
}
