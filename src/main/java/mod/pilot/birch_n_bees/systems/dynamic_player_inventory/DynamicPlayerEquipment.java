package mod.pilot.birch_n_bees.systems.dynamic_player_inventory;

import mod.pilot.birch_n_bees.mixins.EntityEquipmentAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerEquipment;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class DynamicPlayerEquipment extends PlayerEquipment {
    public final Player player;

    public boolean offhand;
    public boolean mainhand;
    public boolean[] armor;

    public void updateMainhand(boolean mainhand){
        ItemStack mainhandStack = getWithoutContext(EquipmentSlot.MAINHAND);
        if (!mainhand && !mainhandStack.isEmpty()){
            player.drop(mainhandStack, false, true);
            setWithoutContext(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }

        this.mainhand = mainhand;
    }
    public void updateOffhand(boolean offhand){
        ItemStack offhandStack = getWithoutContext(EquipmentSlot.OFFHAND);
        if (!offhand && !offhandStack.isEmpty()){
            player.drop(offhandStack, false, true);
            setWithoutContext(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }

        this.offhand = offhand;
    }
    public void updateArmor(boolean[] armor){
        for (int i = 0; i < 4; i++){
            if (armor[i]) continue;
            EquipmentSlot slot = armorSlotByIndex(i);
            ItemStack wornArmor = getWithoutContext(slot);
            if (!wornArmor.isEmpty()){
                player.drop(wornArmor, false, false);
                setWithoutContext(slot, ItemStack.EMPTY);
            }
        }

        this.armor = armor;
    }
    public static EquipmentSlot armorSlotByIndex(int index){
        return switch (index % 4){
            case 0 -> EquipmentSlot.FEET;
            case 1 -> EquipmentSlot.LEGS;
            case 2 -> EquipmentSlot.CHEST;
            case 3 -> EquipmentSlot.HEAD;
            default -> throw new IllegalStateException("Unexpected value: " + index % 4);
        };
    }
    public boolean validateSlot(EquipmentSlot slot){
        return switch (slot){
            case MAINHAND -> mainhand;
            case OFFHAND -> offhand;
            case FEET -> armor[0];
            case LEGS -> armor[1];
            case CHEST -> armor[2];
            case HEAD -> armor[3];
            default -> true;
        };
    }

    public final EnumMap<EquipmentSlot, ItemStack> items;
    public DynamicPlayerEquipment(Player player){
        super(player);
        this.player = player;
        if (this instanceof EntityEquipmentAccessor accessor){
            items = accessor.getItems();
        } else items = new EnumMap<>(EquipmentSlot.class);

        offhand = mainhand = true;
        armor = new boolean[4];
        armor[0] = true;
        armor[1] = true;
        armor[2] = true;
        armor[3] = true;
    }

    public ItemStack setWithoutContext(EquipmentSlot slot, ItemStack itemStack){
        return items.put(slot, itemStack);
    }
    public @NonNull ItemStack set(@NonNull EquipmentSlot slot, @NonNull ItemStack itemStack) {
        if (validateSlot(slot)){
            if (slot == EquipmentSlot.MAINHAND){
                player.getInventory().setSelectedItem(itemStack);
            }
            return Objects.requireNonNullElse(this.items.put(slot, itemStack), ItemStack.EMPTY);
        }
        else return ItemStack.EMPTY;
    }

    public ItemStack getWithoutContext(EquipmentSlot slot){
        return items.getOrDefault(slot, ItemStack.EMPTY);
    }
    public @NonNull ItemStack get(@NonNull EquipmentSlot slot) {
        if (validateSlot(slot)) {
            if (slot == EquipmentSlot.MAINHAND){
                return player.getInventory().getSelectedItem();
            }
            return this.items.getOrDefault(slot, ItemStack.EMPTY);
        }
        else return ItemStack.EMPTY;
    }

    public boolean isEmpty() {
        for (EquipmentSlot slot : items.keySet()){
            if (validateSlot(slot) && !get(slot).isEmpty()) return false;
        }

        return true;
    }

    public void tick(@NonNull Entity owner) {
        for(Map.Entry<EquipmentSlot, ItemStack> entry : this.items.entrySet()) {
            if (validateSlot(entry.getKey())) {
                ItemStack item = entry.getValue();
                if (!item.isEmpty()) {
                    item.inventoryTick(owner.level(), owner, entry.getKey());
                }
            }
        }
    }

    public void setAll(@NonNull EntityEquipment equipment) {
        this.items.clear();
        if (equipment instanceof EntityEquipmentAccessor accessor){
            this.items.putAll(accessor.getItems());
        }
    }

    public void dropAll(@NonNull LivingEntity dropper) {
        for(ItemStack item : this.items.values()) {
            dropper.drop(item, true, false);
        }

        this.clear();
    }

    public void clear() {
        this.items.replaceAll((_, _) -> ItemStack.EMPTY);
    }
}
