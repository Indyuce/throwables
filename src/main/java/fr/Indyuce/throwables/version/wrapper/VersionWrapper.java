package fr.Indyuce.throwables.version.wrapper;

import fr.Indyuce.throwables.version.objects.NBTItem;
import org.bukkit.inventory.ItemStack;

public interface VersionWrapper {
    NBTItem getNBTItem(ItemStack item);

    String parseColors(String str);
}
