package fr.Indyuce.throwables.throwable.itemstat;

import fr.Indyuce.throwables.player.CooldownScope;
import fr.Indyuce.throwables.throwable.itemstat.data.CooldownScopeData;
import fr.Indyuce.throwables.util.Utils;
import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

public class CooldownScopeStat extends ItemStat<CooldownScopeData> {
    public CooldownScopeStat() {
        super("cooldown-scope", "ThrowCooldownScope");
    }

    @Override
    public CooldownScopeData fromItem(ItemStack item, ItemMeta meta) {
        String tag = meta.getPersistentDataContainer().get(Utils.namespacedKey(getNBTPath()), PersistentDataType.STRING);
        return tag == null || tag.isEmpty() ? null : new CooldownScopeData(CooldownScope.valueOf(tag));
    }

    @Nullable
    @Override
    public CooldownScopeData fromConfig(Object object) {
        Validate.isTrue(object instanceof String, "Object must be a string, given " + object.getClass().getSimpleName());
        return new CooldownScopeData(CooldownScope.valueOf(object.toString().toUpperCase()));
    }
}
