package net.danh.craftUpgrade.cmd;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.danh.craftUpgrade.gui.InvClick;
import net.danh.craftUpgrade.gui.split_gui.ItemSplit;
import net.danh.craftUpgrade.gui.upgrade_gui.ItemUpgrade;
import net.danh.craftUpgrade.gui.upgrade_gui.PreviewItem;
import net.danh.craftUpgrade.resources.Chat;
import net.danh.craftUpgrade.resources.Files;
import net.danh.craftUpgrade.resources.Number;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CUC extends CMDBase {
    public CUC() {
        super("CraftUpgrade");
    }

    @Override
    public void execute(@NotNull CommandSender c, String[] args) {

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("list")) {
                if (c instanceof Player p) {
                    if (Objects.requireNonNull(Files.getItemUpgrade().getConfigurationSection("item_upgrade")).getKeys(false).contains(args[1])) {
                        InvClick.getIngredientsItems(p, args[1], 1);
                    }
                }
            }
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                if (c instanceof Player p) {
                    PreviewItem.getPreviewItems(p, 1);
                }
            }
            if (args[0].equalsIgnoreCase("check_upgrade")) {
                if (c instanceof Player p) {
                    ItemStack itemStack = p.getInventory().getItemInMainHand();
                    if (!itemStack.isEmpty() && itemStack.getType() != Material.AIR) {
                        FileConfiguration config = Files.getItemUpgrade();
                        NBTItem nbtItem = NBTItem.get(itemStack);
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
                            if (level > 0 && !final_id.equalsIgnoreCase(nbtItem.getString("MMOITEMS_ITEM_ID"))) {
                                if (config.contains("item_upgrade." + type + ";" + final_id + ".item_requirements")) {
                                    ItemUpgrade.getInventory(p, p.getInventory().getItemInMainHand());
                                }
                            }
                        } else
                            Chat.sendMessage(p, Files.getMessage().getString("user.upgrade_item.can_not_upgrade_item"));
                    } else Chat.sendMessage(p, Files.getMessage().getString("user.upgrade_item.do_not_have_item"));
                }
            }
            if (args[0].equalsIgnoreCase("check_split")) {
                if (c instanceof Player p) {
                    ItemStack itemStack = p.getInventory().getItemInMainHand();
                    if (!itemStack.isEmpty() && itemStack.getType() != Material.AIR) {
                        FileConfiguration config = Files.getItemSplit();
                        NBTItem nbtItem = NBTItem.get(itemStack);
                        if (nbtItem.hasType()) {
                            String type = nbtItem.getType();
                            String item_id = nbtItem.getString("MMOITEMS_ITEM_ID");
                            if (config.contains("item_split." + type + ";" + item_id)) {
                                ItemSplit.getInventory(p);
                            }
                        } else
                            Chat.sendMessage(p, Files.getMessage().getString("user.split_item.can_not_split_item"));
                    } else Chat.sendMessage(p, Files.getMessage().getString("user.split_item.do_not_have_item"));
                }
            }
        }
        if (c.hasPermission("craftUpgrade.admin")) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    Files.reloadFiles();
                    Chat.sendMessage(c, Files.getMessage().getString("admin.reload_files"));
                }
            }
        }
    }

    @Override
    public List<String> TabComplete(CommandSender sender, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> commands = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("craftUpgrade.admin")) {
                commands.add("reload");
            }
            commands.add("check_upgrade");
            commands.add("check_split");
            commands.add("list");
            StringUtil.copyPartialMatches(args[0], commands, completions);
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("list")) {
                commands.addAll(Objects.requireNonNull(Files.getItemUpgrade().getConfigurationSection("item_upgrade")).getKeys(false));
            }
            StringUtil.copyPartialMatches(args[1], commands, completions);
        }
        Collections.sort(completions);
        return completions;
    }
}
