package fr.Indyuce.throwables.throwable.provided.axe;

import fr.Indyuce.throwables.throwable.ThrowableHandler;
import fr.Indyuce.throwables.throwable.ThrowableType;
import fr.Indyuce.throwables.throwable.itemstat.ItemStatList;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class ThrowableAxeHandler extends ThrowableHandler {
    public static ItemStatList statList = new ThrowableAxeStatList();

    private static final long DEFAULT_LIFE_SPAN = 10 * 60 * 20;

    public ThrowableAxeHandler(ThrowableType type) {
        super(type);
    }

    public ThrowableAxeHandler(ThrowableType type, ConfigurationSection config) {
        super(type, config);
    }

    public ThrowableAxeHandler(ThrowableType type, ItemStack item) {
        super(type, item);
    }

    @Deprecated
    public long getLifeSpan() {
        return hasData(ThrowableAxeStatList.LIFE_SPAN) ? (long) (getData(ThrowableAxeStatList.LIFE_SPAN).getValue() * 20) : DEFAULT_LIFE_SPAN;
    }

    public boolean hasLoyalty() {
        return hasData(ThrowableAxeStatList.LOYALTY) && getData(ThrowableAxeStatList.LOYALTY).getValue();
    }

    public boolean doesBounceBack() {
        return hasData(ThrowableAxeStatList.BOUNCE_BACK) && getData(ThrowableAxeStatList.BOUNCE_BACK).getValue();
    }

    @Override
    public ItemStatList getStatList() {
        return statList;
    }
}
