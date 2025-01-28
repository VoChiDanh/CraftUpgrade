package net.danh.craftUpgrade.gui.upgrade_gui;

import io.lumine.mythic.lib.api.item.NBTItem;
import me.clip.placeholderapi.PlaceholderAPI;
import net.Indyuce.mmoitems.MMOItems;
import net.danh.craftUpgrade.resources.Chat;
import net.danh.craftUpgrade.resources.Files;
import net.danh.craftUpgrade.resources.Number;
import net.danh.craftUpgrade.utils.Calculator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ItemUpgrade {

    public static void getInventory(Player p, ItemStack upgradeItem) {
        Inventory inv = Bukkit.createInventory(p, Files.getItemUpgrade().getInt("gui_upgrade.size") * 9,
                Chat.colorize(Files.getItemUpgrade().getString("gui_upgrade.title")));
        FileConfiguration config = Files.getItemUpgrade();
        String gui_path = "gui_upgrade.items";
        for (String item_id : Objects.requireNonNull(config.getConfigurationSection(gui_path)).getKeys(false)) {
            if (item_id.equalsIgnoreCase("default_upgrade_item")) {
                int slot = config.getInt(gui_path + "." + item_id);
                inv.setItem(slot, upgradeItem);
            } else if (item_id.equalsIgnoreCase("upgraded_item")) {
                NBTItem nbtItem = NBTItem.get(upgradeItem);
                if (nbtItem.hasType()) {
                    String type = nbtItem.getType();
                    String item_id_check = nbtItem.getString("MMOITEMS_ITEM_ID");
                    String final_id;
                    int level = 0;
                    int index = item_id_check.lastIndexOf('_');
                    if (index != -1 && index < item_id_check.length() - 1) {
                        final_id = item_id_check.substring(0, index);
                        level = Number.getInteger(item_id_check.substring(index + 1));
                    } else {
                        final_id = item_id_check;
                    }
                    if (config.contains("item_upgrade." + type + ";" + final_id)) {
                        List<ItemStack> reqList = getReqItems(type, final_id, level);
                        List<Integer> listSlot = config.getIntegerList(gui_path + ".requirement_items.slots");
                        for (int i = 0; i < listSlot.size(); i++) {
                            if (reqList.size() > i) {
                                ItemStack itemStack = reqList.get(i);
                                inv.setItem(listSlot.get(i), itemStack);
                            } else {
                                String item_path = gui_path + ".requirement_items.none_items";
                                ItemStack itemStack = getItem(item_path);
                                inv.setItem(listSlot.get(i), itemStack);
                            }
                        }
                        String result_id = final_id + "_" + (level + 1);
                        ItemStack itemStack = MMOItems.plugin.getItem(type, result_id);
                        int slot = config.getInt(gui_path + ".upgraded_item.slot");
                        if (itemStack != null) {
                            inv.setItem(slot, itemStack);
                        } else {
                            inv.setItem(slot, getItem(gui_path + ".upgraded_item.max_items"));
                        }
                    }
                }
            } else {
                String item_path = gui_path + "." + item_id;
                ItemStack itemStack = getItem(item_path);
                if (itemStack != null) {
                    if (config.contains(item_path + ".slot")) {
                        int slot = config.getInt(item_path + ".slot");
                        inv.setItem(slot, itemStack);
                    } else if (config.contains(item_path + ".slots")) {
                        for (int slot : config.getIntegerList(item_path + ".slots")) {
                            inv.setItem(slot, itemStack);
                        }
                    }
                }
            }
        }
        p.openInventory(inv);
    }

    public static boolean getMeetItemsRequirement(@NotNull Player p, @NotNull List<String> requirementsItem) {
        int amountCheck = 0;
        List<String> checkedItems = new ArrayList<>();
        for (ItemStack itemStack : p.getInventory().getContents()) {
            for (String s : requirementsItem) {
                if (!checkedItems.contains(s) && amountCheck < requirementsItem.size()) {
                    String[] reqSplit = s.split(";");
                    String itemPlugin = reqSplit[0];
                    if (itemPlugin.equalsIgnoreCase("MMOITEMS")) {
                        String itemType = reqSplit[1];
                        String itemID = reqSplit[2];
                        int amount = Integer.parseInt(reqSplit[3]);
                        if (itemStack != null) {
                            NBTItem nbtItem = NBTItem.get(itemStack);
                            if (nbtItem != null) {
                                if (nbtItem.hasType() && nbtItem.getType().equalsIgnoreCase(itemType)) {
                                    if (nbtItem.getString("MMOITEMS_ITEM_ID").equalsIgnoreCase(itemID)) {
                                        if (getPlayerAmount(p, itemStack) >= amount) {
                                            amountCheck++;
                                            checkedItems.add(s);
                                        }
                                    }
                                }
                            }
                        }
                    } else if (itemPlugin.equalsIgnoreCase("VANILLA")) {
                        String itemType = reqSplit[1];
                        int amount = Integer.parseInt(reqSplit[2]);
                        if (itemStack != null) {
                            if (itemStack.getType() != Material.AIR) {
                                if (itemStack.getType().toString().equals(itemType)
                                        && !itemStack.hasItemMeta()) {
                                    if (getPlayerAmount(p, itemStack) >= amount) {
                                        amountCheck++;
                                        checkedItems.add(s);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (amountCheck == requirementsItem.size() && checkedItems.size() == requirementsItem.size())
            removeItemReq(p, checkedItems);

        return amountCheck == requirementsItem.size() && checkedItems.size() == requirementsItem.size();
    }

    private static void removeItemReq(@NotNull Player p, List<String> checkedItems) {
        int amountCheck = 0;
        List<String> removedItems = new ArrayList<>();
        for (ItemStack itemStack : p.getInventory().getContents()) {
            for (String checkID : checkedItems) {
                if (!removedItems.contains(checkID) && amountCheck < checkedItems.size()) {
                    String[] reqSplit = checkID.split(";");
                    String itemPlugin = reqSplit[0];
                    if (itemPlugin.equalsIgnoreCase("MMOITEMS")) {
                        String itemType = reqSplit[1];
                        String itemID = reqSplit[2];
                        int amount = Integer.parseInt(reqSplit[3]);
                        if (itemStack != null) {
                            NBTItem nbtItem = NBTItem.get(itemStack);
                            if (nbtItem != null) {
                                if (nbtItem.hasType() && nbtItem.getType().equalsIgnoreCase(itemType)) {
                                    if (nbtItem.getString("MMOITEMS_ITEM_ID").equalsIgnoreCase(itemID)) {
                                        if (getPlayerAmount(p, itemStack) >= amount) {
                                            removeItems(p, itemStack, amount);
                                            amountCheck++;
                                            removedItems.add(checkID);
                                        }
                                    }
                                }
                            }
                        }
                    } else if (itemPlugin.equalsIgnoreCase("VANILLA")) {
                        String itemType = reqSplit[1];
                        int amount = Integer.parseInt(reqSplit[2]);
                        if (itemStack != null) {
                            if (itemStack.getType() != Material.AIR) {
                                if (itemStack.getType().toString().equals(itemType)
                                        && !itemStack.hasItemMeta()) {
                                    if (getPlayerAmount(p, itemStack) >= amount) {
                                        removeItems(p, itemStack, amount);
                                        amountCheck++;
                                        removedItems.add(checkID);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static @NotNull List<ItemStack> getReqItems(String type, String final_id, int level) {
        List<ItemStack> reqList = new ArrayList<>();
        FileConfiguration config = Files.getItemUpgrade();
        if (Objects.requireNonNull(config.getConfigurationSection("item_upgrade." + type + ";" + final_id + ".item_requirements"))
                .getKeys(false).size() >= level) {
            List<String> ingredients =
                    config.getStringList("item_upgrade." + type + ";" + final_id + ".item_requirements.ingredients_" + level);
            int amount_check = 0;
            for (String s : ingredients) {
                String[] reqSplit = s.split(";");
                String itemPlugin = reqSplit[0];
                if (itemPlugin.equalsIgnoreCase("MMOITEMS")) {
                    String itemType = reqSplit[1];
                    String itemID = reqSplit[2];
                    int amount = Integer.parseInt(reqSplit[3]);
                    ItemStack itemStack = MMOItems.plugin.getItem(itemType, itemID);
                    if (itemStack != null) {
                        if (amount_check <= 640
                                && (amount_check + amount) <= 640) {
                            while (amount > 0) {
                                ItemStack stack = itemStack.clone();
                                stack.setAmount(Math.min(amount, stack.getMaxStackSize()));
                                reqList.add(stack);
                                amount_check += stack.getAmount();
                                amount -= stack.getAmount();
                            }
                        }
                    }
                }
            }
        }
        return reqList;
    }

    public static @Nullable ItemStack getItem(String item_path) {
        FileConfiguration config = Files.getItemUpgrade();
        String materialID = config.getString(item_path + ".material");
        if (materialID != null) {
            Material material = Material.getMaterial(materialID);
            if (material != null) {
                ItemStack itemStack = new ItemStack(material);
                ItemMeta meta = itemStack.getItemMeta();
                if (config.contains(item_path + ".enchants")) {
                    for (String enchantID : config.getStringList(item_path + ".enchants")) {
                        String[] enchantmentsID = enchantID.split(";");
                        Enchantment enchantment = Enchantment.getByName(enchantmentsID[0]);
                        if (enchantment != null) {
                            meta.addEnchant(enchantment, Number.getInteger(enchantmentsID[1]), true);
                        }
                    }
                }
                if (config.contains(item_path + ".flags")) {
                    for (String flagID : config.getStringList(item_path + ".flags")) {
                        meta.addItemFlags(ItemFlag.valueOf(flagID));
                    }
                }
                if (config.contains(item_path + ".display")) {
                    meta.setDisplayName(Chat.normalColorize(config.getString(item_path + ".display")));
                }
                if (config.contains(item_path + ".lore")) {
                    meta.setLore(Chat.normalColorize(config.getStringList(item_path + ".lore")));
                }
                itemStack.setItemMeta(meta);
                return itemStack;
            }
        }
        return null;
    }

    public static int getPlayerAmount(@NotNull HumanEntity player, ItemStack item) {
        final PlayerInventory inv = player.getInventory();
        final ItemStack[] items = inv.getContents();
        int c = 0;
        for (final ItemStack is : items) {
            if (is != null) {
                if (is.isSimilar(item)) {
                    c += is.getAmount();
                }
            }
        }
        return c;
    }

    public static void removeItems(@NotNull Player player, ItemStack item, long amount) {
        item = item.clone();
        final PlayerInventory inv = player.getInventory();
        final ItemStack[] items = inv.getContents();
        int c = 0;
        for (int i = 0; i < items.length; ++i) {
            final ItemStack is = items[i];
            if (is != null) {
                if (is.isSimilar(item)) {
                    if (c + is.getAmount() > amount) {
                        final long canDelete = amount - c;
                        is.setAmount((int) (is.getAmount() - canDelete));
                        items[i] = is;
                        break;
                    }
                    c += is.getAmount();
                    items[i] = null;
                }
            }
        }
        inv.setContents(items);
        player.updateInventory();
    }

    public static boolean cost(Player p, @NotNull String cost_papi, String papi_parse, String papi_format, int level, int cost_money) {
        String papi = PlaceholderAPI.setPlaceholders(p, cost_papi.replace("<money>", String.valueOf(cost_money))
                .replace("<level>", String.valueOf(level)));
        int cost = (int) Double.parseDouble(Calculator.calculator(papi, 0));
        if (cost <= Double.parseDouble(PlaceholderAPI.setPlaceholders(p, papi_parse))) {
            return true;
        } else {
            DecimalFormat currencyFormat = new DecimalFormat("###,###.##");
            Chat.sendMessage(p, Objects.requireNonNull(Files.getMessage().getString("user.upgrade_item.not_enough_cost"))
                    .replace("<cost>", currencyFormat.format(cost))
                    .replace("<current>", PlaceholderAPI.setPlaceholders(p, papi_format)));
            return false;
        }
    }

}
