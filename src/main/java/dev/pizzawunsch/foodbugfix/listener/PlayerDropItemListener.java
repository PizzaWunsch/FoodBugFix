package dev.pizzawunsch.foodbugfix.listener;

import dev.pizzawunsch.foodbugfix.FoodBugFix;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropItemListener implements Listener {

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if(FoodBugFix.getInstance().getDropper().contains(event.getPlayer())) {
            event.setCancelled(true);
            return;
        }
    }
}
