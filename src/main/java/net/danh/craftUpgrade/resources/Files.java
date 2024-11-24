package net.danh.craftUpgrade.resources;

import net.xconfig.bukkit.model.SimpleConfigurationManager;
import org.bukkit.configuration.file.FileConfiguration;

public class Files {

    public static void loadFiles() {
        SimpleConfigurationManager.get().build("", false, "config.yml", "message.yml", "item_upgrade.yml", "item_split.yml");
    }

    public static void saveFiles() {
        SimpleConfigurationManager.get().save("config.yml", "message.yml", "item_upgrade.yml", "item_split.yml");
    }

    public static void reloadFiles() {
        SimpleConfigurationManager.get().reload("config.yml", "message.yml", "item_upgrade.yml", "item_split.yml");
    }

    public static FileConfiguration getConfig() {
        return SimpleConfigurationManager.get().get("config.yml");
    }

    public static FileConfiguration getMessage() {
        return SimpleConfigurationManager.get().get("message.yml");
    }

    public static FileConfiguration getItemUpgrade() {
        return SimpleConfigurationManager.get().get("item_upgrade.yml");
    }

    public static FileConfiguration getItemSplit() {
        return SimpleConfigurationManager.get().get("item_split.yml");
    }

}
