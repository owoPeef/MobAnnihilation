package ru.peef.mobannihilation.game;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.registry.WorldData;
import org.bukkit.Location;
import org.bukkit.World;
import ru.peef.mobannihilation.MobAnnihilation;
import ru.peef.mobannihilation.game.players.GamePlayer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Arena {
    public File file;
    public String arenaName;
    public int spawnX, spawnY, spawnZ;
    public int mobSpawnX, mobSpawnY, mobSpawnZ;
    public World world;
    public Operation operation;
    public List<GamePlayer> arenaPlayers = new ArrayList<>();
    EditSession editSession;
    int arenaIndex;

    int arenaOffset = 50;

    public Arena(World world, String name, int spawnX, int spawnY, int spawnZ, int mobSpawnX, int mobSpawnY, int mobSpawnZ) {
        this.world = world;
        this.arenaName = name;
        file = new File(WorldEditPlugin.getPlugin(WorldEditPlugin.class).getDataFolder() + "\\schematics\\" + arenaName + ".schematic");

        arenaIndex = GameManager.PLAYERS_ON_ARENA.size()+1;

        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.spawnZ = spawnZ;

        this.mobSpawnX = mobSpawnX;
        this.mobSpawnY = mobSpawnY;
        this.mobSpawnZ = mobSpawnZ;
    }

    public void add(GamePlayer... players) {
        arenaPlayers.addAll(Arrays.asList(players));
    }

    public Location getPlayerSpawn() {
        int spawnZ = this.spawnZ + Math.max(arenaOffset, arenaIndex * arenaOffset);
        return new Location(world, spawnX+.5f, spawnY, spawnZ+.5f, -90, 0);
    }

    public Location getMobSpawn() {
        int mobSpawnZ = this.mobSpawnZ + Math.max(arenaOffset, arenaIndex * arenaOffset);
        return new Location(world, mobSpawnX+.5f, mobSpawnY, mobSpawnZ+.5f);
    }

    public void load(int x, int y, int z) {
        if (!file.exists()) {
            MobAnnihilation.getInstance().getLogger().info("File (" + file.getName() + ") not found!");
            return;
        }

        z += Math.max(arenaOffset, arenaIndex * arenaOffset);

        Clipboard clipboard;

        ClipboardFormat format = ClipboardFormat.findByFile(file);
        if (format != null) {
            try {
                com.sk89q.worldedit.world.World editWorld = BukkitUtil.getLocalWorld(world);
                WorldData worldData = editWorld.getWorldData();

                ClipboardReader reader = format.getReader(Files.newInputStream(file.toPath()));
                clipboard = reader.read(worldData);
                editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(editWorld, -1);
                operation = new ClipboardHolder(clipboard, worldData)
                        .createPaste(editSession.getSurvivalExtent(), worldData)
                        .to(Vector.toBlockPoint(x, y, z))
                        .ignoreAirBlocks(false)
                        .build();

                Operations.complete(operation);

                MobAnnihilation.getInstance().getLogger().info(String.format("Set schematic on %s %s %s", x, y, z));

                arenaPlayers.forEach(gamePlayer -> {
                    int spawnZ = this.spawnZ + Math.max(arenaOffset, arenaIndex * arenaOffset);
                    gamePlayer.getPlayer().teleport(new Location(world, spawnX+.5f, spawnY, spawnZ+.5f, -90, 0));
                });
            } catch (WorldEditException | IOException e) {
                MobAnnihilation.getInstance().getLogger().info(e.getMessage());
            }
        }
    }

    // TODO: отгрузку арен
    public void unload() {
        if (editSession != null) {
            editSession.undo(editSession);
        }
    }
}
