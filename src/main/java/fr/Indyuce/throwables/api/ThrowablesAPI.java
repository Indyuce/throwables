package fr.Indyuce.throwables.api;

import fr.Indyuce.throwables.Throwables;
import fr.Indyuce.throwables.api.event.ItemThrownEvent;
import fr.Indyuce.throwables.api.event.PlayerThrowItemEvent;
import fr.Indyuce.throwables.player.PlayerData;
import fr.Indyuce.throwables.throwable.ThrowableHandler;
import fr.Indyuce.throwables.throwable.ThrownItem;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.UUID;

public class ThrowablesAPI {
    private final JavaPlugin plugin;

    /**
     * See {@link #isCustomArmorStand(ArmorStand)} for usage
     */
    public static final String ARMOR_STAND_TAG = "ThrowablesArmorStand";

    /**
     * Util class to make Throwables compatibility easier.
     *
     * @param plugin The plugin using the Throwables API
     */
    public ThrowablesAPI(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * @param player Target player
     * @return Player data of the given player
     */
    public PlayerData getPlayerData(Player player) {
        return PlayerData.get(player);
    }

    /**
     * @param uuid Player UUID
     * @return Player data of the given player
     */
    public PlayerData getPlayerData(UUID uuid) {
        return PlayerData.get(uuid);
    }

    /**
     * @param item Item to check
     * @return If the item can be thrown
     */
    public boolean canBeThrown(ItemStack item) {
        return Throwables.plugin.throwableManager.getHandler(item) != null;
    }

    /**
     * @return Checks if the item can be thrown and if it is not on cooldown.
     */
    public boolean canThrow(Player player, ItemStack item) {
        ThrowableHandler handler = Objects.requireNonNull(Throwables.plugin.throwableManager.getHandler(item), "Cannot throw this item");
        return PlayerData.get(player).checkCooldown(item, handler) != null;
    }

    /**
     * Forces a player to throw an item. This calls all the required
     * Bukkit events and registers the item cooldown. This DOES check
     * for the item cooldown and does apply it
     *
     * @param player Target player
     * @param item   Item thrown
     * @param hand   Hand with which the item is thrown
     * @return Thrown item, or null if it was not thrown
     */
    public ThrownItem throwItem(Player player, ItemStack item, EquipmentSlot hand) {
        Validate.isTrue(hand == EquipmentSlot.HAND || hand == EquipmentSlot.OFF_HAND, "Slot must be either HAND or OFF_HAND");

        // Find throwable handler
        ThrowableHandler handler = Objects.requireNonNull(Throwables.plugin.throwableManager.getHandler(item), "Cannot throw this item");

        // Check for item cooldown
        PlayerData playerData = PlayerData.get(player);
        Runnable cooldown = playerData.checkCooldown(item, handler);
        if (cooldown == null)
            return null;

        // Call Bukkit event
        PlayerThrowItemEvent called = new PlayerThrowItemEvent(playerData, item, handler, hand);
        Bukkit.getPluginManager().callEvent(called);
        if (called.isCancelled())
            return null;

        // Apply cooldown
        cooldown.run();

        // Throw item
        ThrownItem thrown = handler.getType().throwItem(item.clone(), handler, playerData, hand);
        Bukkit.getPluginManager().callEvent(new ItemThrownEvent(playerData, thrown));
        return thrown;
    }

    /**
     * Custom armor stands are used to spawn thrown items. These
     * armor stands are marked with a temporary metadata which
     * key is {@link #ARMOR_STAND_TAG}
     *
     * @return If given armor stand is from Throwables
     */
    public boolean isCustomArmorStand(ArmorStand stand) {
        return stand.hasMetadata(ARMOR_STAND_TAG);
    }
}
