package net.azisaba.afnw.randomteleporter;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class RandomTeleporter extends JavaPlugin {
    private Configuration config;
    private Teleporter teleporter;

    @Override
    public void onEnable() {
        this.config = new Configuration(this);
        this.teleporter = new Teleporter(this, this.config);

        Objects.requireNonNull(getCommand("rtpreload")).setExecutor(this.config);
        Objects.requireNonNull(getCommand("givertp")).setExecutor(this.teleporter);
        Bukkit.getServer().getPluginManager().registerEvents(this.teleporter, this);
    }
}
