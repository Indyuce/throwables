package fr.Indyuce.throwables.throwable.itemstat;

import fr.Indyuce.throwables.throwable.itemstat.data.DoubleStatData;
import fr.Indyuce.throwables.util.UtilityMethods;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

public class DoubleStat extends ItemStat<DoubleStatData> {
    public DoubleStat(String id, String nbtPath) {
        super(id, nbtPath);
    }

    @Override
    public DoubleStatData fromItem(ItemStack item, ItemMeta meta) {
        Double d = meta.getPersistentDataContainer().get(UtilityMethods.namespacedKey(getNBTPath()), PersistentDataType.DOUBLE);
        return d == null ? null : new DoubleStatData(this, d);
    }

    @Nullable
    @Override
    public DoubleStatData fromConfig(Object object) {
        UtilityMethods.isTrue(object instanceof Number, "Object must be a double, given " + object.getClass().getSimpleName());
        return new DoubleStatData(this, Double.valueOf(object.toString()));
    }
}
