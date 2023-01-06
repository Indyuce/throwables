package fr.Indyuce.throwables.throwable.itemstat.data;

import fr.Indyuce.throwables.throwable.itemstat.ItemStat;
import fr.Indyuce.throwables.util.Placeholders;
import fr.Indyuce.throwables.util.UtilityMethods;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class StringStatData extends StatData {
    private final String value;

    public StringStatData(ItemStat stat, String value) {
        super(stat);

        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void whenApplied(ItemStack item, ItemMeta meta, Placeholders holders) {
        meta.getPersistentDataContainer().set(UtilityMethods.namespacedKey(getStat().getNBTPath()), PersistentDataType.STRING, value);
    }
}
