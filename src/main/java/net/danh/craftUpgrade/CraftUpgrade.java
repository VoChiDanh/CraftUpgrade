package net.danh.craftUpgrade;

import net.danh.craftUpgrade.cmd.CUC;
import net.danh.craftUpgrade.gui.InvClick;
import net.danh.craftUpgrade.resources.Files;
import net.xconfig.bukkit.model.SimpleConfigurationManager;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public final class CraftUpgrade extends JavaPlugin {

    private static CraftUpgrade craftUpgrade;

    public static CraftUpgrade getCraftUpgrade() {
        return craftUpgrade;
    }

    @Override
    public void onEnable() {
        craftUpgrade = this;
        SimpleConfigurationManager.register(craftUpgrade);
        registerEvents(new InvClick());
        Files.loadFiles();
        new CUC();
    }

    @Override
    public void onDisable() {
        Files.saveFiles();
    }

    private void registerEvents(Listener... listeners) {
        Arrays.asList(listeners).forEach(listener -> {
            Bukkit.getPluginManager().registerEvents(listener, craftUpgrade);
            getLogger().info("Registered Listener " + listener);
        });
    }

}
