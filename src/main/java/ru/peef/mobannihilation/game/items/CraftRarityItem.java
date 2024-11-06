package ru.peef.mobannihilation.game.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CraftRarityItem {
    int mergeCount = 0;
    public List<RarityItem> items;

    public CraftRarityItem(List<RarityItem> items) {
        this.items = items;
        mergeCount++;
    }

    public CraftRarityItem(RarityItem... items) {
        this.items = Arrays.asList(items);
        mergeCount++;
    }

    public List<RarityItem> getBoosts(RarityItem.Boost type) {
        return items.stream()
                .filter(item -> item.boost.equals(type))
                .collect(Collectors.toList());
    }

    public static CraftRarityItem combineItems(CraftRarityItem craftItem1, CraftRarityItem craftItem2) {
        List<RarityItem> combinedItems = new ArrayList<>();
        List<RarityItem> allItems = new ArrayList<>();

        allItems.addAll(craftItem1.items);
        allItems.addAll(craftItem2.items);

        for (RarityItem.Boost boostType : RarityItem.Boost.values()) {
            List<RarityItem> boostItems = allItems.stream()
                    .filter(item -> item.boost == boostType)
                    .collect(Collectors.toList());

            if (!boostItems.isEmpty()) {
                float totalBoostPercent = 0;
                for (RarityItem item : boostItems) {
                    totalBoostPercent += item.boostPercent;
                }

                RarityItem combinedItem = new RarityItem(
                        boostItems.get(0).getTitle(),
                        boostItems.get(0).getDescription(),
                        boostItems.get(0).baseValue,
                        boostType
                );
                combinedItem.boostPercent = totalBoostPercent;
                combinedItems.add(combinedItem);
            }
        }

        return new CraftRarityItem(combinedItems);
    }
}
