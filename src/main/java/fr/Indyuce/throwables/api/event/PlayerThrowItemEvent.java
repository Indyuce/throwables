package fr.Indyuce.throwables.api.event;

import fr.Indyuce.throwables.player.PlayerData;
import fr.Indyuce.throwables.throwable.ThrowableHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * See {@link ItemThrownEvent}
 */
public class PlayerThrowItemEvent extends PlayerDataEvent {
    private final ThrowableHandler handler;
    private final ItemStack item;
    private final EquipmentSlot hand;

    private static final HandlerList handlers = new HandlerList();

    public PlayerThrowItemEvent(PlayerData playerData, ItemStack item, ThrowableHandler handler, EquipmentSlot hand) {
        super(playerData);

        this.handler = handler;
        this.item = item;
        this.hand = hand;
    }

    public ThrowableHandler getHandler() {
        return handler;
    }

    public ItemStack getThrownItem() {
        return item;
    }

    public EquipmentSlot getHand() {
        return hand;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
