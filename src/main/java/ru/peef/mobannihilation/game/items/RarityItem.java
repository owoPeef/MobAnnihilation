package ru.peef.mobannihilation.game.items;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import ru.peef.mobannihilation.Utils;
import ru.peef.mobannihilation.game.players.GamePlayer;

import java.util.Random;

public class RarityItem {
    private final String title;
    private final String description;
    private transient float chance;
    private transient ItemStack itemStack;
    public float baseValue;
    public Boost boost;
    public float boostPercent;
    public final transient boolean boostIsPercent;
    // 1 - обычный
    // 2 - необычный
    // 3 - редкий
    // 4 - очень редкий
    // 5 - легендарный
    public int rarity = -1;

    public enum Boost {
        DAMAGE,
        PROTECTION,
        ATTACK_SPEED,
        SPEED
    }

    public RarityItem(String title, String description, float baseValue, Boost boost, float boostPercent) {
        this.title = title.replace('&', ChatColor.COLOR_CHAR);
        this.description = description.replace('&', ChatColor.COLOR_CHAR);
        this.baseValue = baseValue;
        this.boost = boost;
        this.boostIsPercent = (boost == Boost.DAMAGE || boost == Boost.PROTECTION);
        this.boostPercent = boostPercent;
    }

    public RarityItem(String title, String description, float baseValue, Boost boost) {
        this.title = title.replace('&', ChatColor.COLOR_CHAR);
        this.description = description.replace('&', ChatColor.COLOR_CHAR);
        this.baseValue = baseValue;
        this.boost = boost;
        this.boostIsPercent = (boost == Boost.DAMAGE || boost == Boost.PROTECTION);
        float minBoostPercent = !this.boostIsPercent ? 0 : this.baseValue;
        float maxBoostPercent = !this.boostIsPercent ? 3 : this.baseValue * (this.boost == Boost.PROTECTION ? 1.37f : 1.1f);
        this.boostPercent = (float) Math.floor(getRandomPercent(minBoostPercent, maxBoostPercent));

        if (boost.equals(Boost.SPEED)) boostPercent++;

        this.chance = 100 * (1 - (boostPercent - minBoostPercent) / (maxBoostPercent - minBoostPercent));
    }

    public String getTitle() { return title.replace("{BOOST_NAME}", getBoostName(true, 2)); }

    public String getDescription() {
        return description
                .replace("{BOOST_PERCENT}", boostPercent + (this.boostIsPercent ? "%" : ""))
                .replace("{BOOST_NAME}", getBoostName(true, 1));
    }

    public float getChance() { return this.chance; }
    public String getBoostName(boolean takeCase, int caseNumber) {
        if (takeCase) {
            if (caseNumber == 1) {
                switch (boost) {
                    case DAMAGE: {
                        return "урону";
                    }
                    case PROTECTION: {
                        return "защите";
                    }
                    case SPEED: {
                        return "скорости";
                    }
                    case ATTACK_SPEED: {
                        return "скорости атаки";
                    }
                }
            } else if (caseNumber == 2) {
                switch (boost) {
                    case DAMAGE: {
                        return "Урона";
                    }
                    case PROTECTION: {
                        return "Защиты";
                    }
                    case SPEED: {
                        return "Скорости";
                    }
                    case ATTACK_SPEED: {
                        return "Скорости Атаки";
                    }
                }
            }
        } else {
            switch (boost) {
                case DAMAGE: {
                    return "урон";
                }
                case PROTECTION: {
                    return "защита";
                }
                case SPEED: {
                    return "скорость";
                }
                case ATTACK_SPEED: {
                    return "скорость атаки";
                }
            }
        }

        return "???";
    }

    public String getRarityString() {
        if (rarity == 5) {
            return ChatColor.RED + "ЛЕГЕНДАРНАЯ";
        } else if (rarity == 4) {
            return ChatColor.LIGHT_PURPLE + "ОЧЕНЬ РЕДКАЯ";
        } else if (rarity == 3) {
            return ChatColor.GREEN + "РЕДКАЯ";
        } else if (rarity == 2) {
            return ChatColor.DARK_GREEN + "НЕОБЫЧНАЯ";
        } else {
            return ChatColor.WHITE + "ОБЫЧНАЯ";
        }
    }

    public ItemStack getItemStack() { return this.itemStack; }
    public void setItemStack(ItemStack itemStack) { this.itemStack = itemStack; }

    public static float getRandomPercent(float MIN, float MAX) {
        return (float) Utils.roundTo(MIN + Math.random() * (MAX - MIN), 2);
    }

    public static RarityItem getRandom(GamePlayer gamePlayer) {
        Random rand = new Random();

        int includeBoostsCount = 4;
        int randInt = rand.nextInt(includeBoostsCount);
        Boost boost = (randInt == 3 ? Boost.SPEED : randInt == 2 ? Boost.PROTECTION : Boost.DAMAGE);

        return new RarityItem("Руна {BOOST_NAME}", "Когда находится в инвентаре, дает +{BOOST_PERCENT} к {BOOST_NAME}", gamePlayer.getLevel() / 1.8f, boost);
    }

    public static CraftRarityItem combineItems(RarityItem... items) {
        return new CraftRarityItem(items);
    }

    public static RarityItem create(String title, String description, float baseValue, String boostType, float boostPercent) {
        Boost boost;

        try {
            boost = Boost.valueOf(boostType);
        } catch (IllegalArgumentException ignored) {
            boost = null;
        }

        return new RarityItem(title, description, baseValue, boost, boostPercent);
    }
}
