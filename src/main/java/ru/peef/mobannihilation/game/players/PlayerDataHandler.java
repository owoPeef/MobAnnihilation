package ru.peef.mobannihilation.game.players;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.bukkit.entity.Player;
import ru.peef.mobannihilation.MobAnnihilation;
import ru.peef.mobannihilation.game.items.RarityItem;
import ru.peef.mobannihilation.game.items.RarityItemAdapter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
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

    public static void savePlayer(GamePlayer player) {
        try {
            Map<String, PlayerData> playersData = loadPlayers();
            if (playersData == null) playersData = new HashMap<>();

            PlayerData data = PlayerData.create(player);
            playersData.put(player.getName(), data);

            try (FileWriter writer = new FileWriter(file, false)) {  // false для перезаписи файла
                gson.toJson(playersData, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasPlayer(Player player) { return hasPlayer(player.getName()); }
    public static boolean hasPlayer(String name) {
        Map<String, PlayerData> players = loadPlayers();
        return players != null && players.containsKey(name);
    }

    public static Map<String, PlayerData> loadPlayers() {
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, PlayerData>>() {}.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    public static PlayerData getPlayerData(Player player) { return getPlayerData(player.getName()); }
    public static PlayerData getPlayerData(String name) {
        Map<String, PlayerData> playersData = loadPlayers();

        return playersData != null ? playersData.get(name) : null;
    }
}
