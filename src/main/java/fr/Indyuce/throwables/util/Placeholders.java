package fr.Indyuce.throwables.util;

import fr.Indyuce.throwables.Throwables;

import java.util.HashMap;
import java.util.Map;

public class Placeholders {
    private final Map<String, String> placeholders = new HashMap<>();

    public void register(String path, Object obj) {
        placeholders.put(path, obj.toString());
    }

    /**
     * Applies placeholders as well as Bukkit color codes.
     * Potentially hexadecimal color codes if higher than 1.16
     *
     * @param str String with unparsed placeholders
     * @return String with parsed placeholders
     */
    public String apply(String str) {

        while (str.contains("{") && str.substring(str.indexOf("{")).contains("}")) {
            String holder = str.substring(str.indexOf("{") + 1, str.indexOf("}"));
            str = str.replace("{" + holder + "}", placeholders.getOrDefault(holder, "PHE"));
        }

        return Throwables.plugin.versionWrapper.parseColors(str);
    }
}
