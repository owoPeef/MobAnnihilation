package ru.peef.mobannihilation.game.mobs;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import ru.peef.mobannihilation.game.GameManager;
import ru.peef.mobannihilation.game.players.GamePlayer;
import ru.peef.mobannihilation.game.players.PlayerManager;

import java.util.UUID;

public class GameMob {
    public Entity entity;
    public LivingEntity livingEntity;
    public UUID uniqueId;
    public GamePlayer spawnedFor;

    public GameMob(Entity entity, GamePlayer spawnedFor) {
        this.entity = entity;
        this.livingEntity = (LivingEntity) entity;

        uniqueId = entity.getUniqueId();

        this.spawnedFor = spawnedFor;

        if (livingEntity != null) {
            double mobHealth = livingEntity.getMaxHealth() * (this.spawnedFor.getLevel() / 2.1);
            livingEntity.setMaxHealth(Math.min(mobHealth, 2048.0));
            livingEntity.setHealth(Math.min(mobHealth, 2048.0));
        }

        GameManager.SPAWNED_MOBS.add(this);
    }

    public GameMob(Entity entity, Player spawnedFor) {
        this.entity = entity;
        this.livingEntity = (LivingEntity) entity;

        uniqueId = entity.getUniqueId();

        this.spawnedFor = PlayerManager.get(spawnedFor.getPlayer());

        if (livingEntity != null && this.spawnedFor != null) {
            double mobHealth = livingEntity.getMaxHealth() * (this.spawnedFor.getLevel() / 2.1);
            livingEntity.setMaxHealth(Math.min(mobHealth, 2048.0));
            livingEntity.setHealth(Math.min(mobHealth, 2048.0));
        }

        GameManager.SPAWNED_MOBS.add(this);
    }
}
