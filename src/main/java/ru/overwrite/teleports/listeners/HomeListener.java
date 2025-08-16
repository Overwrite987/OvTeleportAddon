package ru.overwrite.teleports.listeners;

import com.earth2me.essentials.User;
import net.ess3.api.events.UserTeleportHomeEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.overwrite.teleports.OvTeleportAddon;
import ru.overwrite.teleports.TeleportManager;
import ru.overwrite.teleports.configuration.Config;

public class HomeListener implements Listener {

    private final TeleportManager teleportManager;
    private final Config pluginConfig;

    public HomeListener(OvTeleportAddon plugin) {
        this.teleportManager = plugin.getTeleportManager();
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler(ignoreCancelled = true)
    public void onHome(UserTeleportHomeEvent e) {
        if (!pluginConfig.getMainSettings().applyToHome()) {
            return;
        }
        User player = (User) e.getUser();
        Location loc = player.getHome(e.getHomeName());
        teleportManager.preTeleport(player.getBase(), e.getHomeName(), loc, pluginConfig.getHomeSettings());
        e.setCancelled(true);
    }
}
