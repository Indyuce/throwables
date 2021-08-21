package fr.Indyuce.throwables.throwable.provided.axe;

import fr.Indyuce.throwables.player.PlayerData;
import fr.Indyuce.throwables.throwable.ThrowableType;
import fr.Indyuce.throwables.throwable.ThrownItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ThrowableAxeType implements ThrowableType<ThrowableAxeHandler> {

    @Override
    public String getId() {
        return "default_axe";
    }

    @Override
    public ThrowableAxeHandler getHandler(ItemStack item) {
        return new ThrowableAxeHandler(this, item);
    }

    @Override
    public ThrowableAxeHandler newHandler() {
        return new ThrowableAxeHandler(this);
    }

    @Override
    public ThrowableAxeHandler loadHandler(ConfigurationSection config) {
        return new ThrowableAxeHandler(this, config);
    }

    @Override
    public double getDefaultCooldown() {
        return 2;
    }

    @Override
    public double getDefaultDamage() {
        return 7;
    }

    @Override
    public ThrownItem throwItem(ItemStack item, ThrowableAxeHandler handler, PlayerData player, EquipmentSlot hand) {
        return new ThrownAxe(item, handler, player, hand);
    }
}
