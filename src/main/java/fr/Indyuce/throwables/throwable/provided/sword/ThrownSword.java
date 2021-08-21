package fr.Indyuce.throwables.throwable.provided.sword;

import fr.Indyuce.throwables.Throwables;
import fr.Indyuce.throwables.api.ThrowablesAPI;
import fr.Indyuce.throwables.api.event.ThrowableHitEvent;
import fr.Indyuce.throwables.api.event.ThrowableLandEvent;
import fr.Indyuce.throwables.player.PlayerData;
import fr.Indyuce.throwables.throwable.ThrownItem;
import fr.Indyuce.throwables.util.MathUtils;
import fr.Indyuce.throwables.util.Utils;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ThrownSword extends ThrownItem<ThrowableSwordHandler> implements Listener {
    private final ArmorStand stand;
    private final BukkitRunnable runnable;

    // Used for Euler integration
    private Vector vel;
    private final Location loc;

    private final boolean boomerang, piercing;
    private final double boomerangDistance, piercingDamageMultiplier, throwDamage;
    private final Set<UUID> hitEntities = new HashSet<>();

    private boolean inWall, eventsRegistered, isComingBack;
    private double currentDamageMultiplier = 1, distanceTraveled;

    private int ticksLived = 0;

    /**
     * Downwards acceleration due to gravity, already
     * multiplied by {@link #dt} for numerical integration
     * using Euler's method
     */
    private final Vector g;

    // The sword ticks every 1/20sec
    private static final double dt = 0.05;

    public ThrownSword(ItemStack thrown, ThrowableSwordHandler handler, PlayerData player, EquipmentSlot hand) {
        super(thrown, handler, player, hand);

        // Play sound
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ITEM_TRIDENT_THROW, 1, .7f);

        // Cache important item options
        boomerang = getHandler().isBoomerang();
        boomerangDistance = boomerang ? getHandler().getBoomerangDistance() : 0;
        piercing = getHandler().hasPiercing();
        throwDamage = getHandler().getThrowDamage();
        piercingDamageMultiplier = piercing ? getHandler().getPiercingDamageMultiplier() : 1;

        // Downwards acceleration due to gravity
        g = new Vector(0, -10 * getHandler().getGravityMultiplier(), 0);

        // Find location based on the hand used to throw the item
        loc = getPlayer().getLocation().add(Utils.getHandOffset(getPlayer(), hand));
        loc.setPitch(0);
        loc.setYaw(getPlayer().getEyeLocation().getYaw() + 90);

        // Spawn armor stand
        stand = (ArmorStand) getPlayer().getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setInvisible(true);
        stand.setInvulnerable(true);
        stand.setGravity(false);
        stand.setMarker(false);
        stand.setAI(false);
        stand.setCollidable(false);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            stand.addEquipmentLock(slot, ArmorStand.LockType.REMOVING_OR_CHANGING);
            stand.addEquipmentLock(slot, ArmorStand.LockType.ADDING);
        }

        // Initial velocity
        vel = getPlayer().getEyeLocation().getDirection().multiply(40 * getHandler().getThrowForce());

        // Equip item to armor stand
        stand.getEquipment().setHelmet(thrown);

        // Easier checks for other plugins
        stand.setMetadata(ThrowablesAPI.ARMOR_STAND_TAG, new FixedMetadataValue(Throwables.plugin, true));

        // Start runnable
        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        };
        runnable.runTaskTimer(Throwables.plugin, 0, 1);
    }

    /**
     * @param loc Location containing yaw and pitch
     * @return The axis of rotation of the sword
     */
    @Deprecated
    private Vector getRotationAxis(Location loc) {
        double yaw = Math.toRadians(loc.getYaw());
        double pitch = Math.toRadians(loc.getPitch());

        double r = Math.sin(pitch);
        return new Vector(r * Math.cos(yaw), Math.cos(pitch), r * Math.sin(yaw));
    }

    private static final double ARMOR_STAND_HEIGHT = 1.7;

    /**
     * The size of the thrown sword bounding box. Increasing it
     * makes it easier to land the sword on an enemy.
     */
    private static final double RAY_TRACE_WIDTH = .2;

    private void registerEvents() {
        if (eventsRegistered)
            return;

        eventsRegistered = true;
        Bukkit.getPluginManager().registerEvents(this, Throwables.plugin);
    }

    @EventHandler
    public void a(PlayerInteractAtEntityEvent event) {
        if (event.getPlayer().equals(getPlayer()) && event.getRightClicked().equals(stand)) {
            remove();
            giveItemBack();
            stand.getWorld().playSound(stand.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
        }
    }

    @Override
    public Location getItemLocation() {
        return loc.clone().add(0, ARMOR_STAND_HEIGHT, 0);
    }

    /**
     * @return If the sword has already hit an enemy
     */
    public boolean hasHit() {
        return !hitEntities.isEmpty();
    }

    /**
     * @return If the sword has hit a wall and is waiting to be picked up
     */
    public boolean isInWall() {
        return inWall;
    }

    public boolean hasHit(Entity entity) {
        return hitEntities.contains(entity.getUniqueId());
    }

    /**
     * Sword rotation speed is defined by the constant which
     * multiplies the amount of ticks the entity has lived.
     * <p>
     * Rotation speed = .5 rad/tick = 10 rad/s
     *
     * @return The angle of the sword it makes around its rotation axis
     */
    private double getSwordAngle() {
        return (getHand() == EquipmentSlot.HAND ? 1 : -1) * .4 * ticksLived;
    }

    private boolean canHit() {
        return piercing || hitEntities.isEmpty();
    }

    private static final double PICKUP_RANGE_SQUARED = NumberConversions.square(1);

    private void tick() {
        ticksLived++;

        if (inWall)
            return;

        // HERE COMES THE LATITUDE ADJUSTMENT
        stand.setHeadPose(getEulerAngle(getSwordAngle()));

        // Apply gravity
        vel.add(g.clone().multiply(dt));

        // Pickup item when boomerang is coming back
        if (isComingBack) {

            // Apply special velocity
            vel = getPlayer().getLocation().add(0, 1, 0).subtract(getItemLocation()).toVector().normalize().multiply(30);

            if ((!getPlayer().isOnline() || getPlayer().getLocation().add(0, 1, 0).subtract(getItemLocation()).lengthSquared() < PICKUP_RANGE_SQUARED)) {
                giveItemBack();
                remove();
                return;
            }
        }

        // Get "infinitesimal" movement
        Vector dx = vel.clone().multiply(dt);

        /*
         * This checks if the item has hit a wall. If so, the runnable must stop
         * looking for hit entities, slam the sword into the wall and wait for
         * the player to pick it up again.
         *
         * The first solution would be to simply check if the block is air. Tho that
         * is mathematically weak because if the projectile goes fast enough, the item
         * can easily pass through blocks. With a speed of 30, it does happen.
         *
         * That ray trace also checks for hit entities.
         */
        Vector normalizedVel = vel.clone().normalize();
        RayTraceResult hit = isComingBack ? loc.getWorld().rayTraceEntities(getItemLocation(), normalizedVel, dx.length() * 1.5, RAY_TRACE_WIDTH, entity -> entity instanceof LivingEntity && !(entity instanceof ArmorStand) && !entity.equals(getPlayer()) && !hitEntities.contains(entity.getUniqueId()))
                : canHit() ? loc.getWorld().rayTrace(getItemLocation(), normalizedVel, dx.length() * 1.5, FluidCollisionMode.NEVER, false, RAY_TRACE_WIDTH, entity -> entity instanceof LivingEntity && !(entity instanceof ArmorStand) && !entity.equals(getPlayer()) && !hitEntities.contains(entity.getUniqueId()))
                : loc.getWorld().rayTraceBlocks(getItemLocation(), normalizedVel, dx.length() * 1.5, FluidCollisionMode.NEVER, false);

        // Hit an entity
        if (hit != null && hit.getHitEntity() != null) {

            // Call Bukkit event
            ThrowableHitEvent called = new ThrowableHitEvent(this, (LivingEntity) hit.getHitEntity(), throwDamage * currentDamageMultiplier);
            Bukkit.getPluginManager().callEvent(called);
            if (called.isCancelled())
                return;

            // Make the sword stuck in the ground
            if (!piercing) {

                if (boomerang) {

                    // Make sword go back to its thrower
                    isComingBack = true;

                } else {

                    // Change axe velocity
                    vel.setX((random.nextDouble() - .5) * 7);
                    vel.setZ(4);
                    vel.setZ((random.nextDouble() - .5) * 7);
                }
            }

            hitEntities.add(hit.getHitEntity().getUniqueId());
            currentDamageMultiplier *= piercingDamageMultiplier;

            // Handle durability loss
            if (handleDurabilityLoss()) {
                remove();
                return;
            }

            // Deal damage to target
            ((LivingEntity) hit.getHitEntity()).damage(called.getDamage(), getPlayer());

            // Play effect
            loc.getWorld().playSound(loc, Sound.BLOCK_ANVIL_LAND, 1, 2);
            return;
        }

        // Stucking sword in the wall
        if (hit != null && hit.getHitBlock() != null) {

            // If it's a boomerang, the sword will not get stuck
            if (boomerang) {

                // Reduce durability if not already
                if (!hasHit() && handleDurabilityLoss()) {
                    remove();
                    return;
                }

                isComingBack = true;
                return;
            }

            inWall = true;

            // Register events, useless to do it before the sword hit anything
            registerEvents();

            // Call Bukkit event
            Bukkit.getPluginManager().callEvent(new ThrowableLandEvent(this, hit.getHitBlock(), hit.getHitBlockFace()));

            // Reduce durability if not already
            if (!hasHit() && handleDurabilityLoss()) {
                remove();
                return;
            }

            // Find exactly where to put the sword
            Vector impact = hit.getHitPosition().add(new Vector(0, -ARMOR_STAND_HEIGHT, 0));
            impact.add(hit.getHitBlockFace().getDirection().clone().multiply(getOffsetFromWall(vel, hit.getHitBlockFace()))); // Correct intersection point

            // Teleport armor stand
            loc.setX(impact.getX());
            loc.setY(impact.getY());
            loc.setZ(impact.getZ());

            // Teleport armor stand
            stand.teleport(loc);
            stand.setHeadPose(getStuckAngle(hit.getHitBlockFace()));

            loc.getWorld().playSound(loc, Sound.BLOCK_ANVIL_PLACE, 1, 2);
            //playWallStuckParticles(getItemLocation(), vel, res.getHitBlockFace());
            loc.getWorld().spawnParticle(Particle.CRIT, getItemLocation(), 32, 0, 0, 0, .5f);
            return;
        }

        // Integration for position after checking for a hit block
        loc.add(dx);

        // Update the armor stand position (not too often)
        if (ticksLived % 2 == 0)
            stand.teleport(loc);

        stand.getWorld().spawnParticle(isComingBack ? Particle.TOTEM : Particle.CLOUD, getItemLocation(), 0);
    }

    private EulerAngle getEulerAngle(double a) {
        return new EulerAngle(0, a, MathUtils.getPitchRadians(vel));
    }

    /**
     * The angles in that function are chosen arbitrarily and
     * are applied some random offset to have it cooler in game.
     *
     * @param hit Face of block hit
     * @return Angle at which the sword is displayed when stuck on a wall
     */
    private EulerAngle getStuckAngle(BlockFace hit) {
        switch (hit) {
            case UP:
                // Same as throwing axe
                return getEulerAngle(2.87 + (random.nextDouble() - .5) * .7);
            case DOWN:
                // Same as throwing axe
                return getEulerAngle(5.86 + (random.nextDouble() - .5) * .7);
            default:

                /*
                 * Make it so that the sword really appears to be stuck in the wall
                 *
                 * This is done by compensating the armor stand location yaw and
                 * adding the yaw due to the block face being hit.
                 */
                double yaw = 4.94 + (random.nextDouble() - .5) * .7;
                yaw -= Math.toRadians(stand.getLocation().getYaw());
                yaw += MathUtils.getYawRadians(hit.getDirection());

                return getEulerAngle(yaw);
        }
    }

    /**
     * When the sword hits a wall, it can't just stop moving because it is
     * inside the wall. A good approximation is to take the vector that is
     * perpendicular to the intersection plane and add a fraction of it.
     * <p>
     * The fraction you need to add depends on the hit block face.
     *
     * @param hit Hit block face
     * @return Wall distance offset
     */
    private double getOffsetFromWall(Vector vel, BlockFace hit) {

        double offset = hit == BlockFace.DOWN || hit == BlockFace.UP ? .15 : .1;
        double linear = hit == BlockFace.DOWN || hit == BlockFace.UP ? .3 : .15;

        /*
         * If the velocity is almost parallel to the intersection
         * panel, the sword appears to be floating midair and is not
         * really stuck in the wall.
         *
         * This is mathematically only an issue with non-UP or DOWN
         * block directions because of the sword rotation axis.
         */
        return offset - normalize(vel).dot(hit.getDirection()) * linear;
    }

    private Vector normalize(Vector vec) {
        return vec.lengthSquared() == 0 ? vec : vec.clone().normalize();
    }

    @Override
    public void whenRemoved() {

        // Remove armor stand
        stand.remove();

        // Stop runnable
        runnable.cancel();

        // Close listeners
        PlayerInteractEvent.getHandlerList().unregister(this);
    }
}