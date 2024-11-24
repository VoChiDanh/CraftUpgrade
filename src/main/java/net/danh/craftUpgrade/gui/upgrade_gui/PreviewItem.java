package net.danh.craftUpgrade.gui.upgrade_gui;

import net.Indyuce.mmoitems.MMOItems;
import net.danh.craftUpgrade.resources.Chat;
import net.danh.craftUpgrade.resources.Files;
import net.danh.craftUpgrade.resources.Number;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PreviewItem {

    public static void getPreviewItems(Player p, int page) {
        PlayerPageData.setCurrentPage(p, page);

        int inventorySize = Files.getItemUpgrade().getInt("preview_item.preview_items.size") * 9;
        Inventory inv = Bukkit.createInventory(p, inventorySize, Chat.colorize(Files.getItemUpgrade().getString("preview_item.preview_items.title")));
        FileConfiguration config = Files.getItemUpgrade();
        ConfigurationSection section = config.getConfigurationSection("item_upgrade");

        if (section != null) {
            List<String> keys = new ArrayList<>(section.getKeys(false));
            int totalItems = keys.size();

            int totalPages = (int) Math.ceil((double) totalItems / inventorySize);

            int startIndex = (page - 1) * inventorySize;
            int endIndex = Math.min(startIndex + inventorySize, totalItems);

            for (int i = startIndex; i < endIndex; i++) {
                String key = keys.get(i);
                String type = key.split(";")[0];
                String id = key.split(";")[1] + "_1";
                ItemStack item = MMOItems.plugin.getItem(type, id);
                inv.setItem(i - startIndex, item);
            }

            if (totalPages > 1) {
                if (page > 1) {
                    ItemStack backArrow = ItemUpgrade.getItem("page_item.previous_item");
                    inv.setItem(inventorySize - 9, backArrow);
                }

                if (page < totalPages) {
                    ItemStack forwardArrow = ItemUpgrade.getItem("page_item.next_item");
                    inv.setItem(inventorySize - 1, forwardArrow);
                }
            }

            p.openInventory(inv);
        }
    }

    public static void getIngredientsItems(Player p, String idItems, int page) {
        int inventorySize = Files.getItemUpgrade().getInt("preview_item.preview_ingredients.size") * 9;
        Inventory inv = Bukkit.createInventory(p, inventorySize, Chat.colorize(Files.getItemUpgrade().getString("preview_item.preview_ingredients.title")));
        FileConfiguration config = Files.getItemUpgrade();
        ConfigurationSection section = config.getConfigurationSection("item_upgrade." + idItems + ".item_requirements");

        if (section != null) {
            List<String> keys = new ArrayList<>(section.getKeys(false));
            int totalItems = keys.size();

            if (totalItems > inventorySize) {
                int totalPages = (int) Math.ceil((double) totalItems / inventorySize);

                int startIndex = (page - 1) * inventorySize;
                int endIndex = Math.min(startIndex + inventorySize, totalItems);

                for (int i = startIndex; i < endIndex; i++) {
                    String key = keys.get(i);
                    int level = Number.getInteger(key.replace("ingredients_", ""));
                    int slot = i - startIndex; // Slot in the current page
                    String item_id = idItems + "_" + level;
                    String type = item_id.split(";")[0];
                    String id = item_id.split(";")[1];
                    ItemStack item = MMOItems.plugin.getItem(type, id);
                    inv.setItem(slot, item);
                }

                if (totalPages > 1) {
                    if (page > 1) {
                        ItemStack backArrow = ItemUpgrade.getItem("page_item.previous_item");
                        inv.setItem(inventorySize - 9, backArrow);
                    }

                    if (page < totalPages) {
                        ItemStack forwardArrow = ItemUpgrade.getItem("page_item.next_item");
                        inv.setItem(inventorySize - 1, forwardArrow);
                    }
                }
            } else {
                for (int i = 0; i < totalItems; i++) {
                    String key = keys.get(i);
                    int level = Number.getInteger(key.replace("ingredients_", ""));
                    String item_id = idItems + "_" + level;
                    String type = item_id.split(";")[0];
                    String id = item_id.split(";")[1];
                    ItemStack item = MMOItems.plugin.getItem(type, id);
                    inv.setItem(i, item);
                }
            }

            p.openInventory(inv);
        }
    }
}
