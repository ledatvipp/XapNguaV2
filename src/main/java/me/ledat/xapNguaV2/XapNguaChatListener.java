package me.ledat.xapNguaV2;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Material;
import org.bukkit.Bukkit;

public class XapNguaChatListener implements Listener {

    private final XapNguaV2 plugin;

    public XapNguaChatListener(XapNguaV2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null || !(block.getState() instanceof Sign)) return;

        Sign sign = (Sign) block.getState();
        String firstLine = ChatColor.stripColor(sign.getLine(0));

        // Kiểm tra bảng hợp lệ
        if (!"[XapNgua]".equalsIgnoreCase(firstLine)) return;

        // Mở Sign GUI cho người chơi
        this.openSignGUI(player);
    }

    public void openSignGUI(Player player) {
        // Tạo một bảng tạm
        Block block = player.getLocation().getBlock();
        block.setType(Material.OAK_SIGN);

        // Lấy trạng thái bảng
        Sign sign = (Sign) block.getState();

        // Ghi các dòng mặc định (nếu cần)
        sign.setLine(0, "[XapNgua]");
        sign.setLine(1, "Nhập số tiền:");
        sign.setLine(2, " ");
        sign.setLine(3, " ");
        sign.update();

        // Mở bảng hiệu qua NMS hoặc thư viện (xử lý phiên bản không có Player#openSign)
        try {
            player.openSign(sign); // Nếu có hỗ trợ, sử dụng phương thức này
        } catch (NoSuchMethodError e) {
            player.sendMessage(ChatColor.RED + "Không thể mở bảng hiệu. Phiên bản máy chủ không hỗ trợ!");
        }

        // Sau khi hoàn tất, bảng sẽ bị xóa
        Bukkit.getScheduler().runTaskLater(plugin, () -> block.setType(Material.AIR), 20L);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        String[] lines = event.getLines();

        // Kiểm tra bảng hợp lệ
        if (!"[XapNgua]".equalsIgnoreCase(lines[0])) return;

        try {
            double betAmount = Double.parseDouble(lines[1]); // Dòng thứ hai là số tiền cược
            String choice = plugin.getGuiPlayerBets().get(player.getName()); // Lấy lựa chọn Xấp/Ngửa

            if (choice == null) {
                player.sendMessage(ChatColor.RED + "Bạn chưa chọn Xấp hoặc Ngửa!");
                return;
            }

            // Gọi hàm xử lý cược
            plugin.handleGUICuoc(player, choice, betAmount);

        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Số tiền cược không hợp lệ! Vui lòng nhập một số hợp lệ.");
        }
    }
}
