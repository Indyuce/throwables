package fr.Indyuce.throwables.throwable.itemstat;

import fr.Indyuce.throwables.util.UtilityMethods;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ItemStatList {
    private final Map<String, ItemStat> stats = new HashMap<>();

    /**
     * This is a multiplier stat. It's 1 by default
     * and when set to 1.5, throw force is increased by 50%
     */
    public static final DoubleStat THROW_FORCE = new DoubleStat("force", "ThrowForce");

    /**
     * Damage dealt by the throwing weapon
     */
    public static final DoubleStat THROW_DAMAGE = new DoubleStat("damage", "ThrowDamage");

    /**
     * Cooldown of the item.
     */
    public static final DoubleStat THROW_COOLDOWN = new DoubleStat("cooldown", "ThrowCooldown");

    /**
     * The cooldown scope
     */
    public static final CooldownScopeStat THROW_COOLDOWN_SCOPE = new CooldownScopeStat();

    /**
     * When enabled, the item does not leave the
     * inventory when it is thrown.
     */
    public static final BooleanStat KEEP_ON_THROW = new BooleanStat("keep-on-throw", "KeepOnThrow");

    /**
     * It's a multiplier, meaning setting it to 1.5 will
     * increase gravity by a factor of 1.5
     * <p>
     * NBTTag is slightly different as the Gravity tag could be
     * used elsewhere by another plugin
     */
    public static DoubleStat GRAVITY = new DoubleStat("gravity", "ThrowablesGravity");

    public ItemStatList() {
        registerStat(THROW_DAMAGE);
        registerStat(THROW_FORCE);
        registerStat(KEEP_ON_THROW);
        registerStat(THROW_COOLDOWN);
        registerStat(THROW_COOLDOWN_SCOPE);
        registerStat(GRAVITY);
    }

    public void registerStat(ItemStat stat) {
        UtilityMethods.isTrue(!stats.containsKey(stat.getId()), "A stat already exists with the same ID");

        stats.put(stat.getId(), stat);
    }

    public ItemStat getById(String id) {
        return Objects.requireNonNull(stats.get(id), "Found no stat with ID '" + id + "'");
    }

    public Collection<ItemStat> getStats() {
        return stats.values();
    }
}
