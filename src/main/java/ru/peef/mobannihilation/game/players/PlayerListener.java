package ru.peef.mobannihilation.game.players;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import ru.peef.mobannihilation.MobAnnihilation;
import ru.peef.mobannihilation.Utils;
import ru.peef.mobannihilation.game.GameManager;
import ru.peef.mobannihilation.game.items.RarityItem;
import ru.peef.mobannihilation.game.mobs.GameMob;
import ru.peef.mobannihilation.game.npcs.NPC;
import ru.peef.mobannihilation.game.npcs.NPCManager;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class PlayerListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage("");

        Player player = event.getPlayer();

        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(GameManager.BASIC_SPAWN);
        player.setHealth(player.getMaxHealth());
        player.getInventory().clear();

        player.setResourcePack("https://download.mc-packs.net/pack/3d3949e03b080798e87fee534f17e4a39869bbf5.zip");

        // Когда игрок только зашел, у него появляются все доступные команды
        if (!PlayerDataHandler.hasPlayer(player)) {
            player.sendMessage(
                    ChatColor.GREEN + "Привет! Я для тебя подготовил небольшой гайд по командам... Вкратце:\n" +
                    ChatColor.GOLD + "/game info" + ChatColor.AQUA + " - вывести это сообщение\n" +
                    ChatColor.GOLD + "/game stats" + ChatColor.AQUA + " - получить свою статистику\n" +
                    ((player.isOp() || player.hasPermission("game.stats.other")) ? ChatColor.GOLD + "/game stats <ник>" + ChatColor.AQUA + " - получить статистику игрока\n" : "") +
                    ChatColor.GOLD + "/game join" + ChatColor.AQUA + " - войти на арену\n" +
                    ChatColor.GOLD + "/game leave" + ChatColor.AQUA + " - выйти с арены\n" +
                    ((player.isOp() || player.hasPermission("game.add")) ? ChatColor.GOLD + "/game add_level <число>" + ChatColor.AQUA + " - добавить <число> уровней\n" + ChatColor.GOLD + "/game add_progress <число>" + ChatColor.AQUA + " - добавить <число>% прогресса (от 1 до 100)" : "")
            );
        }

        GamePlayer gamePlayer = GamePlayer.fromFile(player);

        gamePlayer.joinServer();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage("");

        Player player = event.getPlayer();
        GamePlayer gamePlayer = PlayerManager.get(player);

        if (gamePlayer != null) {
            gamePlayer.save();

            for (GameMob mob : gamePlayer.mobs) {
                if (mob != null && mob.livingEntity != null && !mob.livingEntity.isDead() && mob.spawnedFor.getName().equals(gamePlayer.getName())) {
                    mob.livingEntity.remove();
                }
            }

            PlayerManager.PLAYERS.removeIf(checkPlayer -> checkPlayer.getPlayer().getName().equals(gamePlayer.getPlayer().getName()));
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();

        if (event.getRightClicked().getType().equals(EntityType.VILLAGER)) {
            NPC npc = NPCManager.get(event.getRightClicked().getUniqueId());

            if (npc != null) {
                player.performCommand(npc.executeCommand);
            }
        } else if (event.getRightClicked().getType().equals(EntityType.PLAYER)) {
            Player interactPlayer = (Player) event.getRightClicked();

            GamePlayer interactGamePlayer = PlayerManager.get(interactPlayer);

            if (interactGamePlayer != null) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.YELLOW + "У " + interactGamePlayer.getName() + " имеет " + ChatColor.GOLD + interactGamePlayer.getLevel() + " уровень"));
            }
        } else {
            LivingEntity entity = (LivingEntity) event.getRightClicked();
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.AQUA + (Utils.roundTo(entity.getHealth(), 1) + "❤/") + (Utils.roundTo(entity.getMaxHealth(), 1) + "❤")));
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        GamePlayer gamePlayer = PlayerManager.get(player);
        ItemStack item = event.getItemDrop().getItemStack();

        if (gamePlayer != null) {
            for (Map.Entry<Integer, RarityItem> entry : gamePlayer.getRarityItems().entrySet()) {
                if (item.isSimilar(entry.getValue().getItemStack())) {
                    gamePlayer.removeItem(entry.getKey());
                    event.getItemDrop().remove();

                    event.setCancelled(false);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntityType().equals(EntityType.PLAYER) && event.getDamager().getType().equals(EntityType.PLAYER)) {
            event.setCancelled(true);
        } else {
            if (event.getEntityType().equals(EntityType.VILLAGER)) event.setCancelled(true);
            if (event.getDamager().getType().equals(EntityType.PLAYER)) {
                if (!event.getEntityType().equals(EntityType.VILLAGER)) {
                    Player damager = (Player) event.getDamager();
                    GamePlayer gDamager = PlayerManager.get(damager);

                    if (gDamager != null) {
                        if (!gDamager.getRarityItems().isEmpty()) {
                            double itemBaseDamage = 0;
                            double damage = event.getFinalDamage();
                            double damagePercent = damage / 100f;

                            List<RarityItem> damageItems = gDamager.getRarityItems(RarityItem.Boost.DAMAGE);

                            double result = 100.0;
                            for (RarityItem rarityItem : damageItems) {
                                result *= (1 + rarityItem.boostPercent / 100);
                                itemBaseDamage += rarityItem.baseValue;
                            }
                            result -= 100;

                            double total = Math.abs(itemBaseDamage + damage) + (damagePercent * result);
//                            damager.sendMessage(String.format("%sБазовый урон: %s (+%s)\n%sИтоговый урон: %s", ChatColor.AQUA, Utils.roundTo(damage, 1), Utils.roundTo(result, 1) + "%", ChatColor.YELLOW, Utils.roundTo(total, 1)));

                            event.setDamage(total);
                        }

                        LivingEntity entity = (LivingEntity) event.getEntity();
                        if (entity.getHealth() - event.getFinalDamage() <= 0) {
                            float baseProgress = 22;
                            gDamager.addProgress(baseProgress / gDamager.getLevel());
                            damager.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20, 5), true);

                            RarityItem dropItem = RarityItem.getRandom(gDamager);
                            Random rand = new Random();
                            if (dropItem.getChance() <= RarityItem.getRandomPercent(0f, 100f) && rand.nextInt(3) == 1) {
                                gDamager.addItem(dropItem, true);
                            }

                            damager.playSound(damager.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, .5f, 1f);

                            GameMob killedMob = gDamager.getMob(entity.getUniqueId());
//                            Bukkit.getLogger().info("Killed mob (" + killedMob.uniqueId + ") by " + damager.getName() + " is null: " + (killedMob.equals(null)));
                            if (killedMob != null) {
                                gDamager.mobs.removeIf(mob -> mob.uniqueId.equals(killedMob.uniqueId));
                                if (!gDamager.hasAliveMobs()) {
                                    Bukkit.getScheduler().runTaskLater(MobAnnihilation.getInstance(), gDamager::spawnMobs, 10L);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntityType().equals(EntityType.PLAYER)) {
            EntityDamageEvent.DamageCause cause = event.getCause();
            if (cause.equals(EntityDamageEvent.DamageCause.FALL) || cause.equals(EntityDamageEvent.DamageCause.FIRE)) {
                event.setCancelled(true);
            } else {
                GamePlayer gamePlayer = PlayerManager.get((Player) event.getEntity());

                if (gamePlayer != null) {
                    double scalingFactor = 1.03;
                    double linearFactor = 0.1;

                    int playerLevel = gamePlayer.getLevel();
                    double baseDamage = event.getFinalDamage();
                    double damage = baseDamage * Math.pow(scalingFactor, playerLevel) + baseDamage * linearFactor * playerLevel;

                    if (!gamePlayer.getRarityItems(RarityItem.Boost.PROTECTION).isEmpty()) {
                        double damagePercent = damage / 100f;

                        List<RarityItem> protectionItems = gamePlayer.getRarityItems(RarityItem.Boost.PROTECTION);

                        double result = 100.0;
                        for (RarityItem rarityItem : protectionItems) result *= (1 + rarityItem.boostPercent / 100);
                        result -= 100;
                        result /= (gamePlayer.getLevel() / 3.3f);

                        damage -= (damagePercent * result);
                    }

                    event.setDamage(damage);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = PlayerManager.get(player);

        if (gamePlayer != null) {
            event.setFormat(ChatColor.AQUA + "[" + gamePlayer.getLevel() + "✫] " + ChatColor.GOLD + "%s " + ChatColor.GREEN + ">> %s");
        } else {
            event.setFormat(ChatColor.GOLD + "%s " + ChatColor.GREEN + ">> %s");
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setDeathMessage("");
        Player player = event.getEntity();
        GamePlayer gamePlayer = PlayerManager.get(player);

        if (gamePlayer != null) {
            gamePlayer.mobs.forEach(mob -> {
                if (mob != null && mob.livingEntity != null) {
                    mob.livingEntity.remove();
                }
            });

            BukkitScheduler scheduler = Bukkit.getScheduler();

            scheduler.runTaskLater(MobAnnihilation.getInstance(), () -> {
                player.spigot().respawn();
                player.setHealth(player.getMaxHealth());
                player.teleport(GameManager.BASIC_SPAWN);
                player.setGameMode(GameMode.ADVENTURE);

                int progress = (int) Math.round(9 + Math.random() * (27 - 9));
                gamePlayer.reduceProgress(progress);
                gamePlayer.leaveArena(false);

                player.sendMessage(String.format(ChatColor.RED + "Вы умерли! Из-за этого вы потеряли: %s%s", ChatColor.GOLD, progress + " опыта"));
            }, 1L);
        }
    }

    @EventHandler
    public void onEntityCombust(EntityCombustEvent event) {
        Entity entity = event.getEntity();

        if (entity.getType() == EntityType.ZOMBIE ||
                entity.getType() == EntityType.SKELETON ||
                entity.getType() == EntityType.HUSK ||
                entity.getType() == EntityType.STRAY) {

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        Entity entity = event.getEntity();

        GameManager.PLAYERS_ON_ARENA.forEach(gamePlayer -> {
            if (gamePlayer.hasMob(entity.getUniqueId())) {
                event.setTarget(gamePlayer.getPlayer());
            }
        });
    }

    @EventHandler public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = PlayerManager.get(player);

        if (gamePlayer != null) {
            event.setCancelled(!gamePlayer.editMode);
        } else {
            event.setCancelled(true);
        }
    }
    @EventHandler public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = PlayerManager.get(player);

        if (gamePlayer != null) {
            event.setCancelled(!gamePlayer.editMode);
        } else {
            event.setCancelled(true);
        }
    }
    @EventHandler public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        Player player = (Player) event.getInventory().getViewers().get(0);
        GamePlayer gamePlayer = PlayerManager.get(player);

        if (gamePlayer != null) {
            event.setCancelled(!gamePlayer.editMode);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getType().equals(InventoryType.ANVIL)) {
            GamePlayer gamePlayer = PlayerManager.get((Player) event.getPlayer());
            if (gamePlayer != null) gamePlayer.assignItems();
        }
    }

    @EventHandler public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        GamePlayer gamePlayer = PlayerManager.get(player);

        if (gamePlayer != null && gamePlayer.editMode) {
            event.setCancelled(false);
        } else {
            if (!event.getClickedInventory().getType().equals(InventoryType.ANVIL) && !event.getAction().equals(InventoryAction.DROP_ALL_SLOT) && !event.getAction().equals(InventoryAction.DROP_ONE_SLOT))
                event.setCancelled(true);
        }
    }
    @EventHandler public void onFoodChange(FoodLevelChangeEvent event) { event.setCancelled(true); }
    @EventHandler public void onPlayerInteractEntity(PlayerInteractEntityEvent event) { if (event.getRightClicked().getType().equals(EntityType.VILLAGER)) event.setCancelled(true); }
}
