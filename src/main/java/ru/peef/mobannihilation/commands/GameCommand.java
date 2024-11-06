package ru.peef.mobannihilation.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.peef.mobannihilation.game.items.RarityItem;
import ru.peef.mobannihilation.game.players.GamePlayer;
import ru.peef.mobannihilation.game.players.PlayerDataHandler;
import ru.peef.mobannihilation.game.players.PlayerManager;

public class GameCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        Player player = (Player) sender;
        GamePlayer gamePlayer = PlayerManager.get(player);

        if (gamePlayer != null) {
            if (args.length == 1) {
                switch (args[0]) {
                    case "info":
                        player.sendMessage(
                                ChatColor.GREEN + "Список всех команд:\n" +
                                ChatColor.GOLD + "/game info" + ChatColor.AQUA + " - вывести это сообщение\n" +
                                ChatColor.GOLD + "/game stats" + ChatColor.AQUA + " - получить свою статистику\n" +
                                ((player.isOp() || player.hasPermission("game.stats.other")) ? ChatColor.GOLD + "/game stats <ник>" + ChatColor.AQUA + " - получить статистику игрока\n" : "") +
                                ChatColor.GOLD + "/game join" + ChatColor.AQUA + " - войти на арену\n" +
                                ChatColor.GOLD + "/game leave" + ChatColor.AQUA + " - выйти с арены\n" +
                                ((player.isOp() || player.hasPermission("game.add")) ? ChatColor.GOLD + "/game add_level <число>" + ChatColor.AQUA + " - добавить <число> уровней\n" + ChatColor.GOLD + "/game add_progress <число>" + ChatColor.AQUA + " - добавить <число>% прогресса (от 1 до 100)" : "")
                        );
                        break;
                    case "join":
                        gamePlayer.joinArena();
                        break;
                    case "leave":
                        gamePlayer.leaveArena();
                        break;
                    case "item":
                        gamePlayer.addItem(RarityItem.getRandom(gamePlayer));
                        break;
                    case "stats":
                        player.sendMessage(gamePlayer.getStatsMessage());
                        break;
                }
            } else if (args.length == 2) {
                if (args[0].equals("clear")) {
                    String playerNick = args[1];

                    if (PlayerDataHandler.hasPlayer(playerNick)) {
                        PlayerDataHandler.deletePlayer(playerNick);
                        player.sendMessage(ChatColor.GREEN + "Весь прогресс игрока был очищен!");

                        Player delPlayer = Bukkit.getPlayer(playerNick);
                        if (delPlayer != null && delPlayer.isOnline()) {
                            delPlayer.kickPlayer(ChatColor.RED + "Вас прогресс очищен! Перезайдите");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Игрок не найден!");
                    }
                }

                if (args[0].equals("add_progress") || args[0].equals("add_level") || args[0].equals("add_lvl")) {
                    try {
                        // ADD
                        int count = Integer.parseInt(args[1]);

                        if (args[0].equals("add_progress")) {
                            if (count > 0 && count <= 100) {
                                gamePlayer.addProgress(count);
                                player.sendMessage(ChatColor.AQUA + "Добавлено прогресса: " + ChatColor.GOLD + count + "%");
                                gamePlayer.save();
                            } else {
                                player.sendMessage(ChatColor.RED + "Прогресс должен иметь диапозон от 1 до 100!");
                            }
                        } else {
                            gamePlayer.addLevel(count);
                            player.sendMessage(ChatColor.AQUA + "Добавлено уровней: " + ChatColor.GOLD + count);
                            gamePlayer.save();
                        }
                    } catch (NumberFormatException ignored) { }
                } else if (args[0].equals("stats")) {
                    if (PlayerDataHandler.hasPlayer(args[1])) {
                        player.sendMessage(GamePlayer.fromFile(args[1]).getStatsMessage());
                    } else {
                        player.sendMessage(ChatColor.RED + "Игрок не найден!");
                    }
                }
            }
        }

        return true;
    }
}
