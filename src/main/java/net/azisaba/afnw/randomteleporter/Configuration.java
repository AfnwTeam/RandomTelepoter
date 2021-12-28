package net.azisaba.afnw.randomteleporter;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class Configuration implements CommandExecutor {
    private final Plugin plugin;
    @Getter private World world;
    @Getter private int delaySec;
    @Getter private int xMax;
    @Getter private int zMax;

    public Configuration (Plugin plugin) {
        this.plugin = plugin;
        this.loadConfig();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("rtpreload")) return false;
        if(!this.loadConfig()){
            sender.sendMessage("config.yml の読み込みに失敗しました．");
            return false;
        }
        sender.sendMessage("config.yml の読み込みに成功しました．");
        return true;
    }

    private boolean loadConfig () {
        this.plugin.saveDefaultConfig();
        this.plugin.reloadConfig();
        FileConfiguration fc = this.plugin.getConfig();

        ConfigurationSection sc = fc.getConfigurationSection("General");
        if (sc == null){
            this.plugin.getLogger().info(ChatColor.RED + "General not Found.");
            return false;
        }

        String worldName = sc.getString("specificTpWorldName");
        if (worldName == null){
            this.plugin.getLogger().info(ChatColor.RED + "General.specificTpWorldName not Found.");
            return false;
        }
        this.world = Bukkit.getWorld(worldName);
        if (this.world == null){
            this.plugin.getLogger().info(ChatColor.RED + worldName + " is not Found.");
            return false;
        }

        this.delaySec = sc.getInt("delaySec");
        this.xMax = sc.getInt("xMax");
        this.zMax = sc.getInt("zMax");
        return true;
    }
}
