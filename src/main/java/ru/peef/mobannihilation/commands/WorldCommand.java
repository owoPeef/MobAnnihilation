package ru.peef.mobannihilation.commands;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WorldCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        Player player = (Player) sender;

        if (args.length > 0) {
            switch (args[0]) {
                case "tp": case "teleport": {
                    if (args.length == 2) {
                        String worldName = args[1];
                        World world = Bukkit.getWorld(worldName);

                        if (world != null) {
                            player.teleport(new Location(world, 0.5, 25, 0.5));
                            player.sendMessage(ChatColor.AQUA + "Телепортирую в мир " + ChatColor.GOLD + world.getName());
                        } else {
                            player.sendMessage(ChatColor.RED + "Мир не найден!" + ChatColor.GOLD + "(" + worldName + ")");
                        }
                    }
                    break;
                }
                case "load": {
                    if (args.length == 2) {
                        String worldName = args[1];
                        World world = Bukkit.createWorld(WorldCreator.name(worldName));

                        if (world != null) {
                            player.sendMessage(ChatColor.GREEN + "Мир был успешно подгружен!");
                        } else {
                            player.sendMessage(ChatColor.RED + "Мир не найден!");
                        }
                    }
                    break;
                }
                case "list": {
                    player.sendMessage(ChatColor.AQUA + "Найденные миры:");
                    for (World world : Bukkit.getWorlds()) {
                        player.sendMessage(ChatColor.GOLD + world.getName() + " - " + world.getWorldType().getName().toUpperCase());
                    }
                    break;
                }
            }
        }

        return true;
    }
}
