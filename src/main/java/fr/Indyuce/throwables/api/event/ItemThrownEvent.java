package fr.Indyuce.throwables.api.event;

import fr.Indyuce.throwables.listener.ThrowablesListener;
import fr.Indyuce.throwables.player.PlayerData;
import fr.Indyuce.throwables.throwable.ThrownItem;
import org.bukkit.event.HandlerList;

/**
 * This event is called after {@link PlayerThrowItemEvent} and
 * is used to manipulate the provided ThrownItem instance as
 * the previous event does not provide it.
 * <p>
 * The previous event can be cancelled but this one cannot.
 * See how it's implemented in {@link ThrowablesListener}
 */
public class ItemThrownEvent extends PlayerDataEvent {
    private final ThrownItem thrown;

    private static final HandlerList handlers = new HandlerList();

    public ItemThrownEvent(PlayerData playerData, ThrownItem thrown) {
        super(playerData);

        this.thrown = thrown;
    }

    public ThrownItem getThrown() {
        return thrown;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
