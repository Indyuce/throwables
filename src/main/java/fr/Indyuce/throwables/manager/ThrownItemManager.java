package fr.Indyuce.throwables.manager;

import fr.Indyuce.throwables.throwable.ThrownItem;

import java.util.HashSet;
import java.util.Set;

/**
 * @deprecated Not used yet
 */
@Deprecated
public class ThrownItemManager {

    /**
     * Mapped LiveThrowables with entity IDs as map keys
     */
    private final Set<ThrownItem> mapped = new HashSet<>();

    public void register(ThrownItem thrown) {
        mapped.add(thrown);
    }

    public void unregister(ThrownItem thrown) {
        mapped.remove(thrown);
    }

    public Set<ThrownItem> getLiving() {
        return mapped;
    }
}
