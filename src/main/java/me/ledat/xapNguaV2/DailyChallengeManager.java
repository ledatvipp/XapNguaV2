package me.ledat.xapNguaV2;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class DailyChallengeManager {

    private final XapNguaV2 plugin;
    private final Map<String, DailyChallenge> dailyChallenges = new HashMap<>();
    private final Map<String, DailyChallengeProgress> playerProgress = new HashMap<>();

    public DailyChallengeManager(XapNguaV2 plugin) {
        this.plugin = plugin;
        loadChallenges();
    }

    // Trong phương thức loadChallenges
    private void loadChallenges() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        // Kiểm tra xem cấu hình có chứa "daily-challenges" hay không
        if (plugin.getConfig().contains("daily-challenges")) {
            for (String key : plugin.getConfig().getConfigurationSection("daily-challenges").getKeys(false)) {
                String name = plugin.getConfig().getString("daily-challenges." + key + ".name");
                String description = plugin.getConfig().getString("daily-challenges." + key + ".description");
                String type = plugin.getConfig().getString("daily-challenges." + key + ".type");
                int target = plugin.getConfig().getInt("daily-challenges." + key + ".target");
                String rewardName = plugin.getConfig().getString("daily-challenges." + key + ".reward.name");
                String rewardType = plugin.getConfig().getString("daily-challenges." + key + ".reward.type");
                int rewardAmount = plugin.getConfig().getInt("daily-challenges." + key + ".reward.amount");

                // Lấy giá trị rewardItem từ cấu hình, nếu không có thì dùng giá trị mặc định
                String rewardItem = plugin.getConfig().getString("daily-challenges." + key + ".reward.item", "STONE"); // "STONE" là giá trị mặc định

                // Khởi tạo DailyChallenge với đầy đủ 7 tham số
                DailyChallenge challenge = new DailyChallenge(name, description, type, target, rewardType, rewardAmount, rewardItem);
                dailyChallenges.put(key, challenge);
            }
        } else {
            plugin.getLogger().warning("Không tìm thấy mục 'daily-challenges' trong cấu hình.");
        }
    }

    // Kiểm tra xem người chơi đã hoàn thành thách thức chưa
    public void checkChallenge(Player player, String challengeKey) {
        DailyChallenge challenge = dailyChallenges.get(challengeKey);
        if (challenge != null) {
            DailyChallengeProgress progress = playerProgress.getOrDefault(player.getName(), new DailyChallengeProgress());
            boolean isComplete = false;

            switch (challenge.getType()) {
                case "bet-xap":
                    if (progress.getBetXapCount() >= challenge.getTarget()) {
                        isComplete = true;
                    }
                    break;
                case "bet-ngua":
                    if (progress.getBetNguaCount() >= challenge.getTarget()) {
                        isComplete = true;
                    }
                    break;
                case "win-streak":
                    if (progress.getWinStreakCount() >= challenge.getTarget()) {
                        isComplete = true;
                    }
                    break;
            }

            if (isComplete) {
                giveReward(player, challenge);
                progress.reset();
                playerProgress.put(player.getName(), progress);
            } else {
                player.sendMessage("Bạn vẫn chưa hoàn thành thách thức này.");
            }
        }
    }

    // Cấp phần thưởng cho người chơi khi hoàn thành thách thức
    private void giveReward(Player player, DailyChallenge challenge) {
        switch (challenge.getRewardType()) {
            case "money":
                plugin.getEconomy().depositPlayer(player, challenge.getRewardAmount());
                player.sendMessage("Bạn đã nhận được " + challenge.getRewardAmount() + " tiền!");
                break;
            case "item":
                player.getInventory().addItem(new ItemStack(Material.valueOf(challenge.getRewardItem()), challenge.getRewardAmount()));
                player.sendMessage("Bạn đã nhận được " + challenge.getRewardAmount() + " " + challenge.getRewardItem());
                break;
            // Có thể mở rộng các loại thưởng khác như thẻ thưởng, xp, etc.
        }
    }

    // Kiểm tra và cập nhật tiến độ cược của người chơi
    public void updateProgress(Player player, String type) {
        DailyChallengeProgress progress = playerProgress.getOrDefault(player.getName(), new DailyChallengeProgress());

        switch (type) {
            case "bet-xap":
                progress.incrementBetXap();
                break;
            case "bet-ngua":
                progress.incrementBetNgua();
                break;
            case "win-streak":
                progress.incrementWinStreak();
                break;
        }

        playerProgress.put(player.getName(), progress);
    }

    // Lớp con dùng để lưu trữ tiến độ thách thức của người chơi
    public static class DailyChallengeProgress {
        private int betXapCount = 0;
        private int betNguaCount = 0;
        private int winStreakCount = 0;

        public void incrementBetXap() {
            betXapCount++;
        }

        public void incrementBetNgua() {
            betNguaCount++;
        }

        public void incrementWinStreak() {
            winStreakCount++;
        }

        public int getBetXapCount() {
            return betXapCount;
        }

        public int getBetNguaCount() {
            return betNguaCount;
        }

        public int getWinStreakCount() {
            return winStreakCount;
        }

        public void reset() {
            betXapCount = 0;
            betNguaCount = 0;
            winStreakCount = 0;
        }
    }

    // Lớp thách thức hàng ngày
    public static class DailyChallenge {
        private final String name;
        private final String description;
        private final String type;
        private final int target;
        private final String rewardType;
        private final int rewardAmount;
        private final String rewardItem;

        public DailyChallenge(String name, String description, String type, int target, String rewardType, int rewardAmount, String rewardItem) {
            this.name = name;
            this.description = description;
            this.type = type;
            this.target = target;
            this.rewardType = rewardType;
            this.rewardAmount = rewardAmount;
            this.rewardItem = rewardItem;
        }

        // Getter methods
        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getType() {
            return type;
        }

        public int getTarget() {
            return target;
        }

        public String getRewardType() {
            return rewardType;
        }

        public int getRewardAmount() {
            return rewardAmount;
        }

        public String getRewardItem() {
            return rewardItem;
        }
    }
}
