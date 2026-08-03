package mod.pilot.birch_n_bees.systems.dynamic_player_inventory;

public interface IPlayerInventoryResizable {
    void ballad$resizeHotbar(int size);
    void ballad$resizeInventory(int size);
    void ballad$updateOffhand(boolean valid);
    void ballad$updateArmor(boolean[] armor);
}
