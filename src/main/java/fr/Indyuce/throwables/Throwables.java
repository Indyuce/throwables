package fr.Indyuce.throwables;

import fr.Indyuce.throwables.api.ThrowablesAPI;
import fr.Indyuce.throwables.command.ThrowablesCommandExecutor;
import fr.Indyuce.throwables.listener.PlayerListener;
import fr.Indyuce.throwables.listener.ThrowablesListener;
import fr.Indyuce.throwables.manager.ThrowableManager;
import fr.Indyuce.throwables.manager.ThrownItemManager;
import fr.Indyuce.throwables.player.PlayerData;
import fr.Indyuce.throwables.version.ServerVersion;
import fr.Indyuce.throwables.version.wrapper.VersionWrapper;
import fr.Indyuce.throwables.version.wrapper.VersionWrapper_1_17_R1;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;

public class Throwables extends JavaPlugin {
    public static Throwables plugin;

    public final ThrowableManager throwableManager = new ThrowableManager();
    public final ThrownItemManager thrownItemManager = new ThrownItemManager();
    public final ThrowablesAPI api = new ThrowablesAPI(this);

    public VersionWrapper versionWrapper = new VersionWrapper_1_17_R1();
    public ServerVersion version;

    @Override
    public void onEnable() {

        plugin = this;

        // Register events
        Bukkit.getPluginManager().registerEvents(new ThrowablesListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

        // Load player data of online players
        Bukkit.getOnlinePlayers().forEach(online -> PlayerData.setup(online));

        // Plugin commands
        getCommand("throwables").setExecutor(new ThrowablesCommandExecutor());

        // Default config
        saveDefaultConfig();

        // Load throwables from config
        throwableManager.loadConfigThrowables();
    }

    @Override
    public void onDisable() {

        // Remove thrown items
        new HashSet<>(thrownItemManager.getLiving()).forEach(living -> living.remove());
    }
}
