package fr.Indyuce.throwables.throwable.provided.sword;

import fr.Indyuce.throwables.player.PlayerData;
import fr.Indyuce.throwables.throwable.ThrowableType;
import fr.Indyuce.throwables.throwable.ThrownItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ThrowableSwordType implements ThrowableType<ThrowableSwordHandler> {

    @Override
    public String getId() {
        return "default_sword";
    }

    @Override
    public ThrowableSwordHandler getHandler(ItemStack item) {
        return new ThrowableSwordHandler(this, item);
    }

    @Override
    public ThrowableSwordHandler newHandler() {
        return new ThrowableSwordHandler(this);
    }

    @Override
    public ThrowableSwordHandler loadHandler(ConfigurationSection config) {
        return new ThrowableSwordHandler(this, config);
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
    public ThrownItem throwItem(ItemStack item, ThrowableSwordHandler handler, PlayerData player, EquipmentSlot hand) {
        return new ThrownSword(item, handler, player, hand);
    }
}
