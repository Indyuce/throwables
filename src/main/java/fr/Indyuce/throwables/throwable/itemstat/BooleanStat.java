package fr.Indyuce.throwables.throwable.itemstat;

import fr.Indyuce.throwables.throwable.itemstat.data.BooleanStatData;
import fr.Indyuce.throwables.util.Utils;
import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

public class BooleanStat extends ItemStat<BooleanStatData> {
    private static final byte TRUE = 1;

    public BooleanStat(String id, String nbtPath) {
        super(id, nbtPath);
    }

    @Override
    public BooleanStatData fromItem(ItemStack item, ItemMeta meta) {
        Byte b = meta.getPersistentDataContainer().get(Utils.namespacedKey(getNBTPath()), PersistentDataType.BYTE);
        return b == null ? null : new BooleanStatData(this, b == TRUE);
    }

    @Nullable
    @Override
    public BooleanStatData fromConfig(Object object) {
        Validate.isTrue(object instanceof Boolean, "Object must be a boolean, given " + object.getClass().getSimpleName());
        return new BooleanStatData(this, (boolean) object);
    }
}
