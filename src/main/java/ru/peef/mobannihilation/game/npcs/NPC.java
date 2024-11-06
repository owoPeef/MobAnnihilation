package ru.peef.mobannihilation.game.npcs;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import ru.peef.mobannihilation.holograms.Hologram;

import java.util.UUID;

public class NPC {
    private transient String name;
    private transient Hologram hologram;
    public transient UUID uniqueId;
    public double x, y, z;
    public float yaw;
    public transient World world;
    public String worldName;
    public String executeCommand;
    public String hologramText;
    private transient Entity entity;

    public NPC(double x, double y, double z, float yaw, String name, String hologramText, String worldName, String command) {
        this.name = name;
        this.hologramText = hologramText;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;

        world = new WorldCreator(worldName).createWorld();
        this.worldName = worldName;

        executeCommand = command;
    }

    public void spawn() {
        if (world == null) {
            world = new WorldCreator(worldName).createWorld();
        }
        entity = world.spawn(new Location(world, x, y, z, yaw, 0), Villager.class);
        entity.setGravity(false);
        ((Villager) entity).setAI(false);
        uniqueId = entity.getUniqueId();

        hologram = new Hologram(this.name, world, x, y, z, hologramText);
    }
    public void despawn() {
        entity.remove();
        if (name != null) save();

        Hologram.HOLOGRAMS.removeIf(holo -> holo.getName().equals(hologram.getName()));
        hologram.remove();
    }
    public void save() { NPCDataHandler.saveNPC(this); }
    public void update() { NPCDataHandler.updateNPC(this); }
    public void setName(String name) { this.name = name; }

    public Entity getEntity() { return entity; }
    public String getName() { return name; }
}
