package fr.Indyuce.throwables.throwable.provided.axe;

import fr.Indyuce.throwables.throwable.itemstat.BooleanStat;
import fr.Indyuce.throwables.throwable.itemstat.DoubleStat;
import fr.Indyuce.throwables.throwable.itemstat.ItemStatList;
import org.bukkit.inventory.PlayerInventory;

public class ThrowableAxeStatList extends ItemStatList {

    /**
     * Time before the item naturally drops on the ground in seconds.
     * By default, the item drops 10 minutes after not being picked up
     * kinda like vanilla items depops after 10min.
     * <p>
     * The main problem is that once the item is dropped, ANYONE can pick it up
     * whereas when it is still on the item stand, only the thrower can pick it up.
     */
    @Deprecated
    public static DoubleStat LIFE_SPAN = new DoubleStat("life-span", "LifeSpan");

    /**
     * When enabled (disabled by default), after hitting the target,
     * the axe bounces back to the thrower's location like
     * Draven's axes in League of Legends.
     * <p>
     * Incompatible with {@link #LOYALTY}
     */
    public static BooleanStat BOUNCE_BACK = new BooleanStat("bounce-back", "BounceBack");

    /**
     * When enabled (disabled by default), after hitting the target,
     * the axe is directly given back to the thrower.
     * <p>
     * If possible the axe is regiven in the correct slot of the
     * player's inventory. If the slot is occupied, it reaches for
     * the first empty slot using {@link PlayerInventory#firstEmpty()}.
     * Otherwise the item is dropped on the ground.
     * <p>
     * Incompatible with {@link #BOUNCE_BACK}
     */
    public static BooleanStat LOYALTY = new BooleanStat("loyalty", "Loyalty");

    public ThrowableAxeStatList() {
        super();

        /*registerStat(LIFE_SPAN);*/
        registerStat(BOUNCE_BACK);
        registerStat(LOYALTY);
    }
}
