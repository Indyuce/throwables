package fr.Indyuce.throwables.manager;

import fr.Indyuce.throwables.Throwables;
import fr.Indyuce.throwables.throwable.ThrowableHandler;
import fr.Indyuce.throwables.throwable.ThrowableItem;
import fr.Indyuce.throwables.throwable.ThrowableType;
import fr.Indyuce.throwables.throwable.provided.axe.ThrowableAxeType;
import fr.Indyuce.throwables.throwable.provided.sword.ThrowableSwordType;
import fr.Indyuce.throwables.util.ConfigFile;
import fr.Indyuce.throwables.util.UtilityMethods;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Throwable type and throwables names are HIGHLY case and character sensitive!
 */
public class ThrowableManager {
    private final Map<String, ThrowableType> mapped = new HashMap<>();
    private final Map<String, ThrowableItem> throwables = new HashMap<>();
    private final Map<Material, ThrowableHandler> defaultHandlers = new HashMap<>();

    /**
     * Type registration must be done before loading throwables
     * otherwise some won't get recognized.
     * <p>
     * To do that, you can register types using {@link JavaPlugin#onLoad()}
     * and Throwables as a (soft) dependency to makes sure it loads beforehand,
     * or using Throwables as a 'loadbefore' and do it onEnable().
     */
    private boolean typeRegistration = true;

    public ThrowableManager() {
        registerHandler(new ThrowableAxeType());
        registerHandler(new ThrowableSwordType());
    }

    public ThrowableType getById(String id) {
        return mapped.containsKey(id) ? mapped.get(id) : null;
    }

    /**
     * @param item Item which might be a throwable item
     * @return Null if the item cannot be thrown. ThrowableHandler otherwise
     */
    @Nullable
    public ThrowableHandler getHandler(ItemStack item) {

        if (!item.hasItemMeta())
            return getDefaultHandler(item.getType());

        PersistentDataContainer nbt = item.getItemMeta().getPersistentDataContainer();
        String tag = nbt.get(UtilityMethods.namespacedKey("ThrowableType"), PersistentDataType.STRING);
        if (tag == null || tag.isEmpty())
            return getDefaultHandler(item.getType());

        return getThrowableType(tag).getHandler(item);
    }

    public ThrowableItem getThrowable(String id) {
        return throwables.get(id);
    }

    public ThrowableType getThrowableType(String id) {
        return mapped.get(id);
    }

    public ThrowableHandler getDefaultHandler(Material material) {
        return defaultHandlers.get(material);
    }

    public Collection<ThrowableItem> getThrowables() {
        return throwables.values();
    }

    public void registerHandler(ThrowableType handler) {
        UtilityMethods.isTrue(typeRegistration, "Please register throwable types before the plugin enables");
        UtilityMethods.isTrue(!mapped.containsKey(handler.getId()), "Found a handler with the same ID ('" + handler.getId() + "')");

        mapped.put(handler.getId(), handler);
    }

    public void registerDefaultHandler(Material material, ThrowableHandler handler) {
        UtilityMethods.notNull(material, "Material cannot be null");
        UtilityMethods.notNull(handler, "Handler cannot be null");

        defaultHandlers.put(material, handler);
    }

    public void registerThrowable(ThrowableItem item) {
        UtilityMethods.isTrue(!throwables.containsKey(item.getId()), "Found a throwable with the same ID ('" + item.getId() + "')");

        throwables.put(item.getId(), item);
    }

    public void loadConfigThrowables() {

        // If it's used in the /reload command
        if (!typeRegistration) {
            throwables.clear();
            defaultHandlers.clear();
        }

        typeRegistration = false;

        ConfigurationSection defaultHandlersConfig = Throwables.plugin.getConfig().getConfigurationSection("default-handlers");
        for (String key : defaultHandlersConfig.getKeys(false))
            try {
                String materialFormat = key.toUpperCase().replace("-", "_").replace(" ", "_");
                Material material = Material.valueOf(materialFormat);
                String typeFormat = Objects.requireNonNull(defaultHandlersConfig.getString(key + ".throwable-type"), "Could not find throwable type");
                ThrowableType type = Objects.requireNonNull(getThrowableType(typeFormat), "Could not find throwable type with ID '" + typeFormat + "'");
                ThrowableHandler handler = type.loadHandler(defaultHandlersConfig.getConfigurationSection(key));
                registerDefaultHandler(material, handler);
            } catch (RuntimeException exception) {
                Throwables.plugin.getLogger().log(Level.WARNING, "Could not load default handler for '" + key + "': " + exception.getMessage());
            }

        FileConfiguration config = new ConfigFile("throwables").getConfig();
        for (String key : config.getKeys(false))
            try {
                String typeFormat = Objects.requireNonNull(config.getString(key + ".throwable-type"), "Please specify a throwable type");
                ThrowableType type = Objects.requireNonNull(getThrowableType(typeFormat), "Could not find throwable type '" + typeFormat + "'");
                registerThrowable(new ThrowableItem(type, config.getConfigurationSection(key)));
            } catch (RuntimeException exception) {
                Throwables.plugin.getLogger().log(Level.WARNING, "Could not load throwable '" + key + "': " + exception.getMessage());
            }

        Throwables.plugin.getLogger().log(Level.INFO, "Loaded " + throwables.size() + " throwable(s) from config");
    }
}
