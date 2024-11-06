package ru.peef.mobannihilation.game.players;

import com.google.gson.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ru.peef.mobannihilation.MobAnnihilation;
import ru.peef.mobannihilation.game.items.RarityItem;
import ru.peef.mobannihilation.game.items.RarityItemAdapter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PlayerDataHandler {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(RarityItem.class, new RarityItemAdapter())
            .setPrettyPrinting()
            .create();
    private static File file = null;

    public static void init() {
        file = new File(MobAnnihilation.getInstance().getDataFolder(), "players.json");
        if (!file.exists()) {
            try {
                MobAnnihilation.getInstance().getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void savePlayer(Player player, double level) {
        try {
            Map<String, Double> playersData = loadAllPlayers();
            playersData.put(player.getName(), level);

            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(playersData, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Double loadPlayer(Player player) { return loadPlayer(player.getName()); }
    public static Double loadPlayer(String name) {
        try {
            Map<String, Double> playersData = loadAllPlayers();
            return playersData.getOrDefault(name, 1.0);
        } catch (Exception e) {
            return 1.0;
        }
    }

    public static void deletePlayer(Player player) { deletePlayer(player.getName()); }
    public static void deletePlayer(String name) {
        try {
            Map<String, Double> playersData = loadAllPlayers();

            if (playersData.remove(name) != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    gson.toJson(playersData, writer);
                }
            } else {
                MobAnnihilation.getInstance().getLogger().info(ChatColor.GOLD + name + ChatColor.RED + " >> игрок не найден в файле " + ChatColor.GRAY + "(удаление)");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasPlayer(Player player) { return hasPlayer(player.getName()); }
    public static boolean hasPlayer(String name) {
        try {
            Map<String, Double> playersData = loadAllPlayers();

            return playersData.containsKey(name);
        } catch (IOException e) {
            return false;
        }
    }

    private static Map<String, GamePlayer> loadAllPlayers() throws IOException {
        if (!file.exists()) {
            file.createNewFile();
            return new HashMap<>();
        }

        try (FileReader reader = new FileReader(file)) {
            JsonElement element = new JsonParser().parse(reader);
            if (element.isJsonObject()) {
                Map<String, GamePlayer> playersData = new HashMap<>();
                JsonObject jsonObject = element.getAsJsonObject();

                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    playersData.put(entry.getKey(), entry.getValue());
                }
                return playersData;
            }
            return new HashMap<>();
        }
    }
}
