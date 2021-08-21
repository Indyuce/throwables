package fr.Indyuce.throwables.throwable.itemstat.data;

import fr.Indyuce.throwables.throwable.ThrowableItem;
import fr.Indyuce.throwables.throwable.itemstat.ItemStat;
import fr.Indyuce.throwables.util.Placeholders;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class StatData {
    private final ItemStat stat;

    public StatData(ItemStat stat) {
        this.stat = stat;
    }

    public ItemStat getStat() {
        return stat;
    }

    /**
     * Called when building an itemStack from all the data saved
     * in a {@link ThrowableItem} instance.
     * <p>
     * This should register the required NBT tags as well as placeholders.
     *
     * @param meta    Item meta of the item being generated
     * @param holders Placeholders which will apply in the item name and lore
     */
    public abstract void whenApplied(ItemStack item, ItemMeta meta, Placeholders holders);
}
