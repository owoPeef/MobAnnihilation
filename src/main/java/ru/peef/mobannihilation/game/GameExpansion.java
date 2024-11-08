package ru.peef.mobannihilation.game;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.peef.mobannihilation.game.players.PlayerData;

import java.util.Map;

public class GameExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "mobannihilation";
    }

    @Override
    public @NotNull String getAuthor() {
        return "owoPeef";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.startsWith("top")) {
            String top = params.replace("top", "");
            try {
                int topNum = Integer.parseInt(top);
                topNum--;
                Map<String, PlayerData> topPlayers = GameManager.getTopByLevel();

                int i = 0;
                for (Map.Entry<String, PlayerData> playerData : topPlayers.entrySet()) {
                    if (i == topNum) return ChatColor.GOLD + (playerData.getKey() + " - ") + ChatColor.AQUA + (playerData.getValue().getLevel() + " ур.");;
                    i++;
                }

                return ChatColor.YELLOW + " - ";
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
