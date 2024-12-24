package me.ledat.xapNguaV2;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.*;

public class XapNguaV2 extends JavaPlugin {

    private DailyChallengeManager dailyChallengeManager;
    private LeaderboardManager leaderboardManager;
    private Economy economy;
    private double totalXap = 0;
    private double totalNgua = 0;
    private final Map<String, String> playerBets = new HashMap<>(); // Player's choice: "xap" or "ngua"
    private final Map<String, Double> playerBetAmounts = new HashMap<>(); // Số tiền cược của người chơi
    private final Map<String, String> guiPlayerBets = new HashMap<>();

    private BukkitRunnable currentSession = null;
    private String currentResult = null; // Result of the current session ("xap" or "ngua")
    private int sessionDuration; // Duration of the session in seconds
    private int remainingTime; // Time remaining for the session to countdown

    private SessionStatus currentSessionStatus = SessionStatus.ONGOING;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveDefaultQuestConfig();
        setupEconomy();

        this.dailyChallengeManager = new DailyChallengeManager(this);
        this.leaderboardManager = new LeaderboardManager(this);

        // Đăng ký sự kiện
        getServer().getPluginManager().registerEvents(new XapNguaV2Listener(this), this);
        getServer().getPluginManager().registerEvents(new XapNguaChatListener(this), this);

