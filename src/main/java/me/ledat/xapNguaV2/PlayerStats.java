package me.ledat.xapNguaV2;

import org.bukkit.configuration.ConfigurationSection;

public class PlayerStats {
    private final String playerName;
    private int gamesPlayed = 0;
    private int gamesWon = 0;
    private double totalBets = 0.0;
    private double totalWinnings = 0.0;

    public PlayerStats(String playerName) {
        this.playerName = playerName != null ? playerName : "Unknown";
    }

    // Các phương thức getter và setter
    public void incrementGamesPlayed() { gamesPlayed++; }
    public void incrementGamesWon() { gamesWon++; }
    public void addBetAmount(double amount) { totalBets += amount; }
    public void addTotalWinnings(double amount) { totalWinnings += amount; }

    public String getPlayerName() { return playerName; }
    public int getGamesPlayed() { return gamesPlayed; }
    public int getGamesWon() { return gamesWon; }
    public double getTotalBets() { return totalBets; }
    public double getTotalWinnings() { return totalWinnings; }
    public double getWinRate() { return gamesPlayed == 0 ? 0.0 : (double) gamesWon / gamesPlayed * 100; }


    public void saveToConfig(final ConfigurationSection section) {
        if (section == null) {
            throw new IllegalArgumentException("ConfigurationSection không được null.");
        }
        section.set("playerName", playerName);
        section.set("gamesPlayed", gamesPlayed);
        section.set("gamesWon", gamesWon);
        section.set("totalBets", totalBets);
        section.set("totalWinnings", totalWinnings);
    }

    // Tải từ YAML
    public static PlayerStats loadFromConfig(final ConfigurationSection section) {
        if (section == null) {
            throw new IllegalArgumentException("ConfigurationSection không được null.");
        }
        String name = section.getString("playerName");
        PlayerStats stats = new PlayerStats(name != null ? name : "Unknown");
        stats.gamesPlayed = section.getInt("gamesPlayed", 0);
        stats.gamesWon = section.getInt("gamesWon", 0);
        stats.totalBets = section.getDouble("totalBets", 0.0);
        stats.totalWinnings = section.getDouble("totalWinnings", 0.0);
        return stats;
    }
}
