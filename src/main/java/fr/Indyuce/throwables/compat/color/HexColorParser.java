package fr.Indyuce.throwables.compat.color;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexColorParser implements ColorParser {
    private static final Pattern PATTERN = Pattern.compile("<(#|HEX)([a-fA-F0-9]{6})>");

    @Override
    public String parseColors(String format) {
        Matcher match = PATTERN.matcher(format);

        while (match.find()) {
            String color = format.substring(match.start(), match.end());
            format = format.replace(color, "" + net.md_5.bungee.api.ChatColor.of('#' + match.group(2)));
            match = PATTERN.matcher(format);
        }

        return ChatColor.translateAlternateColorCodes('&', format);
    }
}
