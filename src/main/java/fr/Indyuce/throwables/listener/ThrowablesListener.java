package fr.Indyuce.throwables.listener;

import fr.Indyuce.throwables.Throwables;
import fr.Indyuce.throwables.api.event.ItemThrownEvent;
import fr.Indyuce.throwables.api.event.PlayerThrowItemEvent;
import fr.Indyuce.throwables.player.PlayerData;
import fr.Indyuce.throwables.throwable.ThrowableHandler;
import fr.Indyuce.throwables.throwable.ThrownItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ThrowablesListener implements Listener {

    @EventHandler
    public void a(PlayerInteractEvent event) {
        if (!event.hasItem() || event.getAction() != Action.RIGHT_CLICK_AIR)
            return;

        // Find throwable type
        ItemStack item = event.getItem();
        ThrowableHandler handler = Throwables.plugin.throwableManager.getHandler(item);
        if (handler == null)
            return;

        // Check item cooldown
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.get(player);
        Runnable cooldown = playerData.checkCooldown(item, handler);
        if (cooldown == null)
            return;

        // Call Bukkit event
        PlayerThrowItemEvent called = new PlayerThrowItemEvent(playerData, item, handler, event.getHand());
        Bukkit.getPluginManager().callEvent(called);
        if (called.isCancelled())
            return;

        // Apply item cooldown
        cooldown.run();

        // Throw item
        ThrownItem thrown = handler.getType().throwItem(item.clone(), handler, playerData, event.getHand());
        Bukkit.getPluginManager().callEvent(new ItemThrownEvent(playerData, thrown));

        // Reduce amount in hand (if not in creative)
        if (!thrown.isKeptOnThrow())
            reduceAmount(player, event.getHand());
    }

    private void reduceAmount(Player player, EquipmentSlot hand) {
        ItemStack item = player.getInventory().getItem(hand);

        if (item.getAmount() > 1)
            item.setAmount(item.getAmount() - 1);
        else
            player.getInventory().setItem(hand, null);
    }
}
