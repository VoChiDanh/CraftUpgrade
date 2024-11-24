package net.danh.craftUpgrade.gui.upgrade_gui;


import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerIngredientsPageData {
    private static final Map<Player, Integer> playerPages = new HashMap<>();
    private static final Map<Player, String> playerItems = new HashMap<>();

    public static int getCurrentPage(Player player) {
        return playerPages.getOrDefault(player, 1); // Default to page 1 if not set
    }

    public static void setCurrentPage(Player player, int page) {
        playerPages.put(player, page);
    }

    public static String getCurrentItemId(Player player) {
        return playerItems.get(player); // Returns null if not set
    }

    public static void setCurrentItemId(Player player, String idItems) {
        playerItems.put(player, idItems);
    }
}
