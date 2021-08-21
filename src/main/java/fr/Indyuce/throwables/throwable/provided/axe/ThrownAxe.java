package fr.Indyuce.throwables.throwable.provided.axe;

import fr.Indyuce.throwables.Throwables;
import fr.Indyuce.throwables.api.ThrowablesAPI;
import fr.Indyuce.throwables.api.event.ThrowableHitEvent;
import fr.Indyuce.throwables.api.event.ThrowableLandEvent;
import fr.Indyuce.throwables.player.PlayerData;
import fr.Indyuce.throwables.throwable.ThrownItem;
import fr.Indyuce.throwables.util.LoyaltyHandler;
import fr.Indyuce.throwables.util.MathUtils;
import fr.Indyuce.throwables.util.Utils;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
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
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

/**
 * Uses an invisible armor stand to spawn a thrown axe
 * - slowly accelerates downwards due to gravity
 * - rotates around an axis perpendicular to Y
 * - Friction can be edited to change the item max velocity
 * <p>
 * When hitting a wall, the axe stays stuck on it till the
 * player picks it up. When picking it up, it reduces the
 * initial item cooldown by a certain amount.
 * <p>
 * If the axe hits an enemy, the item is dropped on the
 * ground and can still be picked up.
 */
public class ThrownAxe extends ThrownItem<ThrowableAxeHandler> implements Listener {
    private final ArmorStand stand;
    private final BukkitRunnable runnable;
    private final double throwAngle;

    // Used for Euler integration
    private final Vector vel;
    private final Location loc;

    /**
     * Downwards acceleration due to gravity, already
     * multiplied by {@link #dt} for numerical integration
     * using Euler's method
     */
    private final Vector g;

    private boolean inWall, hasHit, eventsRegistered;

    private int ticksLived = 0;

    // The axe ticks every 1/20sec
    private static final double dt = 0.05;

    public ThrownAxe(ItemStack thrown, ThrowableAxeHandler handler, PlayerData player, EquipmentSlot hand) {
        super(thrown, handler, player, hand);

        // Play sound
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ITEM_TRIDENT_THROW, 1, .7f);

        // Throw angle
        throwAngle = Math.toRadians(getPlayer().getEyeLocation().getYaw());

        // Calculate gravity
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

    private static final double ARMOR_STAND_HEIGHT = 1.7;

    /**
     * The size of the thrown axe bounding box. Increasing it
     * makes it easier to land the axe on an enemy.
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
     * @return If the axe has already hit an enemy
     */
    public boolean hasHit() {
        return hasHit;
    }

    /**
     * @return If the axe has hit a wall and is waiting to be picked up
     */
    public boolean isInWall() {
        return inWall;
    }

    /**
     * The angle of throw is determined when the axe is thrown and remains
     * a constant. It's also used when crashing the axe in a wall.
     * <p>
     * It's the player's initial yaw converted to radians
     *
     * @return The angle of throws (radians)
     */
    public double getThrowAngle() {
        return throwAngle;
    }

    /**
     * Axe rotation speed is defined by the constant which
     * multiplies the amount of ticks the entity has lived.
     * <p>
     * Rotation speed = .5 rad/tick = 10 rad/s
     *
     * @return The angle of the axe it makes around its rotation axis
     */
    private double getAxeAngle() {
        return .5 * ticksLived;
    }

    private static final double INITIAL_RADIAL_VELOCITY = 4;

