package fr.Indyuce.throwables.throwable.itemstat;

import fr.Indyuce.throwables.throwable.itemstat.data.StatData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

public abstract class ItemStat<T extends StatData> {
    private final String id, nbtPath;

    public ItemStat(String id, String nbtPath) {
        this.id = id.toLowerCase().replace(" ", "-").replace("_", "-");
        this.nbtPath = nbtPath;
    }

    public String getId() {
        return id;
    }

    /**
     * @return The string key that will be used to store the
     *         stat data in the item NBT
     */
    public String getNBTPath() {
        return nbtPath;
    }

    /**
     * @return The string key that is used in the config to add
     *         a stat to a throwable item
     */
    public String getConfigPath() {
        return getId();
    }

    /**
     * Called when loading stat data from an item.
     *
     * @param item Item to load data from
     * @param meta Item meta to load data from
     * @return Corresponding stat data, or null if none
     */
    @Nullable
    public abstract T fromItem(ItemStack item, ItemMeta meta);

    /**
     * Called when loading a throwable item from the config files.
     *
     * @param object Could be a string, boolean, double, configuration section.
     * @return Corresponding stat data, or null if none
     */
    @Nullable
    public abstract T fromConfig(Object object);
}
