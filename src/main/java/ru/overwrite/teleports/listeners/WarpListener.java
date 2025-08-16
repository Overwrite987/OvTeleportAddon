package ru.overwrite.teleports.listeners;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.commands.WarpNotFoundException;
import net.ess3.api.events.UserWarpEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.overwrite.teleports.OvTeleportAddon;
import ru.overwrite.teleports.TeleportManager;
import ru.overwrite.teleports.configuration.Config;
import ru.overwrite.teleports.utils.Utils;

public class WarpListener implements Listener {

    private final TeleportManager teleportManager;
    private final Config pluginConfig;
    private final Essentials essentials;

    public WarpListener(OvTeleportAddon plugin) {
        this.teleportManager = plugin.getTeleportManager();
        this.pluginConfig = plugin.getPluginConfig();
        this.essentials = plugin.getEssentials();
    }

    @EventHandler(ignoreCancelled = true)
    public void onWarp(UserWarpEvent e) {
        if (!pluginConfig.getMainSettings().applyToWarp()) {
            return;
        }
        Player player = e.getUser().getBase();
        Location loc;
        try {
            loc = this.essentials.getWarps().getWarp(e.getWarp());
        } catch (WarpNotFoundException ex) {
            Utils.sendMessage(pluginConfig.getMessages().warpNotFound(), player);
            return;
        }
        teleportManager.preTeleport(player, e.getWarp(), loc, pluginConfig.getWarpSettings());
        e.setCancelled(true);
    }
}