    private void tick() {
        ticksLived++;

        if (inWall)
            return;

        // HERE COMES THE LATITUDE ADJUSTMENT
        stand.setHeadPose(new EulerAngle(0, 0, getAxeAngle()));

        // Integrate for velocity
        vel.add(g.clone().multiply(dt));

        // Get "infinitesimal" movement
        Vector dx = vel.clone().multiply(dt);

        /*
         * This checks if the item has hit a wall. If so, the runnable must stop
         * looking for hit entities, slam the axe into the wall and wait for
         * the player to pick it up again.
         *
         * The first solution would be to simply check if the block is air. Tho that
         * is mathematically weak because if the projectile goes fast enough, the item
         * can easily pass through blocks. With a speed of 30, it does happen.
         *
         * That ray trace also checks for hit entities.
         */
        Vector normalizedVel = vel.clone().normalize();
        RayTraceResult hit = !hasHit ? loc.getWorld().rayTrace(getItemLocation(), normalizedVel, dx.length() * 1.5, FluidCollisionMode.NEVER, false, RAY_TRACE_WIDTH, entity -> entity instanceof LivingEntity && !(entity instanceof ArmorStand) && !entity.equals(getPlayer()))
                : loc.getWorld().rayTraceBlocks(getItemLocation(), normalizedVel, dx.length() * 1.5, FluidCollisionMode.NEVER, false);

        // Hit an entity
        if (!hasHit && hit != null && hit.getHitEntity() != null) {
            hasHit = true;

            // Call Bukkit event
            ThrowableHitEvent called = new ThrowableHitEvent(this, (LivingEntity) hit.getHitEntity(), getHandler().getThrowDamage());
            Bukkit.getPluginManager().callEvent(called);
            if (called.isCancelled())
                return;

            // Handle durability loss
            if (handleDurabilityLoss()) {
                remove();
                return;
            }

            // Deal damage to target
            ((LivingEntity) hit.getHitEntity()).damage(called.getDamage(), getPlayer());

            // Bounce back to thrower
            if (getHandler().doesBounceBack()) {

                // Insane mechanic: you can pick it up mid-air!
                registerEvents();

                /*
                 * Mechanics calculations (freefall object)
                 * y = -g.t²/2 + v0y.t
                 * r = v0r.t
                 *
                 * Given:
                 * - t1 = time of impact
                 * - dr = radial distance
                 * - dz = vertical distance
                 *
                 * Then:
                 * t1 = dr/v0r (v0r must be fixed to our liking)
                 * v0y = dz/t1 + g.t1/2
                 */
                Vector difference = getPlayer().getLocation().subtract(getItemLocation()).toVector();
                double g = -this.g.getY(), dr = difference.clone().setY(0).length(), dz = difference.getY();

                // Calculations
                double t1 = dr / INITIAL_RADIAL_VELOCITY;
                double v0z = dz / t1 + g * t1 / 2;

                // Change axe velocity
                double a = MathUtils.getYawRadians(difference);
                vel.setX(INITIAL_RADIAL_VELOCITY * Math.cos(a));
                vel.setY(v0z);
                vel.setZ(INITIAL_RADIAL_VELOCITY * Math.sin(a));

                // Update throw angle
                loc.setYaw((float) Math.toDegrees(a));

                // Loyalty
            } else if (getHandler().hasLoyalty()) {
                new LoyaltyHandler(this);
                remove();

                // Basic bounce
            } else {

                // Change axe velocity
                vel.setX((random.nextDouble() - .5) * 7);
                vel.setY(10);
                vel.setZ((random.nextDouble() - .5) * 7);

                /*
                 * Upgrade throw angle
                 *
                 * There seems to be an issue with how armor stands reply to yaw
                 * modifications, if you use loc#setYaw(float) and see where the
                 * armor stands faces, it's not the same if you relog.
                 */
                loc.setYaw(MathUtils.getYawDegrees(vel));
            }

            // Play effect
            loc.getWorld().playSound(loc, Sound.BLOCK_ANVIL_LAND, 1, 2);
            return;
        }

        // Stucking axe in the wall
        if (hit != null && hit.getHitBlock() != null) {
            inWall = true;

            // Register events, useless to do it before the axe hit anything
            registerEvents();

            // Call Bukkit event
            Bukkit.getPluginManager().callEvent(new ThrowableLandEvent(this, hit.getHitBlock(), hit.getHitBlockFace()));

            // Reduce durability if not already
            if (!hasHit && handleDurabilityLoss()) {
                remove();
                return;
            }

            // Find exactly where to put the axe
            Vector impact = hit.getHitPosition().add(new Vector(0, -ARMOR_STAND_HEIGHT, 0));
            impact.add(hit.getHitBlockFace().getDirection().clone().multiply(getOffsetFromWall(vel, hit.getHitBlockFace()))); // Correct intersection point

            // Teleport armor stand
            loc.setX(impact.getX());
            loc.setY(impact.getY());
            loc.setZ(impact.getZ());

            // Teleport armor stand
            stand.teleport(loc);
            stand.setHeadPose(new EulerAngle(0, 0, getStuckAngle(hit.getHitBlockFace())));

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

        stand.getWorld().spawnParticle(Particle.CLOUD, getItemLocation(), 0);
    }

    /**
     * The angles in that function are chosen arbitrarily and
     * are applied some random offset to have it cooler in game.
     *
     * @param hit Face of block hit
     * @return Angle at which the axe is displayed when stuck on a wall
     */
    private double getStuckAngle(BlockFace hit) {
        switch (hit) {
            case UP:
                return 2.87 + (random.nextDouble() - .5) * .7;
            case DOWN:
                return 5.86 + (random.nextDouble() - .5) * .7;
            default:
                return 1.37 + (random.nextDouble() - .5) * .7;
        }
    }

    /**
     * When the axe hits a wall, it can't just stop moving because it is
     * inside the wall. A good approximation is to take the vector that is
     * perpendicular to the intersection plane and add a fraction of it.
     * <p>
     * The fraction you need to add depends on the hit block face.
     *
     * @param hit Hit block face
     * @return Wall distance offset
     */
    private double getOffsetFromWall(Vector vel, BlockFace hit) {
        switch (hit) {
            case UP:
            case DOWN:
                return .49;
            default: {

                /*
                 * If the velocity is almost parallel to the intersection
                 * panel, the axe appears to be floating midair and is not
                 * really stuck in the wall.
                 *
                 * This is mathematically only an issue with non-UP or DOWN
                 * block directions because of the axe rotation axis.
                 */
                double m = vel.lengthSquared() == 0 ? 0 : -vel.clone().normalize().dot(hit.getDirection());

                return .25 * m;
            }
        }
    }

    @Deprecated
    private void playWallStuckParticles(Location loc, Vector vel, BlockFace hit) {

        /*  Vector normal = hit.getDirection();*/
        for (int j = 0; j < 16; j++) {

            Vector randomOffset = new Vector(random.nextDouble() - .5, random.nextDouble() - .5, random.nextDouble() - .5).multiply(.5);
            randomOffset.add(vel.clone().multiply(vel.clone().normalize().dot(randomOffset)));

            // Velocity with some random component
            Vector vel1 = vel.clone().normalize().add(randomOffset).normalize();
            loc.getWorld().spawnParticle(Particle.CRIT, loc, 0, vel1.getX(), vel1.getY(), vel1.getZ(), .9f);
        }
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