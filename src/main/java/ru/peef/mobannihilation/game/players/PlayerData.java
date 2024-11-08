package ru.peef.mobannihilation.game.players;

import ru.peef.mobannihilation.game.items.RarityItem;

import java.util.List;

public class PlayerData {
    public double level;
    public List<RarityItem> rarity_items;

    public PlayerData(double level, List<RarityItem> rarity_items) {
        this.level = level;
        this.rarity_items = rarity_items;
    }

    public int getLevel() { return (int) Math.floor(level); }

    public static PlayerData create(GamePlayer gamePlayer) {
        return new PlayerData(gamePlayer.level, gamePlayer.getAllRarityItems());
    }
}
