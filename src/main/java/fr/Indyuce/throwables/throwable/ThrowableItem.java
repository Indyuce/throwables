package fr.Indyuce.throwables.throwable;

import fr.Indyuce.throwables.throwable.itemstat.data.StatData;
import fr.Indyuce.throwables.util.Placeholders;
import fr.Indyuce.throwables.util.Utils;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A throwable item is an item that is stored in the configuration
 * files and that can be generated at any time. The {@link #handler}
 * field contains all specific information about the throwable item,
 * like its stats like damage, throw force...
 * <p>
 * See {@link ThrowableHandler} for more info
 */
public class ThrowableItem {
    private final String id;
    private final ThrowableType type;
    private final ThrowableHandler handler;

    private final Material material;
    private final String name;
    private final List<String> lore;
    private final int modelData;

    public ThrowableItem(ThrowableType type, ConfigurationSection config) {
        Validate.notNull(config, "Config cannot be null");

        this.type = type;
        id = config.getName();
        material = Material.valueOf(Objects.requireNonNull(config.getString("material"), "Please specify a material like IRON_SWORD").toUpperCase().replace(" ", "_").replace("-", "_"));
        name = config.getString("name");
        lore = config.getStringList("lore");
        modelData = config.getInt("custom-model-data");
        handler = type.loadHandler(config);
    }

    /**
     * Public constructor which can be used by other plugins
     */
    public ThrowableItem(ThrowableType type, Material material, String name, List<String> lore, int modelData) {
        this("None", type, material, name, lore, modelData);
    }

    /**
     * Public constructor which can be used by other plugins
     */
    public ThrowableItem(String id, ThrowableType type, Material material, String name, List<String> lore, int modelData) {
        this.id = id;
        this.type = type;
        this.material = material;
        this.name = name;
        this.lore = lore;
        Validate.isTrue(modelData >= 0, "Custom model data must be a positive integer");
        this.modelData = modelData;
        this.handler = type.newHandler();
    }

    /**
     * @return The ID of the throwable item if the item was loaded from
     *         configuration files or given using the public constructor.
     *         If you haven't specified any, it is "None" by default;
     *         it does not change much anyways.
     */
    public String getId() {
        return id;
    }

    public ThrowableType getType() {
        return type;
    }

    public ThrowableHandler getHandler() {
        return handler;
    }

    /**
     * Used to build an item stack with the specific characteristics
     * saved in the ThrowableItem class. This outputs an item with some
     * material that most likely has to be changed afterwards.
     * <p>
     * When using the ThrowableItem constructor, not all the data about
     * the throwable is actually saved in the item. That means something
     * like new ThrowableItem(itemStack).build() will NEVER return the
     * original item stack.
     *
     * @return Built throwable item
     */
    public ItemStack build() {

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        Placeholders placeholders = new Placeholders();

        /*
         * Apply type-specific stats before display name and lore as
         * these stats potentially register some placeholders first.
         */
        for (StatData data : handler.getDatas())
            data.whenApplied(item, meta, placeholders);

        if (name != null)
            meta.setDisplayName(placeholders.apply(name));

        if (lore != null) {
            List<String> lore = new ArrayList<>();
            for (String str : this.lore)
                lore.add(placeholders.apply(str));
            meta.setLore(lore);
        }

        if (modelData > 0)
            meta.setCustomModelData(modelData);

        if (type != null)
            meta.getPersistentDataContainer().set(Utils.namespacedKey("ThrowableType"), PersistentDataType.STRING, type.getId());

        item.setItemMeta(meta);
        return item;
    }
}
