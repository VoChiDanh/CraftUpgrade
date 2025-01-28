package net.danh.craftUpgrade.gui;

import io.lumine.mythic.lib.api.item.NBTItem;
import me.clip.placeholderapi.PlaceholderAPI;
import net.Indyuce.mmoitems.MMOItems;
import net.danh.craftUpgrade.gui.split_gui.ItemSplit;
import net.danh.craftUpgrade.gui.upgrade_gui.ItemUpgrade;
import net.danh.craftUpgrade.gui.upgrade_gui.PlayerIngredientsPageData;
import net.danh.craftUpgrade.gui.upgrade_gui.PlayerPageData;
import net.danh.craftUpgrade.gui.upgrade_gui.PreviewItem;
import net.danh.craftUpgrade.resources.Chat;
import net.danh.craftUpgrade.resources.Files;
import net.danh.craftUpgrade.resources.Number;
import net.danh.craftUpgrade.utils.Calculator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InvClick implements Listener {

    public static void getIngredientsItems(Player p, String idItems, int page) {
        PlayerIngredientsPageData.setCurrentPage(p, page);
        PlayerIngredientsPageData.setCurrentItemId(p, idItems);
        PreviewItem.getIngredientsItems(p, idItems, page);
    }

    public static @NotNull String removeSuffix(@NotNull String itemId) {
        int lastIndex = itemId.lastIndexOf('_');
        if (lastIndex != -1) {
            return itemId.substring(0, lastIndex);
        }
        return itemId;
    }

    private int getCurrentPage(Player player) {
        return PlayerPageData.getCurrentPage(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(@NotNull InventoryClickEvent e) {
        if (e.getView().getTitle().equalsIgnoreCase(Chat.normalColorize(Files.getItemUpgrade().getString("preview_item.preview_items.title")))) {
            e.setCancelled(true);

            if (e.getWhoClicked() instanceof Player p) {
                ItemStack clickedItem = e.getCurrentItem();

                if (clickedItem != null) {
                    NBTItem nbtItem = NBTItem.get(clickedItem);
                    if (!nbtItem.hasType()) {
                        if (clickedItem.isSimilar(ItemUpgrade.getItem("page_item.previous_item"))) {
                            int currentPage = getCurrentPage(p);
                            if (currentPage > 1) {
                                PreviewItem.getPreviewItems(p, currentPage - 1);
                            }
                        } else if (clickedItem.isSimilar(ItemUpgrade.getItem("page_item.next_item"))) {
                            int currentPage = getCurrentPage(p);
                            PreviewItem.getPreviewItems(p, currentPage + 1);
                        }
                    } else {
                        String type = nbtItem.getType();
                        String id = removeSuffix(nbtItem.getString("MMOITEMS_ITEM_ID"));
                        getIngredientsItems(p, type + ";" + id, 1);
                    }
                }
            }
        } else if (e.getView().getTitle().equalsIgnoreCase(Chat.normalColorize(Files.getItemUpgrade().getString("preview_item.preview_ingredients.title")))) {
            e.setCancelled(true);
            if (e.getWhoClicked() instanceof Player p) {
                ItemStack clickedItem = e.getCurrentItem();
                String idItems = PlayerIngredientsPageData.getCurrentItemId(p);
                if (clickedItem != null) {
                    NBTItem nbtItem = NBTItem.get(clickedItem);
                    if (!nbtItem.hasType()) {
                        if (clickedItem.isSimilar(ItemUpgrade.getItem("page_item.previous_item"))) {
                            int currentPage = PlayerIngredientsPageData.getCurrentPage(p);
                            if (currentPage > 1) {
                                getIngredientsItems(p, idItems, currentPage - 1);
                            }
                        } else if (clickedItem.isSimilar(ItemUpgrade.getItem("page_item.next_item"))) {
                            int currentPage = PlayerIngredientsPageData.getCurrentPage(p);
                            getIngredientsItems(p, idItems, currentPage + 1);
                        }
                    } else {
                        String type = nbtItem.getType();
                        String id = nbtItem.getString("MMOITEMS_ITEM_ID");
                        ItemUpgrade.getInventory(p, MMOItems.plugin.getItem(type, id));
                    }
                }
            }
        } else if (e.getView().getTitle().equalsIgnoreCase(Chat.normalColorize(Files.getItemSplit().getString("gui_split.title")))) {
            e.setCancelled(true);
            if (e.getWhoClicked() instanceof Player p) {
                FileConfiguration config = Files.getItemSplit();
                String gui_path = "gui_split.items";
                for (String item_id : Objects.requireNonNull(config.getConfigurationSection(gui_path)).getKeys(false)) {
                    String item_path = gui_path + "." + item_id;
                    List<Integer> slots = new ArrayList<>();
                    if (config.contains(item_path + ".slots"))
                        slots.addAll(config.getIntegerList(item_path + ".slots"));
                    if (config.contains(item_path + ".slot"))
                        slots.add(config.getInt(item_path + ".slot"));
                    if (slots.contains(e.getSlot())) {
                        if (config.contains(item_path + ".action")) {
                            String action = config.getString(item_path + ".action");
                            if (action != null) {
                                if (action.equalsIgnoreCase("confirm_split")) {
                                    NBTItem nbtItem = NBTItem.get(p.getInventory().getItemInMainHand());
                                    if (nbtItem.hasType()) {
                                        String type = nbtItem.getType();
                                        String item_idz = nbtItem.getString("MMOITEMS_ITEM_ID");
                                        List<String> splitItems = PlaceholderAPI.setPlaceholders(p,
                                                config.getStringList("item_split." + type + ";" + item_idz));
                                        if (!splitItems.isEmpty()) {
                                            p.closeInventory();
                                            p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                                            for (ItemStack itemStack : ItemSplit.getSplitItems(type, item_idz)) {
                                                p.getInventory().addItem(itemStack);
                                            }
                                        }
                                        Chat.sendMessage(p,
                                                Files.getMessage().getString("user.split_item.split_success"));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (e.getView().getTitle().equalsIgnoreCase(Chat.normalColorize(Files.getItemUpgrade().getString("gui_upgrade.title")))) {
            e.setCancelled(true);
            if (e.getWhoClicked() instanceof Player p) {
                FileConfiguration config = Files.getItemUpgrade();
                String gui_path = "gui_upgrade.items";
                ItemStack upgradeItem = e.getInventory().getItem(config.getInt(gui_path + ".default_upgrade_item"));
                for (String item_id : Objects.requireNonNull(config.getConfigurationSection(gui_path)).getKeys(false)) {
                    String item_path = gui_path + "." + item_id;
                    List<Integer> slots = new ArrayList<>();
                    if (config.contains(item_path + ".slots"))
                        slots.addAll(config.getIntegerList(item_path + ".slots"));
                    if (config.contains(item_path + ".slot"))
                        slots.add(config.getInt(item_path + ".slot"));
                    if (slots.contains(e.getSlot())) {
                        if (config.contains(item_path + ".action")) {
                            String action = config.getString(item_path + ".action");
                            if (action != null) {
                                if (action.equalsIgnoreCase("confirm_upgrade")) {
                                    NBTItem nbtItem = NBTItem.get(upgradeItem);
                                    NBTItem nbtMainItem = NBTItem.get(p.getInventory().getItemInMainHand());
                                    if (nbtItem.hasType() && nbtMainItem.hasType()) {
                                        String type = nbtItem.getType();
                                        String mainType = nbtMainItem.getType();
                                        String item_id_check = nbtItem.getString("MMOITEMS_ITEM_ID");
                                        String mainID = nbtMainItem.getString("MMOITEMS_ITEM_ID");
                                        String final_id;
                                        int level = 0;
                                        int index = item_id_check.lastIndexOf('_');
                                        if (index != -1 && index < item_id_check.length() - 1) {
                                            final_id = item_id_check.substring(0, index);
                                            level = Number.getInteger(item_id_check.substring(index + 1));
                                        } else {
                                            final_id = item_id_check;
                                        }
                                        String result_id = final_id + "_" + (level + 1);
                                        ItemStack nextItem = MMOItems.plugin.getItem(type, result_id);
                                        if (type.equalsIgnoreCase(mainType) && item_id_check.equalsIgnoreCase(mainID)) {
                                            if (nextItem != null) {
                                                List<String> requirementsItem = PlaceholderAPI.setPlaceholders(p,
                                                        config.getStringList("item_upgrade." + type + ";" + final_id + ".item_requirements.ingredients_" + level));
                                                String cost_papi = config.getString("item_upgrade." + type + ";" + final_id + ".cost.placeholder");
                                                String papi_parse = config.getString("item_upgrade." + type + ";" + final_id + ".cost.papi_parse");
                                                String papi_format = config.getString("item_upgrade." + type + ";" + final_id + ".cost.papi_format");
                                                int price = config.getInt("item_upgrade." + type + ";" + final_id + ".cost.price");
                                                List<String> command = config.getStringList("item_upgrade." + type + ";" + final_id + ".cost.command");
                                                if (cost_papi != null && ItemUpgrade.cost(p, cost_papi, papi_parse, papi_format, level, price)) {
                                                    if (ItemUpgrade.getMeetItemsRequirement(p, requirementsItem)) {
                                                        String papi = PlaceholderAPI.setPlaceholders(p, cost_papi.replace("<money>", String.valueOf(price))
                                                                .replace("<level>", String.valueOf(level)));
                                                        int cost = (int) Double.parseDouble(Calculator.calculator(papi, 0));
                                                        for (String cmd : command) {
                                                            Bukkit.getServer().dispatchCommand(
                                                                    Bukkit.getServer().getConsoleSender(),
                                                                    PlaceholderAPI.setPlaceholders(p, cmd.replace("<cost>", String.valueOf(cost))
                                                                            .replace("<level>", String.valueOf(level))));
                                                        }
                                                        p.closeInventory();
                                                        p.getInventory().setItemInMainHand(nextItem);
                                                        Chat.sendMessage(p,
                                                                Files.getMessage().getString("user.upgrade_item.upgrade_success"));
                                                    }
                                                } else {
                                                    Chat.sendMessage(p,
                                                            Files.getMessage().getString("user.upgrade_item.not_enough_item"));
                                                }
                                            } else {
                                                Chat.sendMessage(p,
                                                        Files.getMessage().getString("user.upgrade_item.reach_max_upgrade"));
                                            }
                                        }
                                    }
                                } else if (action.equalsIgnoreCase("force_upgrade")) {
                                    NBTItem nbtItem = NBTItem.get(upgradeItem);
                                    NBTItem nbtMainItem = NBTItem.get(p.getInventory().getItemInMainHand());
                                    if (nbtItem.hasType()) {
                                        String type = nbtItem.getType();
                                        String mainType = nbtMainItem.getType();
                                        String item_id_check = nbtItem.getString("MMOITEMS_ITEM_ID");
                                        String mainID = nbtMainItem.getString("MMOITEMS_ITEM_ID");
                                        String final_id;
                                        int level = 0;
                                        int index = item_id_check.lastIndexOf('_');
                                        if (index != -1 && index < item_id_check.length() - 1) {
                                            final_id = item_id_check.substring(0, index);
                                            level = Number.getInteger(item_id_check.substring(index + 1));
                                        } else {
                                            final_id = item_id_check;
                                        }
                                        String result_id = final_id + "_" + (level + 1);
                                        ItemStack nextItem = MMOItems.plugin.getItem(type, result_id);
                                        if (type.equalsIgnoreCase(mainType) && item_id_check.equalsIgnoreCase(mainID)) {
                                            if (nextItem != null) {
                                                List<String> requirementsItem = PlaceholderAPI.setPlaceholders(p,
                                                        config.getStringList(item_path + ".force_upgrade_item"));
                                                if (ItemUpgrade.getMeetItemsRequirement(p, requirementsItem)) {
                                                    p.closeInventory();
                                                    p.getInventory().setItemInMainHand(nextItem);
                                                    Chat.sendMessage(p,
                                                            Files.getMessage().getString("user.upgrade_item.upgrade_success"));
                                                } else {
                                                    Chat.sendMessage(p,
                                                            Files.getMessage().getString("user.upgrade_item.not_enough_item"));
                                                }
                                            } else {
                                                Chat.sendMessage(p,
                                                        Files.getMessage().getString("user.upgrade_item.reach_max_upgrade"));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
