package fr.Indyuce.throwables.api.event;

import fr.Indyuce.throwables.throwable.ThrownItem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;

public class ThrowableHitEvent extends PlayerDataEvent {
    private final ThrownItem item;
    private final LivingEntity target;

    private double damage;

    private static final HandlerList handlers = new HandlerList();

    /**
     * Called when a throwable hits an entity
     *
     * @param item   Thrown item
     * @param target Entity being hit
     * @param damage Damage dealt to entity
     */
    public ThrowableHitEvent(ThrownItem item, LivingEntity target, double damage) {
        super(item.getPlayerData());

        this.item = item;
        this.target = target;
        this.damage = damage;
    }

    public ThrownItem getThrowable() {
        return item;
    }

    public LivingEntity getTarget() {
        return target;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
