package fr.Indyuce.throwables.player;

public enum CooldownScope {

    /**
     * Most restrictive scope. There is ONLY one
     * cooldown for every item whatever their type.
     */
    PLAYER,

    /**
     * Intermediate scope. Items with the same throwable
     * type, like swords or axes, cannot be used temporarily.
     */
    THROWABLE_TYPE,

    /**
     * Cooldown only applies on one specific item and is
     * saved in the item NBT. It's the least restrictive scope
     */
    ITEM;

    public static final CooldownScope DEFAULT_SCOPE = THROWABLE_TYPE;
}
