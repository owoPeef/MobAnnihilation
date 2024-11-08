package ru.peef.mobannihilation.game;

import org.bukkit.*;
import ru.peef.mobannihilation.MobAnnihilation;
import ru.peef.mobannihilation.ScoreboardUtils;
import ru.peef.mobannihilation.game.mobs.GameMob;
import ru.peef.mobannihilation.game.players.GamePlayer;
import ru.peef.mobannihilation.game.players.PlayerData;
import ru.peef.mobannihilation.game.players.PlayerDataHandler;
import ru.peef.mobannihilation.game.players.PlayerManager;

import java.util.*;

public class GameManager {
    // TODO: Все это получать из конфига
    public static World BASIC_WORLD, ARENA_WORLD;
    public static Location BASIC_SPAWN, ARENA_SPAWN;
    public static Location MOB_SPAWN;
    public static int SHOW_TOP_PLAYERS_COUNT = 5;
    public static boolean hideOnArena = false;
    public static int scoreboardUpdateSeconds = 15;

    // Это нет
    public static List<GamePlayer> PLAYERS_ON_ARENA = new ArrayList<>();

    public static void init() {
        // TODO: Изменение названия миров в конфиге (и точек спавна)
        BASIC_WORLD = Bukkit.getWorld("lobby");
        ARENA_WORLD = Bukkit.createWorld(WorldCreator.name("arena"));

        BASIC_SPAWN = new Location(BASIC_WORLD, 0.5f, 20, 0.5f);
        ARENA_SPAWN = new Location(ARENA_WORLD, 0.5f, 18, 0.5f, -90, 0);

        MOB_SPAWN = new Location(ARENA_WORLD, 10.5f, 7, 0.5f);

        Bukkit.getScheduler().runTaskTimer(MobAnnihilation.getInstance(), () -> PlayerManager.PLAYERS.forEach(ScoreboardUtils::updateScoreboard), 0, scoreboardUpdateSeconds * 20L);
    }

    public static Map<String, PlayerData> getTopByLevel() {
        Map<String, PlayerData> players = PlayerDataHandler.loadPlayers();
        List<Map.Entry<String, PlayerData>> entries = new ArrayList<>(players.entrySet());
        entries.sort((entry1, entry2) -> Double.compare(entry2.getValue().level, entry1.getValue().level));
        Map<String, PlayerData> sortedPlayers = new LinkedHashMap<>();
        for (Map.Entry<String, PlayerData> entry : entries) {
            sortedPlayers.put(entry.getKey(), entry.getValue());
        }

        return sortedPlayers;
    }
}
