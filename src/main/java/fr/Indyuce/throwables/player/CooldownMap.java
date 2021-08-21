package fr.Indyuce.throwables.player;

import java.util.HashMap;
import java.util.Map;

public class CooldownMap {
    private final Map<String, CooldownInfo> map = new HashMap<>();

    public CooldownInfo getInfo(String path) {
        return map.get(path);
    }

    /**
     * Applies a cooldown
     *
     * @param path     The skill or action path, must be completely unique
     * @param cooldown Initial skill or action cooldown
     */
    public void applyCooldown(String path, double cooldown) {
        map.put(path, new CooldownInfo(cooldown));
    }

    /**
     * @param path The skill or action path, must be completely unique
     * @return If the mechanic can be used by the player
     */
    public boolean isOnCooldown(String path) {
        CooldownInfo found = map.get(path);
        return found != null && !found.hasEnded();
    }
}