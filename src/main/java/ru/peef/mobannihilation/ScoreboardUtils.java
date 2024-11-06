package ru.peef.mobannihilation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import ru.peef.mobannihilation.game.items.RarityItem;
import ru.peef.mobannihilation.game.players.GamePlayer;

public class ScoreboardUtils {
    private static Scoreboard getScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective(ChatColor.GREEN + (ChatColor.BOLD + "Annihilation"), player.getName());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        return scoreboard;
    }

    public static void updateScoreboard(GamePlayer gamePlayer) {
        Scoreboard scoreboard = getScoreboard(gamePlayer.getPlayer());
        Objective objective = scoreboard.getObjective(ChatColor.GREEN + (ChatColor.BOLD + "Annihilation"));

        objective.getScore(ChatColor.AQUA + "Вы: " + ChatColor.GOLD + gamePlayer.getName()).setScore(10);
        objective.getScore(" ").setScore(9);
        objective.getScore(ChatColor.AQUA + "Уровень: " + ChatColor.GOLD + gamePlayer.getLevel()).setScore(8);
        objective.getScore(ChatColor.AQUA + "Прогресс: " + ChatColor.GOLD + gamePlayer.getProgress() + "% " + gamePlayer.lastProg).setScore(7);
        objective.getScore("  ").setScore(6);
        objective.getScore(ChatColor.AQUA + "Рун Урона: " + ChatColor.YELLOW + gamePlayer.getRarityItems(RarityItem.Boost.DAMAGE).size() + " (" + Utils.roundTo(gamePlayer.getRarityPercent(RarityItem.Boost.DAMAGE), 1) + "%)").setScore(5);
        objective.getScore(ChatColor.AQUA + "Рун Защиты: " + ChatColor.YELLOW + gamePlayer.getRarityItems(RarityItem.Boost.PROTECTION).size() + " (" + Utils.roundTo(gamePlayer.getRarityPercent(RarityItem.Boost.PROTECTION), 1) + "%)").setScore(4);
        objective.getScore(ChatColor.AQUA + "Рун Скорости: " + ChatColor.YELLOW + gamePlayer.getRarityItems(RarityItem.Boost.SPEED).size() + " (" + Utils.roundTo(gamePlayer.getRarityPercent(RarityItem.Boost.SPEED), 1) + ")").setScore(3);
        objective.getScore(ChatColor.AQUA + "Рун Скор. Атк: " + ChatColor.YELLOW + gamePlayer.getRarityItems(RarityItem.Boost.ATTACK_SPEED).size() + " (" + Utils.roundTo(gamePlayer.getRarityPercent(RarityItem.Boost.ATTACK_SPEED), 1) + ")").setScore(2);

        gamePlayer.getPlayer().setScoreboard(scoreboard);

        gamePlayer.lastProg = "";
    }
}