        // Register PlaceholderAPI if available
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIExpansion(this).register();
        }

        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault plugin không được tìm thấy, tính năng cược sẽ không hoạt động hahahaha!");
        }

        getCommand("xmm").setExecutor(this);
        getCommand("xmm").setTabCompleter(this);

        getLogger().info("Lê Đạt quá đẹp trai. Lỗi đéo cho đọc đâu =))");
        startNewSession();
    }

    @Override
    public void onDisable() {
        if (currentSession != null) {
            currentSession.cancel();
        }
        getLogger().info("XapNguaV2 đã tắt.");
    }

    public LeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }

    public DailyChallengeManager getDailyChallengeManager() {
        return this.dailyChallengeManager;
    }

    public Map<String, Double> getPlayerBetAmounts() {
        return playerBetAmounts;
    }

    public Map<String, String> getPlayerBets() {
        return playerBets;
    }

    public Map<String, String> getGuiPlayerBets() {
        return guiPlayerBets;
    }

    public Economy getEconomy() {
        return economy;
    }

    // Phương thức để cập nhật tổng tiền cược Xấp
    public void addToTotalXap(double amount) {
        this.totalXap += amount;
    }

    // Phương thức để cập nhật tổng tiền cược Ngửa
    public void addToTotalNgua(double amount) {
        this.totalNgua += amount;
    }

    // Getter cho các giá trị totalXap và totalNgua nếu cần
    public double getTotalXap() {
        return totalXap;
    }

    public double getTotalNgua() {
        return totalNgua;
    }

    public void openBettingGUI(Player player) {
        // Tạo một GUI với 9 slot
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.DARK_AQUA + "Cược Xấp Ngửa");

        // Ô cược Ngửa (Slot 2)
        ItemStack betNguaItem = new ItemStack(Material.PAPER);
        ItemMeta betNguaMeta = betNguaItem.getItemMeta();
        betNguaMeta.setDisplayName(ChatColor.GREEN + "Cược Ngửa");
        betNguaItem.setItemMeta(betNguaMeta);
        gui.setItem(2, betNguaItem);

        // Ô cược Xấp (Slot 6)
        ItemStack betXapItem = new ItemStack(Material.PAPER);
        ItemMeta betXapMeta = betXapItem.getItemMeta();
        betXapMeta.setDisplayName(ChatColor.GREEN + "Cược Xấp");
        betXapItem.setItemMeta(betXapMeta);
        gui.setItem(6, betXapItem);

        // Mở GUI cho người chơi
        player.openInventory(gui);
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
                this.openBettingSign(player, "Ngửa"); // Mở bảng Sign cho cược "Ngửa"
                player.closeInventory();
            }
            // Kiểm tra nếu người chơi nhấn vào vật phẩm "Cược Xấp"
            else if (itemName.equals("Cược Xấp")) {
                player.sendMessage(ChatColor.YELLOW + "Bạn đã chọn cược Xấp.");
                this.openBettingSign(player, "Xấp"); // Mở bảng Sign cho cược "Xấp"
                player.closeInventory();
            }
        }
    }

    public void openBettingSign(Player player, String choice) {
        // Tạo vị trí để đặt bảng Sign ngay dưới chân người chơi
        Location location = player.getLocation();
        location.setY(location.getY() - 1); // Đặt bảng Sign dưới chân người chơi (Y - 1)

        Block signBlock = location.getBlock();

        // Kiểm tra xem có thể đặt bảng Sign ở vị trí này không
        if (!signBlock.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "Không thể đặt bảng cược tại đây. Vui lòng thử lại ở vị trí khác.");
            return;
        }

        // Đặt block Sign
        signBlock.setType(Material.OAK_SIGN);
        Sign sign = (Sign) signBlock.getState();

        // Đảm bảo bảng Sign có thể chỉnh sửa
        sign.setEditable(true);

        // Cập nhật nội dung bảng Sign
        sign.setLine(0, ChatColor.GREEN + "Cược " + choice);
        sign.setLine(1, ChatColor.WHITE + "Nhập số tiền cược");
        sign.setLine(2, ChatColor.YELLOW + "Bấm Enter để xác nhận");
        sign.setLine(3, "");
        sign.update();

        // Gửi thông báo cho người chơi
        player.sendMessage(ChatColor.YELLOW + "Bảng cược đã được tạo. Vui lòng nhập số tiền.");

        // Thông báo cho người chơi biết bảng đã được đặt
        player.sendMessage(ChatColor.YELLOW + "Bảng cược được tạo tại " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
    }


    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        String[] lines = event.getLines();

        // Kiểm tra nếu bảng Sign chứa thông tin về cược
        if (lines[0].startsWith(ChatColor.GREEN + "Cược")) {
            String choice = lines[0].substring(7); // Lấy "Xấp" hoặc "Ngửa"
            String amountStr = lines[3]; // Dòng cuối cùng là số tiền

            try {
                double amount = Double.parseDouble(amountStr);
                if (amount > 0) {
                    player.sendMessage(ChatColor.GREEN + "Bạn đã đặt cược " + amount + " vào " + choice + ".");
                    this.handleBet(player, choice, amount); // Xử lý cược
                } else {
                    player.sendMessage(ChatColor.RED + "Số tiền cược phải lớn hơn 0.");
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Số tiền cược phải là số hợp lệ.");
            }

            // Sau khi xử lý xong, xóa bảng Sign
            event.getBlock().setType(Material.AIR);
        }
    }

    public void handleBet(Player player, String choice, double amount) {
        // Lưu hoặc xử lý thông tin cược
        player.sendMessage(ChatColor.GREEN + "Cược của bạn đã được xử lý: " + amount + " vào " + choice + ".");
        // Thêm logic tùy ý ở đây...
    }

    public void clearPlayerBet(Player player) {
        guiPlayerBets.remove(player.getName());  // Xóa cược khỏi guiPlayerBets
        playerBetAmounts.remove(player.getName()); // Xóa thông tin tiền cược
    }

    public void handleGUICuoc(Player player, String choice, double betAmount) {
        // Xóa thông tin cược cũ nếu có, đảm bảo không bị lỗi "Bạn đã đặt cược trong phiên này rồi!"
        if (guiPlayerBets.containsKey(player.getName())) {
            clearPlayerBet(player); // Xóa cược cũ trước khi bắt đầu cược mới
        }

        // Kiểm tra số tiền cược hợp lệ
        if (betAmount <= 0) {
            player.sendMessage(ChatColor.RED + "Số tiền cược phải lớn hơn 0!");
            return;
        }

        // Kiểm tra nếu người chơi có đủ tiền
        if (!economy.has(player, betAmount)) {
            double balance = economy.getBalance(player);
            player.sendMessage(ChatColor.RED + "Bạn không đủ tiền để đặt cược! Bạn đang có: " + formatMoney(balance));
            return;
        }

        // Kiểm tra lựa chọn hợp lệ từ Sign GUI
        if (!choice.equalsIgnoreCase("xap") && !choice.equalsIgnoreCase("ngua")) {
            player.sendMessage(ChatColor.RED + "Lựa chọn không hợp lệ, hãy chọn 'xap' hoặc 'ngua'!");
            return;
        }

        // Kiểm tra giới hạn cược
        double maxBetAmount = this.getConfig().getDouble("max-bet-amount", 10000000.0);
        if (betAmount > maxBetAmount) {
            player.sendMessage(ChatColor.RED + "Số tiền cược vượt quá giới hạn cho phép (" + maxBetAmount + ").");
            return;
        }

        // Kiểm tra nếu thời gian cược đã hết
        if (isSessionEnded()) {
            player.sendMessage(ChatColor.RED + "Phiên cược đã kết thúc! Bạn không thể đặt cược nữa.");
            return;
        }

        // Xử lý thời gian cược (Lưu thời gian bắt đầu cược khi người chơi mở GUI)
        long startTime = System.currentTimeMillis();  // Lưu thời gian bắt đầu cược
        long timeLimit = this.getConfig().getLong("bet-time-limit", 10); // Thời gian cược tối đa 10 giây
        long elapsedTime = System.currentTimeMillis() - startTime;

        if (elapsedTime > timeLimit * 1000) {
            player.sendMessage(ChatColor.RED + "Thời gian cược đã hết! Bạn không thể cược nữa.");
            clearPlayerBet(player); // Xóa cược khỏi guiPlayerBets nếu hết thời gian
            return;
        }

        // Rút tiền và lưu thông tin cược
        economy.withdrawPlayer(player, betAmount);
        guiPlayerBets.put(player.getName(), choice); // Lưu cược vào guiPlayerBets
        playerBetAmounts.put(player.getName(), betAmount);

        // Cập nhật tổng tiền cược
        if ("xap".equalsIgnoreCase(choice)) {
            totalXap += betAmount;
            this.getDailyChallengeManager().updateProgress(player, "bet-xap");
        } else if ("ngua".equalsIgnoreCase(choice)) {
            totalNgua += betAmount;
            this.getDailyChallengeManager().updateProgress(player, "bet-ngua");
        }

        // Cập nhật tiến độ thách thức hàng ngày
        String challengeKey = "xap".equalsIgnoreCase(choice) ? "bet-xap" : "bet-ngua";
        this.getDailyChallengeManager().checkChallenge(player, challengeKey);

        // Thông báo cho người chơi
        double remainingBalance = economy.getBalance(player);
        player.sendMessage(ChatColor.GREEN + "Bạn đã cược " + formatMoney(betAmount) + " vào " + choice +
                ". Số tiền còn lại: " + formatMoney(remainingBalance));

        // Kiểm tra kết quả nếu phiên cược đã kết thúc
        if (isSessionEnded()) {
            boolean isWin = checkIfWin(choice); // Hàm kiểm tra kết quả thắng/thua
            leaderboardManager.updateStats(player, isWin, betAmount); // Cập nhật thống kê
            String resultMessage = isWin ? ChatColor.GREEN + "Bạn đã thắng!" : ChatColor.RED + "Bạn đã thua!";
            player.sendMessage(resultMessage);
        }
    }

    public boolean checkIfWin(String choice) {
        // Giả sử logic: nếu lựa chọn của người chơi đúng (xap/ngua) thì thắng
        Random random = new Random();
        boolean isWin = random.nextBoolean(); // Bạn có thể thay đổi phần này để phù hợp với cơ chế của bạn

        // Đưa ra kết quả thắng thua tùy vào cơ chế chơi của bạn.
        return isWin;
    }

    public boolean isSessionEnded() {
        // Logic kiểm tra xem phiên cược đã kết thúc hay chưa
        long currentTime = System.currentTimeMillis();
        long sessionEndTime = getSessionEndTime(); // Lấy thời gian kết thúc phiên cược
        return currentTime > sessionEndTime;
    }

    private long getSessionEndTime() {
        // Giả sử phiên cược kết thúc sau 10 phút
        long sessionDuration = 10 * 60 * 1000; // 10 phút = 600,000ms
        return System.currentTimeMillis() + sessionDuration;
    }

    public static String formatMoney(double amount) {
        // Đọc cấu hình từ plugin
        FileConfiguration config = Bukkit.getPluginManager().getPlugin("XapNguaV2").getConfig();
        boolean isEnabled = config.getBoolean("money-format.enabled", true); // Kiểm tra tính năng bật/tắt
        int decimals = config.getInt("money-format.decimals", 1); // Số chữ số thập phân

        // Nếu tính năng định dạng bị tắt, trả về số nguyên
        if (!isEnabled) {
            return String.format("%.0f", amount);
        }

        // Các hậu tố: nghìn, triệu, tỷ, nghìn tỷ
        String[] suffixes = {"", "K", "M", "B", "T"};
        int index = 0;

        // Chia dần số tiền cho 1000 đến khi nhỏ hơn 1000
        while (amount >= 1000 && index < suffixes.length - 1) {
            amount /= 1000;
            index++;
        }

        return (decimals > 0 && amount % 1 != 0)
                ? String.format("%." + decimals + "f%s", amount, suffixes[index])
                : String.format("%.0f%s", amount, suffixes[index]);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("xmm")) {
            // Kiểm tra người gửi là player
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Lệnh này chỉ có thể được sử dụng bởi người chơi!");
                return true;
            }

            Player player = (Player) sender;

            // lệnh /xmm gui
            if (args.length == 1 && args[0].equalsIgnoreCase("gui")) {
                openBettingGUI(player);
                return true;
            }

            // lệnh /xmm quest
            if (args.length == 1 && args[0].equalsIgnoreCase("quest")) {
                showQuests(player);
                return true;
            }

            // lệnh /xmm leaderboard
            if (args.length == 1 && args[0].equalsIgnoreCase("leaderboard")) {
                LeaderboardManager leaderboardManager = this.getLeaderboardManager();

                if (leaderboardManager == null) {
                    player.sendMessage(ChatColor.RED + "Không thể truy cập bảng xếp hạng. Hãy thử lại sau!");
                    return true;
                }

                List<String> topPlayers = leaderboardManager.getTopPlayersByWinnings(10);

                if (topPlayers == null || topPlayers.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "Hiện chưa có ai trên bảng xếp hạng.");
                } else {
                    player.sendMessage(" ");
                    player.sendMessage(ChatColor.GOLD + "Bảng Xếp Hạng Thắng (Top 10):");
                    for (int i = 0; i < topPlayers.size(); i++) {
                        player.sendMessage(ChatColor.GRAY + " #" + (i + 1) + " " + ChatColor.YELLOW + topPlayers.get(i));
                    }
                    player.sendMessage(" ");
                }
                return true;
            }

            // Nếu lệnh không hợp lệ
            player.sendMessage(" ");
            player.sendMessage(ChatColor.GOLD + "MINIGAME | XẤP NGỬA");
            player.sendMessage(" ");
            player.sendMessage(ChatColor.AQUA + "/xmm gui " + ChatColor.WHITE + "Để mở giao diện chính");
            player.sendMessage(ChatColor.AQUA + "/xmm cuoc " + ChatColor.WHITE + "Để đặt cược nhanh");
            player.sendMessage(ChatColor.AQUA + "/xmm quest " + ChatColor.WHITE + "Các thử thách cược / thắng");
            player.sendMessage(ChatColor.AQUA + "/xmm leaderboard " + ChatColor.WHITE + "Bảng xếp hạng người thắng");
            player.sendMessage(" ");
            player.sendMessage(ChatColor.GOLD + "DEV |" + ChatColor.WHITE + " zonecluck (D.)");
            player.sendMessage(" ");
            return true;
        }
        return false;
    }

    private void showQuests(Player player) {
        // Tải questConfig từ quest.yml
        File questFile = new File(getDataFolder(), "quest.yml");
        FileConfiguration questConfig = YamlConfiguration.loadConfiguration(questFile);

        // Kiểm tra cấu hình có chứa daily-challenges không
        ConfigurationSection quests = questConfig.getConfigurationSection("daily-challenges");
        if (quests == null) {
            player.sendMessage(ChatColor.RED + "Không có nhiệm vụ nào được cấu hình.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "Danh sách nhiệm vụ hằng ngày:");

        // Duyệt qua từng nhiệm vụ và hiển thị thông tin
        for (String key : quests.getKeys(false)) {
            String name = quests.getString(key + ".name");
            String description = quests.getString(key + ".description");
            String rewardName = quests.getString(key + ".reward.name");

            if (name != null && description != null && rewardName != null) {
                player.sendMessage(ChatColor.GRAY + " ┣ " + ChatColor.GRAY + "Tên: " + ChatColor.YELLOW + name);
                player.sendMessage(ChatColor.GRAY + " ┣ " + ChatColor.GRAY + "Mô tả: " + ChatColor.WHITE + description);
                player.sendMessage(ChatColor.GRAY + " ┗ " + ChatColor.GRAY + "Phần thưởng: " + ChatColor.GREEN + rewardName);
                player.sendMessage(ChatColor.GRAY + " ");
            }
        }
    }

    private void saveDefaultQuestConfig() {
        // File quest.yml trong thư mục resources
        File questFile = new File(getDataFolder(), "quest.yml");

        // Kiểm tra nếu quest.yml chưa có trong data folder, sao chép từ resource
        if (!questFile.exists()) {
            saveResource("quest.yml", false);  // Sao chép file từ resources vào thư mục plugin
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("xmm")) {
            List<String> completions = new ArrayList<>();
            if (args.length == 1) {
                completions.add("help");
                completions.add("gui");
                completions.add("quest");
                completions.add("leaderboard");
            } else if (args.length == 2 && args[0].equalsIgnoreCase("cuoc")) {
                completions.add("xap");
                completions.add("ngua");
            } else if (args.length == 3 && args[0].equalsIgnoreCase("cuoc")) {
                return Collections.emptyList();
            }
            return completions;
        }
        return null;
    }
    private void startNewSession() {
        if (currentSession != null) {
            currentSession.cancel();
        }

        currentResult = new Random().nextBoolean() ? "xap" : "ngua";
        sessionDuration = getConfig().getInt("session.duration", 300); // Default to 300 seconds
        remainingTime = sessionDuration;


        currentSession = new BukkitRunnable() {
            @Override
            public void run() {
                if (remainingTime <= 0) {
                    endSession();
                } else {
                    remainingTime--;
                }
            }
        };

        currentSession.runTaskTimer(this, 0L, 20L); // Update every second
        Bukkit.getOnlinePlayers().forEach(player ->
                player.sendMessage(ChatColor.GOLD + "Phiên cược mới bắt đầu! Bạn có " + sessionDuration + " giây để đặt cược!")
        );
        Bukkit.getOnlinePlayers().forEach(player ->
                player.sendMessage(" ")
        );
    }

    private void endSession() {
        currentSessionStatus = SessionStatus.ENDED;
        double multiplier = getConfig().getDouble("win-multiplier", 2.0); // Lấy tỷ lệ thắng từ config (mặc định là 2.0)
        StringBuilder sessionSummary = new StringBuilder();

        // Thông báo kết thúc
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(" "));
        Bukkit.getOnlinePlayers().forEach(player ->
                player.sendMessage(ChatColor.GOLD + "Phiên cược đã kết thúc! Kết quả là " + ChatColor.GREEN + currentResult)
        );

        // Tạo thông báo tổng kết phiên cược
        sessionSummary.append(ChatColor.DARK_GRAY + " ┣" + ChatColor.WHITE + " Tổng cược Xấp: " + ChatColor.GOLD + formatMoney(totalXap));
        sessionSummary.append("\n");
        sessionSummary.append(ChatColor.DARK_GRAY + " ┣" + ChatColor.WHITE + " Tổng cược Ngửa: " + ChatColor.GOLD + formatMoney(totalNgua));
        sessionSummary.append("\n");
        sessionSummary.append(ChatColor.DARK_GRAY + " ┗" + ChatColor.WHITE + " Tổng người đã cược: " + playerBets.size());
        sessionSummary.append("\n");

        // Thông báo tổng kết cho người chơi
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(sessionSummary.toString()));

        // Xử lý kết quả cược cho từng người chơi
        for (Map.Entry<String, String> entry : playerBets.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                String choice = entry.getValue();
                double betAmount = playerBetAmounts.getOrDefault(player.getName(), 0.0);

                // Kiểm tra kết quả cược và xử lý chiến thắng/thua
                if (choice.equals(currentResult)) {
                    double reward = betAmount * multiplier;
                    economy.depositPlayer(player, reward);
                    leaderboardManager.updateStats(player, true, betAmount); // Cập nhật thống kê

                    // Thông báo chiến thắng
                    String formattedReward = formatMoney(reward);
                    player.sendTitle(ChatColor.GREEN + "Chiến thắng!", ChatColor.YELLOW + "Bạn đã thắng " + formattedReward + " tiền", 10, 70, 20);
                } else {
                    // Thông báo thua
                    player.sendTitle(ChatColor.RED + "Thua cuộc!", ChatColor.YELLOW + "Kết quả là " + currentResult, 10, 70, 20);
                }

                // Thêm thông báo chi tiết cho người chơi
                String playerResult = ChatColor.GRAY + "" +
                        ChatColor.YELLOW + "Cược: " + choice +
                        " | Tiền cược: " + formatMoney(betAmount) +
                        " | Tiền thắng: " + (choice.equals(currentResult) ? formatMoney(betAmount * multiplier) : ChatColor.RED + "Thua");
                player.sendMessage(playerResult);
            }
        }

        // Xóa tất cả các cược sau khi phiên kết thúc
        playerBets.clear(); // Xóa tất cả các cược
        playerBetAmounts.clear(); // Xóa số tiền cược
        totalXap = 0;
        totalNgua = 0;

        // Bắt đầu phiên cược mới
        startNewSession();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Vault plugin không được tìm thấy!");
            return false;
        }
        economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
        return economy != null;
    }

    private class PlaceholderAPIExpansion extends PlaceholderExpansion {

        private final XapNguaV2 plugin;

        public PlaceholderAPIExpansion(XapNguaV2 plugin) {
            this.plugin = plugin;
        }

        @Override
        public String getIdentifier() {
            return "xapngua";
        }

        @Override
        public String getAuthor() {
            return plugin.getDescription().getAuthors().toString();
        }

        @Override
        public String getVersion() {
            return plugin.getDescription().getVersion();
        }

        @Override
        public String onPlaceholderRequest(Player player, String identifier) {

            if (identifier.equals("total_xap")) {
                return formatMoney(totalXap);
            }

            if (identifier.equals("total_ngua")) {
                return formatMoney(totalNgua);
            }

            if (identifier.equals("players")) {
                return playerBets.getOrDefault(player.getName(), "Không đặt cược");
            }

            // Thời gian còn lại của phiên
            if (identifier.equals("session_duration")) {
                return String.valueOf(remainingTime);
            }

            // Tổng số trận chơi của người chơi
            if (identifier.equals("games_played")) {
                PlayerStats stats = leaderboardManager.getPlayerStats(player);
                return String.valueOf(stats.getGamesPlayed());
            }

            // Tổng số trận thắng của người chơi
            if (identifier.equals("games_won")) {
                PlayerStats stats = leaderboardManager.getPlayerStats(player);
                return String.valueOf(stats.getGamesWon());
            }

            // Tổng tiền thắng của người chơi
            if (identifier.equals("total_winnings")) {
                PlayerStats stats = leaderboardManager.getPlayerStats(player);
                return formatMoney(stats.getTotalWinnings());
            }

            if (identifier.equals("total_bet_amount")) {
                PlayerStats stats = leaderboardManager.getPlayerStats(player);
                return formatMoney(stats.getTotalBets());
            }

            if (identifier.equals("win_rate")) {
                PlayerStats stats = leaderboardManager.getPlayerStats(player);
                return String.format("%.2f%%", stats.getWinRate());
            }
            return null;
        }
    }
}