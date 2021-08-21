package fr.Indyuce.throwables.throwable.itemstat.data;

import fr.Indyuce.throwables.player.CooldownScope;
import fr.Indyuce.throwables.throwable.itemstat.ItemStatList;
import fr.Indyuce.throwables.util.Placeholders;
import fr.Indyuce.throwables.util.Utils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class CooldownScopeData extends StatData {
    private final CooldownScope value;

    public CooldownScopeData(CooldownScope value) {
        super(ItemStatList.THROW_COOLDOWN_SCOPE);

        this.value = value;
    }

    public CooldownScope getValue() {
        return value;
    }

    @Override
    public void whenApplied(ItemStack item, ItemMeta meta, Placeholders holders) {
        meta.getPersistentDataContainer().set(Utils.namespacedKey(getStat().getNBTPath()), PersistentDataType.STRING, value.name());
    }
}
