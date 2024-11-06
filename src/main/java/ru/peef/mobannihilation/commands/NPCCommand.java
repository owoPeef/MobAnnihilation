package ru.peef.mobannihilation.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.peef.mobannihilation.game.npcs.NPC;
import ru.peef.mobannihilation.game.npcs.NPCDataHandler;
import ru.peef.mobannihilation.game.npcs.NPCManager;
import ru.peef.mobannihilation.holograms.Hologram;

public class NPCCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        Player player = (Player) sender;

        if (args.length == 1) {
            if (args[0].equals("count")) player.sendMessage(ChatColor.AQUA + "Количество NPC: " + ChatColor.GOLD + NPCManager.CHARACTERS.size());
        } else if (args.length == 2) {
            // npc create <name>
            if (args[0].equals("create")) {
                String name = args[1];

                if (NPCDataHandler.hasNPC(name)) {
                    player.sendMessage(ChatColor.RED + "NPC с таким именем уже существует!");
                } else {
                    Location loc = player.getLocation();
                    NPC npc = new NPC(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), name, "", loc.getWorld().getName(), "test command");

                    npc.spawn();
                    npc.save();
                    NPCManager.CHARACTERS.add(npc);

                    player.sendMessage(ChatColor.GREEN + "NPC создан! " + ChatColor.GOLD + "[" + name + "]");
                }
            } else if (args[0].equals("hologram") && args[1].equals("despawn")) {
                player.sendMessage(String.format(ChatColor.AQUA + "Удалено голограмм: %s%s", ChatColor.GOLD, Hologram.HOLOGRAMS.size()));
                Hologram.HOLOGRAMS.forEach(Hologram::remove);
            }
        } else if (args.length >= 3) {
            // hologram <name> <text1> <text2>
            if (args[0].equals("hologram")) {
                Hologram hologram = Hologram.get(args[1]);

                if (hologram != null) {
                    StringBuilder newText = new StringBuilder();

                    for (int i = 2; i < args.length; i++) {
                        newText.append(args[i]).append(" ");
                    }

                    hologram.setText(newText.toString());
                    player.sendMessage(ChatColor.AQUA + "Текст изменен на: " + hologram.getText());
                }
            }
        }

        return true;
    }
}
