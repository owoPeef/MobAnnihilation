package ru.peef.mobannihilation.game;

import org.bukkit.*;
import ru.peef.mobannihilation.MobAnnihilation;
import ru.peef.mobannihilation.ScoreboardUtils;
import ru.peef.mobannihilation.game.mobs.GameMob;
import ru.peef.mobannihilation.game.players.GamePlayer;
import ru.peef.mobannihilation.game.players.PlayerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class GameManager {
    // TODO: Все это получать из конфига
    public static World BASIC_WORLD, ARENA_WORLD;
    public static Location BASIC_SPAWN, ARENA_SPAWN;
    public static Location MOB_SPAWN;
    public static List<GameMob> SPAWNED_MOBS = new ArrayList<>();
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

    public static List<GameMob> getSpawnedMobs() {
        List<GameMob> mobs = new ArrayList<>();

        SPAWNED_MOBS.forEach(mob -> {
            if (mob != null) mobs.add(mob);
        });

        return mobs;
    }

    public static GameMob getByUniqueId(UUID uniqueId) {
        AtomicReference<GameMob> mob = new AtomicReference<>();
        SPAWNED_MOBS.forEach(gameMob -> { if (gameMob.uniqueId.equals(uniqueId)) mob.set(gameMob); });
        return mob.get();
    }
}
