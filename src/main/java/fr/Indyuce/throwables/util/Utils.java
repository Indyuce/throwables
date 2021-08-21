package fr.Indyuce.throwables.util;

import fr.Indyuce.throwables.Throwables;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.function.Consumer;

public class Utils {
    private static final double HAND_ANGLE = Math.PI / 4;
    private static final double HAND_RADIUS = .4;

    private static final Random random = new Random();

    public static Vector getHandOffset(Player player, EquipmentSlot hand) {
        Validate.isTrue(hand == EquipmentSlot.HAND || hand == EquipmentSlot.OFF_HAND, "hand must be either HAND or OFF_HAND");

        double a = Math.toRadians(player.getEyeLocation().getYaw());
        double p = hand == EquipmentSlot.HAND ? 1 : -1;
        double x = HAND_RADIUS * Math.cos(a + Math.PI / 2 + p * HAND_ANGLE);
        double z = HAND_RADIUS * Math.sin(a + Math.PI / 2 + p * HAND_ANGLE);

        return new Vector(x, 0d, z);
    }

    /**
     * Iterates through the 9 surrounding chunks and collect all entities
     * from them. Then calls the consumer for every of these entities.
     * <p>
     * This method is used to reduce list lookups, when you need to iterate
     * through the entity near some other entity like a thrown item.
     *
     * @param loc      Base location
     * @param consumer Will be called for every nearby entity
     */
    public static void forEachNearbyEntity(Location loc, Consumer<LivingEntity> consumer) {
        int cx = loc.getChunk().getX();
        int cz = loc.getChunk().getZ();

        for (int x = -1; x < 2; x++)
            for (int z = -1; z < 2; z++)
                for (Entity entity : loc.getWorld().getChunkAt(cx + x, cz + z).getEntities())
                    if (entity instanceof LivingEntity)
                        consumer.accept((LivingEntity) entity);
    }

    public static NamespacedKey namespacedKey(String str) {
        return new NamespacedKey(Throwables.plugin, str);
    }

    /**
     * Draws a vector, with some origin given as a location.
     * <p>
     * Used a lot for testing. Amount of particles used
     * is 3 per block length unit.
     *
     * @param loc Starting location
     * @param dir Vector to display
     */
    public static void drawVector(Location loc, Vector dir) {
        double dx = 1d / (6 * dir.length());

        for (double d = 0; d < 1.01; d += dx) {
            Location intermediate = loc.clone().add(dir.clone().multiply(d));
            intermediate.getWorld().spawnParticle(Particle.REDSTONE, intermediate, 0, new Particle.DustOptions(Color.RED, .3f));
        }
    }

    /**
     * Uses the vanilla durability loss formula to check if the item
     * shoud lose durability. TODO custom durability amounts
     *
     * @param item Item being damaged
     * @return Random durability loss
     */
    public static int getDurabilityLoss(ItemStack item) {
        if (!item.hasItemMeta() || !(item.getItemMeta() instanceof Damageable))
            return 1;

        int unbreakingLevel = item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY);
        return random.nextDouble() < 1 / (1 + unbreakingLevel) ? 0 : 1;
    }

    public static boolean isAir(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }
}
