package fr.Indyuce.throwables.compat.color;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public class SimpleColorParser implements ColorParser {

    @Override
    public String parseColors(@NotNull String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }
}
