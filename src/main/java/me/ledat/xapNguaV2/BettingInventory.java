package me.ledat.xapNguaV2;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class BettingInventory implements InventoryHolder {
    private final String title;
    private final Inventory inventory;

    public BettingInventory(String title, Inventory inventory) {
        this.title = title;
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public String getTitle() {
        return title;
    }
}
