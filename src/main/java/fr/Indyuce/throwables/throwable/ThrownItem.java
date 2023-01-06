package fr.Indyuce.throwables.throwable;

import fr.Indyuce.throwables.Throwables;
import fr.Indyuce.throwables.player.PlayerData;
import fr.Indyuce.throwables.util.UtilityMethods;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;

/**
 * Instantiated every time an item is thrown.
 */
public abstract class ThrownItem<T extends ThrowableHandler> {
    private final ItemStack item;
    private final T handler;
    private final PlayerData playerData;
    private final Player player;
    private final EquipmentSlot hand;

    /**
     * When the throwable is kept in the player's inventory.
     * This decreases the item life span to 10s and the
     * item disappears when picked up.
     * <p>
     * This applies for both creative mode, and when the
     * 'kept-on-throw' item option is enabled.
     */
    private final boolean kept;

    private boolean removed;

    protected static final Random random = new Random();

    /**
     * @param item    A CLONE of the item being thrown
     * @param handler The throwableHandler of the item being thrown
     * @param player  Player throwing the item
     * @param hand    The hand used to throw the item
     */
    public ThrownItem(ItemStack item, T handler, PlayerData player, EquipmentSlot hand) {
        this.item = item;
        this.handler = handler;
        this.playerData = player;
        this.player = player.getPlayer();
        this.hand = hand;

        // Important: set amount to 1
        item.setAmount(1);

        // Cache boolean if the item is kept on throw
        kept = player.getPlayer().getGameMode() == GameMode.CREATIVE || handler.isKeptOnThrow();

        // Important: register thrown item
        Throwables.plugin.thrownItemManager.register(this);
    }

    public ItemStack getItem() {
        return item;
    }

    /**
     * @return Item containing all the information needed to
     *         compute the behaviour of the thrown item
     */
    public T getHandler() {
        return handler;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public Player getPlayer() {
        return player;
    }

    public EquipmentSlot getHand() {
        return hand;
    }

    public boolean isRemoved() {
        return removed;
    }

    public boolean isKeptOnThrow() {
        return kept;
    }

    /**
     * Called when the thrown item is destroyed.
     */
    public abstract void whenRemoved();

    /**
     * @return The location of the thrown item mid air. Can be
     *         used to display particles for instance
     */
    public abstract Location getItemLocation();

    /**
     * First tries to put the item in the player's main hand,
     * then tries to add it to his inventory, then drops it on the ground
     * if no other possibility.
     * <p>
     * This is used when picking up throwables.
     */
    public void giveItemBack() {
        if (kept)
            return;

        // In case the player disconnects
        if (!playerData.isOnline()) {
            getPlayer().getWorld().dropItem(getItemLocation(), getItem());
            return;
        }

        if (UtilityMethods.isAir(player.getInventory().getItem(hand))) {
            player.getInventory().setItem(hand, getItem());
            return;
        }

        int empty = player.getInventory().firstEmpty();
        if (empty >= 0) {
            player.getInventory().setItem(empty, getItem());
            return;
        }

        player.getWorld().dropItem(player.getLocation(), getItem());
    }

    /**
     * Deletes the item. Throws an exception if the item is already
     * removed which can be checked using {@link #isRemoved()}
     */
    public void remove() {
        UtilityMethods.isTrue(!removed, "Item already removed");

        removed = true;
        Throwables.plugin.thrownItemManager.unregister(this);
        whenRemoved();
    }


    /**
     * Reduces durability by one point
     *
     * @return If the item broke
     */
    public boolean handleDurabilityLoss() {

        // Check if item is damageable
        if (!(getItem().getItemMeta() instanceof Damageable) || getItem().getItemMeta().isUnbreakable())
            return false;

        int loss = UtilityMethods.getDurabilityLoss(getItem());

        PlayerItemDamageEvent called = new PlayerItemDamageEvent(getPlayer(), getItem(), loss);
        Bukkit.getPluginManager().callEvent(called);
        if (called.isCancelled())
            return false;

        // Break item
        Damageable meta = (Damageable) getItem().getItemMeta();
        if (meta.getDamage() + called.getDamage() >= getItem().getType().getMaxDurability())
            return true;

        meta.setDamage(meta.getDamage() + called.getDamage());
        getItem().setItemMeta((ItemMeta) meta);
        return false;
    }
}
