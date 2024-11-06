package ru.peef.mobannihilation.game;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AnvilGUI implements Listener {
    public static void openAnvil(Player player) {
        Inventory anvil = Bukkit.createInventory(null, InventoryType.ANVIL, "Соединение предметов");
        player.openInventory(anvil);
    }

    // Обработка логики слияния предметов
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory anvilInventory = event.getInventory();
        ItemStack item1 = anvilInventory.getItem(0); // Первый слот
        ItemStack item2 = anvilInventory.getItem(1); // Второй слот

        if (item1 != null && item2 != null) {
            // Проверка, что это кастомные предметы и что их можно комбинировать
            if (isCustomItem(item1) && isCustomItem(item2)) {
                ItemStack result = combineCustomItems(item1, item2);
                event.setResult(result); // Установка результата
            }
        }
    }

    // Проверка, кастомный ли предмет
    private static boolean isCustomItem(ItemStack item) {
        // Проверить тип, метаданные или любую другую информацию, чтобы удостовериться, что это кастомный предмет
        return item.getType() == Material.DIAMOND_SWORD; // Пример проверки
    }

    // Объединение кастомных предметов
    private static ItemStack combineCustomItems(ItemStack item1, ItemStack item2) {
        // Пример: создание нового предмета, объединяя эффекты или характеристики
        ItemStack combinedItem = new ItemStack(Material.DIAMOND_SWORD); // Новый предмет
        combinedItem.getItemMeta().setDisplayName("Комбинированный предмет");
        return combinedItem;
    }

    // Обработка клика по слотам наковальни
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType() == InventoryType.ANVIL) {
            if (event.getSlot() == 2) { // Проверка выхода
                ItemStack result = event.getCurrentItem();
                if (result != null && result.getType() == Material.DIAMOND_SWORD) {
                    event.setCancelled(true); // Отменяем стандартное поведение (например, если нужно запретить крафт)
                }
            }
        }
    }
}
