package ru.peef.mobannihilation;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.peef.mobannihilation.commands.GameCommand;
import ru.peef.mobannihilation.commands.NPCCommand;
import ru.peef.mobannihilation.game.GameManager;
import ru.peef.mobannihilation.game.npcs.NPCDataHandler;
import ru.peef.mobannihilation.game.players.GamePlayer;
import ru.peef.mobannihilation.game.players.PlayerDataHandler;
import ru.peef.mobannihilation.game.players.PlayerListener;
import ru.peef.mobannihilation.game.players.PlayerManager;
import ru.peef.mobannihilation.game.npcs.NPC;
import ru.peef.mobannihilation.game.npcs.NPCManager;

public final class MobAnnihilation extends JavaPlugin {

    @Override
    public void onEnable() {
        GameManager.init();

        getCommand("game").setExecutor(new GameCommand());
        getCommand("npc").setExecutor(new NPCCommand());
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        Bukkit.getScheduler().runTaskLater(this, () -> {
            for (World world : getServer().getWorlds()) {
                world.getEntities().forEach(Entity::remove);
            }

            PlayerDataHandler.init();
            NPCDataHandler.init();

            NPCManager.init();
        }, 5L);
    }

    @Override
    public void onDisable() {
        PlayerManager.PLAYERS.forEach(GamePlayer::save);
        NPCManager.CHARACTERS.forEach(NPC::despawn);

        GameManager.BASIC_WORLD.getEntities().forEach(Entity::remove);
        GameManager.ARENA_WORLD.getEntities().forEach(Entity::remove);
    }

    public static Plugin getInstance() { return getProvidingPlugin(MobAnnihilation.class); }
}
