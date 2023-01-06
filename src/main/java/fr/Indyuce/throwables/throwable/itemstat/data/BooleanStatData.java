package fr.Indyuce.throwables.throwable.itemstat.data;

import fr.Indyuce.throwables.throwable.itemstat.ItemStat;
import fr.Indyuce.throwables.util.Placeholders;
import fr.Indyuce.throwables.util.UtilityMethods;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * There is no PersistentDataType for booleans
 * so there are stored using bytes in the item NBT
 */
public class BooleanStatData extends StatData {
    private final boolean value;

    public BooleanStatData(ItemStat stat, boolean value) {
        super(stat);

        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public void whenApplied(ItemStack item, ItemMeta meta, Placeholders holders) {
        meta.getPersistentDataContainer().set(UtilityMethods.namespacedKey(getStat().getNBTPath()), PersistentDataType.BYTE, (byte) (value ? 1 : 0));
    }
}
