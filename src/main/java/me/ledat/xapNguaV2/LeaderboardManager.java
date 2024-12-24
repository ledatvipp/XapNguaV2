package me.ledat.xapNguaV2;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardManager {
    private final XapNguaV2 plugin;
    private final Map<UUID, PlayerStats> playerStats = new HashMap<>();
    private final File leaderboardFile;
    private FileConfiguration leaderboardConfig;

    public LeaderboardManager(XapNguaV2 plugin) {
        this.plugin = plugin;

        leaderboardFile = new File(plugin.getDataFolder(), "leaderboard.yml");
        if (!leaderboardFile.exists()) {
            try {
                leaderboardFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                plugin.getLogger().severe("Không thể tạo file leaderboard.yml!");
            }
        }

        leaderboardConfig = YamlConfiguration.loadConfiguration(leaderboardFile);
        loadStats(); // Tải dữ liệu ngay khi khởi tạo
    }

    // In bảng xếp hạng
    public void printLeaderboard(Player player) {
        List<PlayerStats> topStats = playerStats.values().stream()
                .sorted(Comparator.comparingDouble(PlayerStats::getTotalWinnings).reversed())
                .limit(10)
                .collect(Collectors.toList());

        if (topStats.isEmpty()) {
            player.sendMessage("§cKhông có ai trên bảng xếp hạng.");
        } else {
            player.sendMessage("§6Bảng Xếp Hạng Chiến Thắng");
            for (int i = 0; i < topStats.size(); i++) {
                PlayerStats stats = topStats.get(i);
                player.sendMessage("§e#" + (i + 1) + " §6" + stats.getPlayerName() + " - §c" +
                        plugin.formatMoney(stats.getTotalWinnings()) + " thắng");
            }
        }
    }

    // Lưu dữ liệu
    public void saveStats() {
        leaderboardConfig = new YamlConfiguration(); // Tạo mới config
        for (UUID playerId : playerStats.keySet()) {
            PlayerStats stats = playerStats.get(playerId);
            leaderboardConfig.createSection(playerId.toString());
            stats.saveToConfig(leaderboardConfig.getConfigurationSection(playerId.toString()));
        }

        try {
            leaderboardConfig.save(leaderboardFile);
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Không thể lưu file leaderboard.yml!");
        }
    }

    // Tải dữ liệu
    private void loadStats() {
        for (String key : leaderboardConfig.getKeys(false)) {
            UUID playerId;
            try {
                playerId = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Bỏ qua key không hợp lệ: " + key);
                continue;
            }

            PlayerStats stats = PlayerStats.loadFromConfig(leaderboardConfig.getConfigurationSection(key));
            playerStats.put(playerId, stats);
        }
    }

    // Cập nhật thống kê
    public void updateStats(Player player, boolean isWin, double betAmount) {
        UUID playerId = player.getUniqueId();
        PlayerStats stats = playerStats.getOrDefault(playerId, new PlayerStats(player.getName()));

        stats.incrementGamesPlayed();
        stats.addBetAmount(betAmount);

        if (isWin) {
            stats.incrementGamesWon();
            stats.addTotalWinnings(betAmount);
        }

        playerStats.put(playerId, stats);
        saveStats(); // Lưu ngay sau khi cập nhật
    }

    // Lấy danh sách top người chơi
    public List<String> getTopPlayersByWinnings(int limit) {
        return playerStats.values().stream()
                .sorted(Comparator.comparingDouble(PlayerStats::getTotalWinnings).reversed())
                .limit(limit)
                .map(stats -> stats.getPlayerName() + ": " + plugin.formatMoney(stats.getTotalWinnings()))
                .collect(Collectors.toList());
    }

    // Lấy thống kê của người chơi
    public PlayerStats getPlayerStats(Player player) {
        return playerStats.getOrDefault(player.getUniqueId(), new PlayerStats(player.getName()));
    }
}
