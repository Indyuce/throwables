package fr.Indyuce.throwables.command;

import fr.Indyuce.throwables.Throwables;
import fr.Indyuce.throwables.throwable.ThrowableItem;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ThrowablesExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("throwables.admin"))
            return false;

        if (args.length == 0) {


            return true;
        }


        if (args[0].equalsIgnoreCase("reload")) {
            Throwables.plugin.reloadConfig();
            Throwables.plugin.throwableManager.loadConfigThrowables();
            sender.sendMessage(ChatColor.YELLOW + "Successfully reloaded Throwables " + Throwables.plugin.getDescription().getVersion());
            sender.sendMessage(ChatColor.YELLOW + "- Loaded " + Throwables.plugin.throwableManager.getThrowables().size() + " item(s)");
            return true;
        }

        if (args[0].equalsIgnoreCase("itemlist")) {

            sender.sendMessage(ChatColor.YELLOW + "Registered throwables (" + Throwables.plugin.throwableManager.getThrowables().size() + "):");
            for (ThrowableItem item : Throwables.plugin.throwableManager.getThrowables())
                sender.sendMessage(ChatColor.YELLOW + "- " + item.getId());

            return true;
        }


        if (args[0].equalsIgnoreCase("give")) {

            if (args.length < 2) {
                sender.sendMessage("Usage: /throwables give <item> (player) (amount)");
                return true;
            }

            // Find item to give
            ThrowableItem item = Throwables.plugin.throwableManager.getThrowable(args[1]);
            if (item == null) {
                sender.sendMessage(ChatColor.RED + "Could not find throwable called '" + args[1] + "'");
                return true;
            }

            // Find target
            Player target = args.length > 2 ? Bukkit.getPlayer("") : sender instanceof Player ? (Player) sender : null;
            if (target == null && args.length > 2) {
                sender.sendMessage(ChatColor.RED + "Could not find player called '" + args[2] + "'");
                return true;
            } else if (target == null && !(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Please specify a player");
                return true;
            }

            // Find amount
            int amount = 1;
            if (args.length > 3)
                try {
                    amount = Integer.parseInt(args[3]);
                    Validate.isTrue(amount > 0);
                } catch (IllegalArgumentException exception) {
                    sender.sendMessage(ChatColor.RED + args[3] + " is not a valid number");
                    return true;
                }

            // Generate item
            ItemStack generated = item.build();
            generated.setAmount(amount);

            // Give item
            target.getInventory().addItem(generated);
            return true;
        }

        return true;
    }
}
