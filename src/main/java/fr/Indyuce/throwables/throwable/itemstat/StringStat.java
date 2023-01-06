package fr.Indyuce.throwables.throwable.itemstat;

import fr.Indyuce.throwables.throwable.itemstat.data.StringStatData;
import fr.Indyuce.throwables.util.UtilityMethods;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

public class StringStat extends ItemStat<StringStatData> {
    public StringStat(String id, String nbtPath) {
        super(id, nbtPath);
    }

    @Override
    public StringStatData fromItem(ItemStack item, ItemMeta meta) {
        String str = meta.getPersistentDataContainer().get(UtilityMethods.namespacedKey(getNBTPath()), PersistentDataType.STRING);
        return str == null ? null : new StringStatData(this, str);
    }

    @Nullable
    @Override
    public StringStatData fromConfig(Object object) {
        return new StringStatData(this, object.toString());
    }
}
