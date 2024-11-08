package ru.peef.mobannihilation.holograms;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import ru.peef.mobannihilation.MobAnnihilation;

import java.util.ArrayList;
import java.util.List;

public class Hologram {
    public static List<Hologram> HOLOGRAMS = new ArrayList<>();

    public static Hologram get(String name) {
        for (Hologram hologram : HOLOGRAMS) {
            if (hologram.getName().equals(name)) return hologram;
        }

        return null;
    }

    private final ArmorStand armorStand;
    private final String name;
    public Hologram(String name, World world, double x, double y, double z, String text) {
        this.name = name;
        Location location = new Location(world, x,  y, z);
        armorStand = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);

        setText(text);
        if (!text.isEmpty()) armorStand.setCustomNameVisible(true);
        armorStand.setGravity(false);
        armorStand.setInvulnerable(true);
        armorStand.setMarker(true);
        armorStand.setVisible(false);

        MobAnnihilation.getInstance().getLogger().info(String.format("Spawned Hologram at %s %s %s (%s)", x, y, z, name));

        HOLOGRAMS.add(this);
    }

    public void setText(String text) {
        if (armorStand != null && !text.isEmpty()) {
            String holoText = PlaceholderAPI.setPlaceholders(null, text.replace('&', ChatColor.COLOR_CHAR).trim());
            armorStand.setCustomName(holoText);
            armorStand.setCustomNameVisible(true);
        }
    }
    public void remove() {
        if (armorStand != null) {
            armorStand.remove();
        }
    }

    public String getText() { return armorStand.getCustomName(); }
    public Location getLocation() { return armorStand != null ? armorStand.getLocation() : null; }
    public String getName() { return name; }
}
