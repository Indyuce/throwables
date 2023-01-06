package fr.Indyuce.throwables.throwable.itemstat.data;

import fr.Indyuce.throwables.throwable.itemstat.ItemStat;
import fr.Indyuce.throwables.util.Placeholders;
import fr.Indyuce.throwables.util.UtilityMethods;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.text.DecimalFormat;

public class DoubleStatData extends StatData {
    private final double value;

    private static final DecimalFormat doubleFormat = new DecimalFormat("0.####");

    public DoubleStatData(ItemStat stat, double value) {
        super(stat);

        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public void whenApplied(ItemStack item, ItemMeta meta, Placeholders holders) {
        meta.getPersistentDataContainer().set(UtilityMethods.namespacedKey(getStat().getNBTPath()), PersistentDataType.DOUBLE, value);
        holders.register(getStat().getId(), doubleFormat.format(value));
    }
}
