package fr.Indyuce.throwables.player;

import fr.Indyuce.throwables.util.UtilityMethods;

public class CooldownInfo {
    private final long initialCooldown, castTime = System.currentTimeMillis();

    private long nextUse;

    /**
     * The main issue being solved here is allowing external plugins
     * to manipulate the cooldowns of the thrown items. An ongoing
     * cooldown can be reduced with three possible ways:
     * - a FLAT amount of seconds is taken away, see {@link #reduceFlat(double)}
     * - a percentage of the INITIAL cooldown is taken away, see {@link #reduceInitialCooldown(float)}
     * - a percentage of the REMAINING cooldown is taken away, see {@link #reduceRemainingCooldown(float)}
     *
     * @param initialCooldown Initial cooldown, in seconds
     */
    public CooldownInfo(double initialCooldown) {
        this.initialCooldown = (long) (initialCooldown * 1000);
        this.nextUse = castTime + this.initialCooldown;
    }

    /**
     * @return Time in millis where the cooldown info was initialized
     */
    public long getCastTime() {
        return castTime;
    }

    /**
     * @return The initial cooldown duration
     */
    public long getInitialCooldown() {
        return initialCooldown;
    }

    /**
     * @return Remaining time in milliseconds, or 0 if the item is ready.
     */
    public long getRemaining() {
        return Math.max(0, nextUse - System.currentTimeMillis());
    }

    /**
     * @return True if the item is no longer on cooldown.
     */
    public boolean hasEnded() {
        return System.currentTimeMillis() > nextUse;
    }

    /**
     * Takes off a percentage of the remaining cooldown
     *
     * @param p Percentage of the remaining cooldown to remove
     */
    public void reduceRemainingCooldown(float p) {
        UtilityMethods.isTrue(p >= 0 && p <= 1, "p must be between 0 and 1");

        double left = getRemaining();
        nextUse -= (long) (left * p);
    }

    public void reduceInitialCooldown(float p) {
        UtilityMethods.isTrue(p >= 0 && p <= 1, "p must be between 0 and 1");

        nextUse -= initialCooldown * p;
    }

    /**
     * Takes of a specific amount of time off the remaining cooldown.
     *
     * @param t Amount of seconds to take off
     */
    public void reduceFlat(double t) {
        nextUse -= 1000 * t;
    }
}
