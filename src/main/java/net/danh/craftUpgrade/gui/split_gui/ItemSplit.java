package net.danh.craftUpgrade.gui.split_gui;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.danh.craftUpgrade.resources.Chat;
import net.danh.craftUpgrade.resources.Files;
import net.danh.craftUpgrade.resources.Number;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ItemSplit {

    public static void getInventory(Player p) {
        Inventory inv = Bukkit.createInventory(p, Files.getItemSplit().getInt("gui_split.size") * 9,
                Chat.colorize(Files.getItemSplit().getString("gui_split.title")));
        FileConfiguration config = Files.getItemSplit();
        String gui_path = "gui_split.items";
        for (String item_id : Objects.requireNonNull(config.getConfigurationSection(gui_path)).getKeys(false)) {
            if (item_id.equalsIgnoreCase("default_split_item")) {
                NBTItem nbtItem = NBTItem.get(p.getInventory().getItemInMainHand());
                if (nbtItem.hasType()) {
                    String type = nbtItem.getType();
                    String item_id_check = nbtItem.getString("MMOITEMS_ITEM_ID");
                    int slot = config.getInt(gui_path + "." + item_id);
                    inv.setItem(slot, p.getInventory().getItemInMainHand());
                    if (config.contains("item_split." + type + ";" + item_id_check)) {
                        List<ItemStack> reqList = getSplitItems(type, item_id_check);
                        List<Integer> listSlot = config.getIntegerList(gui_path + ".split_items.slots");
                        for (int i = 0; i < listSlot.size(); i++) {
                            if (reqList.size() > i) {
                                ItemStack itemStack = reqList.get(i);
                                inv.setItem(listSlot.get(i), itemStack);
                            } else {
                                String item_path = gui_path + ".split_items.none_items";
                                ItemStack itemStack = getItem(item_path);
                                inv.setItem(listSlot.get(i), itemStack);
                            }
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

    public static @NotNull List<ItemStack> getSplitItems(String type, String final_id) {
        List<ItemStack> reqList = new ArrayList<>();
        FileConfiguration config = Files.getItemSplit();
        if (!config.getStringList("item_split." + type + ";" + final_id).isEmpty()) {
            List<String> split =
                    config.getStringList("item_split." + type + ";" + final_id);
            int amount_check = 0;
            for (String s : split) {
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
        FileConfiguration config = Files.getItemSplit();
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

}
