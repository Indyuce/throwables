package fr.Indyuce.throwables.util;

import fr.Indyuce.throwables.Throwables;
import fr.Indyuce.throwables.throwable.ThrownItem;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.function.Consumer;

/**
 * Handles the Loyalty stat, which is basically a custom version
 * of the vanilla Loyalty trident enchantment. When hitting an
 * entity, the throwable will come back to the owner.
 */
public class LoyaltyHandler implements Listener {
    private final Player player;
    private final EquipmentSlot hand;
    private final ItemStack dropped;
    private final BukkitRunnable runnable = new BukkitRunnable() {
        @Override
        public void run() {
            tick();
        }
    };

    private final Location loc;
    private final Vector initialDirection;

    private double d;

    private static final Random random = new Random();

    private static final double INITIAL_VERTICAL_VELOCITY = .5,
            INITIAL_RADIAL_VELOCITY = 1.5;

    public LoyaltyHandler(ThrownItem thrown) {
        this(thrown.getItemLocation(), thrown.getPlayer(), thrown.getItem(), thrown.getHand());
    }

    public LoyaltyHandler(Location start, Player player, ItemStack dropped, EquipmentSlot hand) {
        this.player = player;
        this.dropped = dropped;
        this.loc = start;
        this.hand = hand;

        // Initial velocity
        Vector dir = player.getLocation().add(0, 1, 0).toVector().subtract(start.toVector());
        double a = MathUtils.getYawRadians(dir);
        a += (random.nextBoolean() ? 1 : -1) * Math.PI / 2; // Random offset
        dir = new Vector(INITIAL_RADIAL_VELOCITY * Math.cos(a), INITIAL_VERTICAL_VELOCITY, INITIAL_RADIAL_VELOCITY * Math.sin(a));

        UtilityMethods.drawVector(loc, dir);

        this.initialDirection = dir;

        // Timer
        runnable.runTaskTimer(Throwables.plugin, 0, 1);
    }

    public ItemStack getDropped() {
        return dropped;
    }

    public Player getPlayer() {
        return player;
    }

    private void close() {
        runnable.cancel();
        giveItemBack();
    }

    private void giveItemBack() {

        // Drop item if player disconnected
        if (!player.isOnline()) {
            player.getWorld().dropItem(player.getLocation(), dropped);
            return;
        }

        // Try to give in hand
        if (!UtilityMethods.isAir(player.getInventory().getItem(hand))) {
            player.getInventory().setItem(hand, dropped);
            return;
        }

        // Give or drop item
        for (ItemStack item : player.getInventory().addItem(dropped).values())
            player.getWorld().dropItem(player.getLocation(), dropped);
    }

    private void tick() {

        if ((d += .04) > 1) {
            close();
            return;
        }

        Vector target = player.getLocation().add(0, 1, 0).toVector();
        Vector added = initialDirection.clone().multiply(1 - d).add(target.subtract(loc.toVector()).multiply(d));

        // Apply percentage of diff-vector every tick
        loc.add(added);

        // Display particle!!!
        displayLine(loc, added, inter -> inter.getWorld().spawnParticle(Particle.TOTEM, inter, 0));
    }

    /**
     * Distance between two consecutive particles
     */
    private static final double STEP = .3;

    private void displayLine(Location loc, Vector vec, Consumer<Location> consumer) {

        double l = vec.length();
        double n = Math.max(1, Math.floor(l / STEP));

        for (int k = 0; k < n; k++) {
            Location intermediate = loc.clone().add(vec.clone().multiply(k / n));
            consumer.accept(intermediate);
        }
    }
}
