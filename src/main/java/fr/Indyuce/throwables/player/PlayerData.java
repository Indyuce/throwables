package fr.Indyuce.throwables.player;

import fr.Indyuce.throwables.throwable.ThrowableHandler;
import fr.Indyuce.throwables.util.UtilityMethods;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerData {
    private final UUID uuid;

    private Player player;

    /**
     * Used to handle throwable cooldowns with THROWABLE_TYPE scope
     * and PLAYER scope. The key used for the PLAYER scope is PLAYER_COOLDOWN
     */
    private final CooldownMap playerCooldowns = new CooldownMap();

    private static final Map<UUID, PlayerData> mapped = new HashMap<>();

    private PlayerData(Player player) {
        this.uuid = player.getUniqueId();
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isOnline() {
        return player != null;
    }

    public void updatePlayer(Player player) {
        this.player = player;
    }

    public CooldownMap getCooldowns() {
        return playerCooldowns;
    }

    /**
     * Checks if a player has an item on cooldown. This supports any cooldown scope.
     *
     * @param item    Item being thrown
     * @param handler Handler of the thrown item
     * @return Null if the item cannot be used. If the returned value is not null,
     *         the runnable can be ran later to apply the cooldown.
     */
    public Runnable checkCooldown(ItemStack item, ThrowableHandler handler) {
        CooldownScope scope = Objects.requireNonNull(handler.getCooldownScope(), "Could not find cooldown scope");
        double cooldown = handler.getCooldown();

        // Player cooldown
        if (scope == CooldownScope.PLAYER)
            return playerCooldowns.isOnCooldown("player_cooldown") ? null : () -> playerCooldowns.applyCooldown("player_cooldown", cooldown);

        // Throwable type cooldown
        if (scope == CooldownScope.THROWABLE_TYPE)
            return playerCooldowns.isOnCooldown(handler.getType().getId()) ? null : () -> playerCooldowns.applyCooldown(handler.getType().getId(), cooldown);

        // Item cooldown
        long lastUse = item.hasItemMeta() ? item.getItemMeta().getPersistentDataContainer().getOrDefault(UtilityMethods.namespacedKey("LastThrow"), PersistentDataType.LONG, 0l) : 0l;
        return lastUse + cooldown * 1000 > System.currentTimeMillis() ? null : () -> {
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(UtilityMethods.namespacedKey("LastThrow"), PersistentDataType.LONG, System.currentTimeMillis());
            item.setItemMeta(meta);
        };
    }

    public static void setup(Player player) {
        if (!mapped.containsKey(player.getUniqueId()))
            mapped.put(player.getUniqueId(), new PlayerData(player));
        else
            mapped.get(player.getUniqueId()).updatePlayer(player);
    }

    public static PlayerData get(Player player) {
        return get(player.getUniqueId());
    }

    public static PlayerData get(UUID uuid) {
        return mapped.get(uuid);
    }
}
