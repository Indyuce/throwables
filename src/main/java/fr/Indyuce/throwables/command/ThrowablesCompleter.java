package fr.Indyuce.throwables.command;

import fr.Indyuce.throwables.Throwables;
import fr.Indyuce.throwables.throwable.ThrowableItem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ThrowablesCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("throwables.admin"))
            return null;

        List<String> list = new ArrayList<>();

        if (args.length == 0) {
            list.add("reload");
            list.add("give");
            list.add("itemlist");
            return list;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("give")) {
                for (ThrowableItem item : Throwables.plugin.throwableManager.getThrowables())
                    list.add(item.getId());
                return list;
            }
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                for (Player player : Bukkit.getOnlinePlayers())
                    list.add(player.getName());
                return list;
            }
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give")) {
                list.add("1");
                list.add("10");
                list.add("64");
                return list;
            }
        }

        return null;
    }
}
