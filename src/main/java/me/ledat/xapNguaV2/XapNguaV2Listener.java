package me.ledat.xapNguaV2;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class XapNguaV2Listener implements Listener {

    private final XapNguaV2 plugin;

    public XapNguaV2Listener(XapNguaV2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        // Kiểm tra tiêu đề của giao diện cược
        if (event.getView().getTitle().equals(ChatColor.DARK_AQUA + "Cược Xấp Ngửa")) {
            event.setCancelled(true); // Hủy hành động di chuyển vật phẩm

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) return;

            String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

            // Kiểm tra nếu người chơi nhấn vào vật phẩm "Cược Ngửa"
            if (itemName.equals("Cược Ngửa")) {
                player.sendMessage(ChatColor.YELLOW + "Bạn đã chọn cược Ngửa.");
                plugin.openBettingSign(player, "Ngửa"); // Mở bảng Sign cho cược "Ngửa"
                player.closeInventory();
            }
            // Kiểm tra nếu người chơi nhấn vào vật phẩm "Cược Xấp"
            else if (itemName.equals("Cược Xấp")) {
                player.sendMessage(ChatColor.YELLOW + "Bạn đã chọn cược Xấp.");
                plugin.openBettingSign(player, "Xấp"); // Mở bảng Sign cho cược "Xấp"
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        // Kiểm tra tiêu đề của giao diện
        if (event.getView().getTitle().equals(ChatColor.DARK_AQUA + "Cược Xấp Ngửa")) {
            // Nếu người chơi đóng GUI mà không cược gì, xóa thông tin cược
            plugin.getPlayerBets().remove(player.getName());
        }
    }
}