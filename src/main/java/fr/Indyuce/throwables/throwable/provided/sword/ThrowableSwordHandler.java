package fr.Indyuce.throwables.throwable.provided.sword;

import fr.Indyuce.throwables.throwable.ThrowableHandler;
import fr.Indyuce.throwables.throwable.ThrowableType;
import fr.Indyuce.throwables.throwable.itemstat.ItemStatList;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class ThrowableSwordHandler extends ThrowableHandler {
    public static ItemStatList statList = new ThrowableSwordStatList();

    private static final double DEFAULT_BOOMERANG_DISTANCE = 50;

    public ThrowableSwordHandler(ThrowableType type) {
        super(type);
    }

    public ThrowableSwordHandler(ThrowableType type, ConfigurationSection config) {
        super(type, config);
    }

    public ThrowableSwordHandler(ThrowableType type, ItemStack item) {
        super(type, item);
    }

    public boolean isBoomerang() {
        return hasData(ThrowableSwordStatList.BOOMERANG) && getData(ThrowableSwordStatList.BOOMERANG).getValue();
    }

    public double getBoomerangDistance() {
        return hasData(ThrowableSwordStatList.BOOMERANG_MAX_DISTANCE) ? getData(ThrowableSwordStatList.BOOMERANG_MAX_DISTANCE).getValue() : DEFAULT_BOOMERANG_DISTANCE;
    }

    public boolean hasPiercing() {
        return hasData(ThrowableSwordStatList.PIERCING) && getData(ThrowableSwordStatList.PIERCING).getValue();
    }

    public double getPiercingDamageMultiplier() {
        return hasData(ThrowableSwordStatList.PIERCING_MULTIPLIER) ? getData(ThrowableSwordStatList.PIERCING_MULTIPLIER).getValue() : 1;
    }

    @Override
    public ItemStatList getStatList() {
        return statList;
    }
}
