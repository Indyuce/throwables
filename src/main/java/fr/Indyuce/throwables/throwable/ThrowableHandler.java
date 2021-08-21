package fr.Indyuce.throwables.throwable;

import fr.Indyuce.throwables.player.CooldownScope;
import fr.Indyuce.throwables.throwable.itemstat.ItemStat;
import fr.Indyuce.throwables.throwable.itemstat.ItemStatList;
import fr.Indyuce.throwables.throwable.itemstat.data.StatData;
import fr.Indyuce.throwables.throwable.provided.axe.ThrowableAxeStatList;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.*;

/**
 * All the data that has to be loaded from an itemStack when
 * throwing an item is stored inside this object.
 * <p>
 * In order to generate an itemStack that can be thrown, some extra
 * data is required and it is stored in {@link ThrowableItem}
 */
public abstract class ThrowableHandler {
    private final ThrowableType type;
    private final Map<String, StatData> stats = new HashMap<>();

    /**
     * Config keys which must not be taken into account
     * when loading a throwable item from the config file
     */
    private static final List<String> otherConfigKeys = Arrays.asList("throwable-type", "material", "name", "lore", "custom-model-data");

    public ThrowableHandler(ThrowableType type) {
        this.type = type;
    }

    public ThrowableHandler(ThrowableType type, ConfigurationSection config) {
        this.type = type;

        for (String key : config.getKeys(false)) {
            if (otherConfigKeys.contains(key))
                continue;

            String id = key.toLowerCase().replace(" ", "-").replace("_", "-");
            ItemStat stat = getStatList().getById(id);
            registerData(Objects.requireNonNull(stat.fromConfig(config.get(key)), "Could not load data of stat '" + stat.getId() + "'"));
        }
    }

    public ThrowableHandler(ThrowableType type, ItemStack item) {
        this.type = type;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer nbt = item.getItemMeta().getPersistentDataContainer();

        // Load custom stats
        for (ItemStat stat : getStatList().getStats()) {
            StatData optional = stat.fromItem(item, meta);
            if (optional != null)
                registerData(optional);
        }
    }

    public ThrowableType getType() {
        return type;
    }

    public Collection<StatData> getDatas() {
        return stats.values();
    }

    public boolean hasData(ItemStat stat) {
        return stats.containsKey(stat.getId());
    }

    public <T extends StatData> T getData(ItemStat<T> stat) {
        return (T) stats.get(stat.getId());
    }

    public void registerData(StatData data) {
        stats.put(data.getStat().getId(), data);
    }

    public double getCooldown() {
        return hasData(ItemStatList.THROW_COOLDOWN) ? getData(ItemStatList.THROW_COOLDOWN).getValue() : getType().getDefaultCooldown();
    }

    public CooldownScope getCooldownScope() {
        return hasData(ItemStatList.THROW_COOLDOWN_SCOPE) ? getData(ItemStatList.THROW_COOLDOWN_SCOPE).getValue() : CooldownScope.DEFAULT_SCOPE;
    }

    public double getThrowDamage() {
        return hasData(ItemStatList.THROW_DAMAGE) ? getData(ItemStatList.THROW_DAMAGE).getValue() : getType().getDefaultDamage();
    }

    public double getThrowForce() {
        return hasData(ItemStatList.THROW_FORCE) ? getData(ItemStatList.THROW_FORCE).getValue() : 1;
    }

    public boolean isKeptOnThrow() {
        return hasData(ItemStatList.KEEP_ON_THROW) && getData(ItemStatList.KEEP_ON_THROW).getValue();
    }

    public double getGravityMultiplier() {
        return hasData(ThrowableAxeStatList.GRAVITY) ? getData(ThrowableAxeStatList.GRAVITY).getValue() : 1;
    }

    /**
     * @return The stat list that should be the same for every item
     *         of the same throwableType! This should return a STATIC field
     *         just like {@link Event#getHandlers()}
     */
    public abstract ItemStatList getStatList();
}
