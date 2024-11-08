package ru.peef.mobannihilation.game.players;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.peef.mobannihilation.MobAnnihilation;
import ru.peef.mobannihilation.ScoreboardUtils;
import ru.peef.mobannihilation.Utils;
import ru.peef.mobannihilation.game.Arena;
import ru.peef.mobannihilation.game.GameManager;
import ru.peef.mobannihilation.game.items.RarityItem;
import ru.peef.mobannihilation.game.mobs.GameMob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class GamePlayer {
    private final Player player;
    private final String name;
    public double level = 1.0;
    public List<GameMob> mobs = new ArrayList<>();
    private final HashMap<Integer, RarityItem> items = new HashMap<>();
    public boolean onArena = false;
    public String lastProg = "";
    public boolean editMode = false;

    // TODO: Брать из конфига
    public int maxItemsCount = 3;

    // TODO: Ребитхи

    public GamePlayer(Player player, Double level) {
        this.player = player;
        this.name = player.getName();
        this.level = level;

        updateProgress();
    }

    public GamePlayer(String name, Double level) {
        this.name = name;
        this.player = Bukkit.getPlayer(this.name);
        this.level = level;
    }

    public GamePlayer(String name, Double level, List<RarityItem> items) {
        this.name = name;
        this.player = Bukkit.getPlayer(this.name);
        this.level = level;

        items.forEach(item -> addItem(item, false));
    }

    public String getName() { return player == null ? name : player.getName(); }
    public Player getPlayer() { return player; }
    public int getLevel() { return (int) Math.floor(level); }
    public int getProgress() { return (int) Math.floor((level - Math.floor(level)) * 100); }

    public String getStatsMessage() {
        return ChatColor.GREEN + "===============\n"
                + ChatColor.AQUA + "Игрок " + ChatColor.GOLD + getName() + "\n"
                + ChatColor.AQUA + "Уровень: " + ChatColor.YELLOW + getLevel() + "\n"
                + ChatColor.AQUA + "Прогресс: " + ChatColor.YELLOW + getProgress() + "%\n"
                + ChatColor.AQUA + "Рун Урона: " + ChatColor.YELLOW + getRarityItems(RarityItem.Boost.DAMAGE).size() + " (" + Utils.roundTo(getRarityPercent(RarityItem.Boost.DAMAGE), 1) + "%)" + "\n"
                + ChatColor.AQUA + "Рун Защиты: " + ChatColor.YELLOW + getRarityItems(RarityItem.Boost.PROTECTION).size() + " (" + Utils.roundTo(getRarityPercent(RarityItem.Boost.PROTECTION), 1) + "%)" + "\n"
                + ChatColor.AQUA + "Рун Скорости: " + ChatColor.YELLOW + getRarityItems(RarityItem.Boost.SPEED).size() + " (" + Utils.roundTo(getRarityPercent(RarityItem.Boost.SPEED), 1) + ")" + "\n"
//                + ChatColor.AQUA + "Рун Скорости Атаки: " + ChatColor.YELLOW + getRarityItems(RarityItem.Boost.ATTACK_SPEED).size() + " (" + Utils.roundTo(getRarityPercent(RarityItem.Boost.ATTACK_SPEED), 1) + ")" + "\n"
                + ChatColor.GREEN + "===============\n";
    }

    public void save() { PlayerDataHandler.savePlayer(this); }
    public void addLevel(int level) { addProgress(level * 100); }
    public void addProgress(double prog) {
        int lvl = getLevel();
        level += prog / 100;

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(String.format("%sПрогресс: %s%s %s(+%s)", ChatColor.AQUA + (ChatColor.BOLD).toString(), ChatColor.GOLD+ (ChatColor.BOLD).toString(), getProgress() + "%", ChatColor.GREEN+ (ChatColor.BOLD).toString(), Utils.roundTo(prog, 1) + "%")));
        if (getLevel() > lvl) {
            player.sendTitle(ChatColor.GOLD + (ChatColor.BOLD + "НОВЫЙ УРОВЕНЬ!"), ChatColor.AQUA + "Вы достигли " + ChatColor.GOLD + getLevel() + " уровня", 5, 20, 5);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            setSword();
        }

        updateProgress();

        lastProg = ChatColor.GREEN + "(+" + (int)prog + "%)";
    }
    public void reduceProgress(double prog) { level -= prog / 100; updateProgress(); lastProg = ChatColor.RED + "(-" + (int)prog + "%)"; }

    public void updateProgress() {
        player.setLevel(getLevel());

        float experience = getProgress() / 100.0f;
        player.setExp(experience);

        setSword();
        save();
    }

    public void setSword() {
        int currentLevel = getLevel();
        ItemStack sword = new ItemStack((currentLevel < 5 ? Material.WOOD_SWORD : currentLevel < 25 ? Material.STONE_SWORD : currentLevel < 60 ? Material.IRON_SWORD : Material.DIAMOND_SWORD), 1);
        ItemMeta swordMeta = sword.getItemMeta();
        if (swordMeta != null) {
            swordMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            swordMeta.setDisplayName(ChatColor.GOLD + "Перчатка для Битья");
            swordMeta.setUnbreakable(true);

            if (sword.getType().equals(Material.DIAMOND_SWORD)) {
                swordMeta.addEnchant(Enchantment.DAMAGE_ALL, Math.min((currentLevel - 55) / 5, 10), true);
            }

            sword.setItemMeta(swordMeta);
        }
        player.getInventory().setItem(0, sword);
    }

    public void setPotions() {
        player.removePotionEffect(PotionEffectType.SPEED);
        if (!getRarityItems(RarityItem.Boost.SPEED).isEmpty()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, ((int) getRarityPercent(RarityItem.Boost.SPEED))-1, false));
        }
    }

    public void joinServer() {
        updateProgress();
        PlayerManager.PLAYERS.add(this);
        getPlayer().getInventory().setHeldItemSlot(0);

        setPotions();
        ScoreboardUtils.updateScoreboard(this);
    }

    Arena arena = null;

    public void joinArena() {
        if (!onArena) {
            onArena = true;
            player.sendMessage(ChatColor.GREEN + "Вы были телепортированы на арену!");
            player.sendMessage(ChatColor.YELLOW + (ChatColor.BOLD + "ПОМНИТЕ!") + ChatColor.AQUA + " Для выхода из арены: " + ChatColor.GOLD + "/game leave");

            for (Player p : GameManager.ARENA_WORLD.getPlayers()) {
                if (GameManager.hideOnArena) {
                    player.hidePlayer(MobAnnihilation.getInstance(), p);
                    p.hidePlayer(MobAnnihilation.getInstance(), player);
                }
            }

            GameManager.PLAYERS_ON_ARENA.add(this);

            arena = new Arena(GameManager.ARENA_WORLD,
                    "mainArena",
                    5, 18, 40,
                    21, 7, 40
            );
            arena.add(this);
            arena.load(0, 25, 50);

            spawnMob(EntityType.ZOMBIE);
        } else {
            player.sendMessage(ChatColor.RED + "Ты уже на арене!");
        }
    }

    public void leaveArena(boolean sendMessages) {
        if (onArena) {
            player.teleport(GameManager.BASIC_SPAWN);
            if (sendMessages) player.sendMessage(ChatColor.AQUA + "Вы вышли с арены!");
            GameManager.PLAYERS_ON_ARENA.removeIf(checkPlayer -> checkPlayer.getName().equals(player.getName()));
            onArena = false;

            arena.unload();
            arena = null;
        } else if (sendMessages) {
            player.sendMessage(ChatColor.RED + "Ты не находишься на арене!");
        }
    }

    public void addItem(RarityItem item, boolean chatAnnounce) {
        if (items.size() < maxItemsCount) {
            for (int i = 21; i < 26; i++) {
                ItemStack slot = player.getInventory().getStorageContents()[i];
                if (slot == null) {
                    ItemStack itemStack = new ItemStack(Material.EMERALD, 1);
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    if (itemMeta != null) {
                        List<String> lore = new ArrayList<>();
                        itemMeta.setDisplayName(ChatColor.RESET + (ChatColor.GOLD + item.getTitle()));

                        lore.add(ChatColor.GREEN + "Редкость: " + item.getRarityString() + ChatColor.GRAY + " (" + item.boostPercent + (item.boostIsPercent ? "%" : "") + ")");
                        if (item.getChance() != 0) lore.add(ChatColor.GREEN + "Шанс выпадения: " + ChatColor.GOLD + Utils.roundTo(item.getChance(), 2) + "%");
                        lore.add(ChatColor.GRAY + item.getDescription());

                        itemMeta.setLore(lore);
                    }

                    itemStack.setItemMeta(itemMeta);
                    player.getInventory().setItem(i, itemStack);
                    item.setItemStack(itemStack);
                    items.put(i, item);

                    if (item.rarity == 5) {
                        // TODO show text on hover
                        Bukkit.broadcastMessage(ChatColor.GOLD + getName() + ChatColor.AQUA + " выбил " + ChatColor.RED + "ЛЕГЕНДАРНУЮ" + ChatColor.AQUA + " руну!");
                    }

                    break;
                }
            }

            if (item.boost.equals(RarityItem.Boost.SPEED)) {
                setPotions();
            }

            if (chatAnnounce) {
                TextComponent message = new TextComponent(ChatColor.AQUA + "Получен предмет: ");
                TextComponent hoverMessage = new TextComponent(ChatColor.GOLD + "[" + item.getTitle() + "]");

                hoverMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GOLD + item.getTitle() + "\n" +
                        ChatColor.GREEN + "Редкость: " + item.getRarityString() + ChatColor.GRAY + " (" + item.boostPercent + (item.boostIsPercent ? "%" : "") + ")" + "\n" +
                        ChatColor.AQUA + item.getDescription()
                ).create()));
                message.addExtra(hoverMessage);
                player.spigot().sendMessage(message);
            }
        } else {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Инвентарь заполнен!"));
        }
    }

    public void assignItems() {
        player.getInventory().clear();

        for (int i = 21; i < 26; i++) {
            if (items.containsKey(i)) {
                RarityItem item = items.get(i);
                player.getInventory().remove(i);

                ItemStack itemStack = new ItemStack(Material.EMERALD, 1);
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta != null) {
                    List<String> lore = new ArrayList<>();
                    itemMeta.setDisplayName(ChatColor.RESET + (ChatColor.GOLD + item.getTitle()));

                    lore.add(ChatColor.GREEN + "Редкость: " + item.getRarityString() + ChatColor.GRAY + " (" + item.boostPercent + (item.boostIsPercent ? "%" : "") + ")");
                    if (item.getChance() != 0) lore.add(ChatColor.GREEN + "Шанс выпадения: " + ChatColor.GOLD + Utils.roundTo(item.getChance(), 2) + "%");
                    lore.add(ChatColor.GRAY + item.getDescription());

                    itemMeta.setLore(lore);
                }

                itemStack.setItemMeta(itemMeta);
                player.getInventory().setItem(i, itemStack);
                item.setItemStack(itemStack);
            }
        }

        setSword();
    }

    public void removeItem(Integer slot) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.AQUA + "Вы выбросили " + ChatColor.GOLD + "[" + items.get(slot).getTitle() + "]"));
        items.remove(slot);
        setPotions();
    }

    public HashMap<Integer, RarityItem> getRarityItems() { return items; }
    public List<RarityItem> getAllRarityItems() { return new ArrayList<>(items.values()); }
    public List<RarityItem> getRarityItems(RarityItem.Boost boostType) {
        List<RarityItem> rarityItems = new ArrayList<>();
        items.forEach((slot, item) -> { if (item.boost.equals(boostType)) rarityItems.add(item); });
        return rarityItems;
    }

    public float getRarityPercent(RarityItem.Boost type) {
        float value = 0;
        for (RarityItem protectionItem : getRarityItems(type)) {
            value += protectionItem.boostPercent;
        }
        return value;
    }
    public void spawnMobs() {
        for (int i = 0; i < 3; i++) {
            spawnMob(EntityType.ZOMBIE);
        }
    }

    public void spawnMob(EntityType type) {
        Entity entity = GameManager.ARENA_WORLD.spawnEntity(arena.getMobSpawn(), type);
        GameMob mob = GameMob.createAndAppend(entity, this);
        MobAnnihilation.getInstance().getLogger().info(String.format("Spawned mob for %s at %s %s %s", mob.spawnedFor.getName(), entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ()));
    }

    public GameMob getMob(UUID uniqueId) {
        for (GameMob mob : mobs) {
            if (mob.uniqueId.equals(uniqueId)) return mob;
        }
        return null;
    }

    public boolean hasAliveMobs() {
        for (GameMob mob : mobs) {
//            player.sendMessage(ChatColor.GOLD + (mob.uniqueId + (ChatColor.AQUA + " is dead: ") + mob.livingEntity.isDead()));
            if (mob.livingEntity != null && !mob.livingEntity.isDead()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasMob(UUID uniqueId) {
        for (GameMob mob : mobs) {
            if (mob.livingEntity != null && !mob.livingEntity.isDead() && mob.uniqueId.equals(uniqueId)) return true;
        }
        return false;
    }

    public static GamePlayer fromFile(String name) {
        PlayerData playerData = PlayerDataHandler.getPlayerData(name);

        if (playerData != null) return new GamePlayer(name, Math.max(1.0, playerData.level), playerData.rarity_items);
        else return new GamePlayer(name, 1.0);
    }
    public static GamePlayer fromFile(Player player) { return fromFile(player.getName()); }
}
