package fr.Indyuce.throwables;

import fr.Indyuce.throwables.api.ThrowablesAPI;
import fr.Indyuce.throwables.command.ThrowablesCompleter;
import fr.Indyuce.throwables.command.ThrowablesExecutor;
import fr.Indyuce.throwables.compat.Metrics;
import fr.Indyuce.throwables.compat.color.ColorParser;
import fr.Indyuce.throwables.compat.color.HexColorParser;
import fr.Indyuce.throwables.compat.color.SimpleColorParser;
import fr.Indyuce.throwables.listener.PlayerListener;
import fr.Indyuce.throwables.listener.ThrowablesListener;
import fr.Indyuce.throwables.manager.ThrowableManager;
import fr.Indyuce.throwables.manager.ThrownItemManager;
import fr.Indyuce.throwables.player.PlayerData;
import fr.Indyuce.throwables.version.ServerVersion;
import fr.Indyuce.throwables.version.SpigotPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;

public class Throwables extends JavaPlugin {
    public static Throwables plugin;

    public final ThrowableManager throwableManager = new ThrowableManager();
    public final ThrownItemManager thrownItemManager = new ThrownItemManager();
    public final ThrowablesAPI api = new ThrowablesAPI(this);

    public ColorParser colorParser;
    public ServerVersion version;

    @Override
    public void onEnable() {

        plugin = this;

        // Update checker
        new SpigotPlugin(95551, this);

        // Metrics
        new Metrics(this);

        // Find plugin version
        version = new ServerVersion(Bukkit.getServer().getClass());

        // Hex color support
        colorParser = version.isStrictlyHigher(1, 15) ? new HexColorParser() : new SimpleColorParser();

        // Register events
        Bukkit.getPluginManager().registerEvents(new ThrowablesListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

        // Load player data of online players
        Bukkit.getOnlinePlayers().forEach(online -> PlayerData.setup(online));

        // Plugin commands
        getCommand("throwables").setExecutor(new ThrowablesExecutor());
        getCommand("throwables").setTabCompleter(new ThrowablesCompleter());

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
