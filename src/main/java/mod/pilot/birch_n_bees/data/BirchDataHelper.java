package mod.pilot.birch_n_bees.data;

import com.google.common.collect.Lists;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class BirchDataHelper {
    private static final ArrayList<Item> CONTRABAND = Lists.newArrayList(
            Items.WOODEN_AXE,
            Items.WOODEN_HOE,
            Items.WOODEN_PICKAXE,
            Items.WOODEN_SHOVEL,
            Items.WOODEN_SWORD,
            Items.STONE_AXE,
            Items.STONE_HOE,
            Items.STONE_PICKAXE,
            Items.STONE_SHOVEL,
            Items.STONE_SWORD);
    public static boolean contraband(ItemStack item){
        return contraband(item.getItem());
    }
    public static boolean contraband(Item item){
        return CONTRABAND.contains(item);
    }

    public static void popBees(ServerLevel server, Vec3 pos, int count){
        popBees(server, pos, count, null);
    }
    public static void popBees(ServerLevel server, Vec3 pos, int count, @Nullable LivingEntity target){
        RandomSource random = server.getRandom();
        for (int i = 0; i < count; i++) {
            Bee bee = new Bee(EntityTypes.BEE, server);
            bee.setPos(pos);
            bee.setTarget(target);
            bee.invulnerableTime = 20;

            Vec3 delta = new Vec3(1, 0, 1);
            delta = delta.yRot((float)Math.toRadians(random.nextIntBetweenInclusive(-180, 180)));
            bee.setDeltaMovement(delta.scale(0.25));

            server.addFreshEntity(bee);
        }
    }

    public static void popKillerBees(ServerLevel server, Vec3 pos, int count){
        popKillerBees(server, pos, count, null);
    }
    public static void popKillerBees(ServerLevel server, Vec3 pos, int count, @Nullable LivingEntity target){
        RandomSource random = server.getRandom();
        for (int i = 0; i < count; i++) {
            Bee bee = new Bee(EntityTypes.BEE, server){
                @Override
                public boolean hasStung() {
                    return false;
                }
            };
            bee.setCustomName(Component.translatable("birch_n_bees.killer_bee"));

            AttributeInstance scale = bee.getAttribute(Attributes.SCALE);
            if (scale != null) scale.setBaseValue(1.5);
            AttributeInstance fSpeed = bee.getAttribute(Attributes.FLYING_SPEED);
            if (fSpeed != null) fSpeed.setBaseValue(1.2);
            AttributeInstance speed = bee.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) speed.setBaseValue(.75);
            AttributeInstance damage = bee.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damage != null) damage.setBaseValue(5);
            AttributeInstance hp = bee.getAttribute(Attributes.MAX_HEALTH);
            if (hp != null) hp.setBaseValue(20);

            bee.setPos(pos);
            bee.setTarget(target);
            bee.invulnerableTime = 20;

            Vec3 delta = new Vec3(1, 0, 1);
            delta = delta.yRot((float)Math.toRadians(random.nextIntBetweenInclusive(-180, 180)));
            bee.setDeltaMovement(delta.scale(0.25));

            server.addFreshEntity(bee);
        }
    }
}
