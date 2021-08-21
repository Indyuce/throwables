package fr.Indyuce.throwables.throwable;

import fr.Indyuce.throwables.player.PlayerData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public interface ThrowableType<T extends ThrowableHandler> {

    /**
     * @return The handler identifier. Has to be unique and specific to that throwable type.
     */
    String getId();

    /**
     * @param item
     * @return
     */
    T getHandler(ItemStack item);

    T newHandler();

    T loadHandler(ConfigurationSection config);

    double getDefaultCooldown();

    double getDefaultDamage();

    /**
     * Called when a player throws an item. The plugin automatically
     * registers the thrown item (see {@link ThrownItem#}
     *
     * @param item    A CLONE of the item being thrown
     * @param handler The handler of the item being thrown
     * @param player  Player data of player throwing the item
     * @param hand    The hand the item was thrown with
     * @return The live throwable which was created
     */
    ThrownItem throwItem(ItemStack item, T handler, PlayerData player, EquipmentSlot hand);
}
