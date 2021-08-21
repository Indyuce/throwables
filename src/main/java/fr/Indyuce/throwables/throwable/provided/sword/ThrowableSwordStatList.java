package fr.Indyuce.throwables.throwable.provided.sword;

import fr.Indyuce.throwables.throwable.itemstat.BooleanStat;
import fr.Indyuce.throwables.throwable.itemstat.DoubleStat;
import fr.Indyuce.throwables.throwable.itemstat.ItemStatList;

public class ThrowableSwordStatList extends ItemStatList {

    /**
     * When enabled, the sword will act like a boomerang and
     * come back to its thrower once it has reached a maximum distance.
     */
    public static BooleanStat BOOMERANG = new BooleanStat("boomerang", "LikeBoomerang");

    /**
     * Maximum distance of a throwable sword before it
     * comes back like a boomerang
     */
    public static DoubleStat BOOMERANG_MAX_DISTANCE = new DoubleStat("boomerang-max-distance", "BoomerangMaxDistance");

    /**
     * When enabled, the sword can damage multiple targets
     * in the same throw.
     */
    public static BooleanStat PIERCING = new BooleanStat("piercing", "SwordPiercing");

    /**
     * The decrease of damage every time a new entity is hit.
     * When set to .9, the damage loss will be geometric: 90%
     * then 81% etc.
     */
    public static DoubleStat PIERCING_MULTIPLIER = new DoubleStat("piercing-multiplier", "PiercingMultiplier");

    public ThrowableSwordStatList() {
        super();

        registerStat(BOOMERANG);
        registerStat(BOOMERANG_MAX_DISTANCE);
        registerStat(PIERCING);
        registerStat(PIERCING_MULTIPLIER);
    }
}
